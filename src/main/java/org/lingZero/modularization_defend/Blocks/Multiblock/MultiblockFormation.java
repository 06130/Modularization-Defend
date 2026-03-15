package org.lingZero.modularization_defend.Blocks.Multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * 多方块成型处理器
 * 负责处理多方块的检测、放置和验证逻辑
 * 支持任意形状的多方块结构（通过 IMultiblockConfig 配置）
 */
public class MultiblockFormation {
    
    private static final String MOD_ID = "modularization_defend";
    private final Level level;
    private final BlockPos controllerPos;
    private final IMultiblockConfig config;
    
    public MultiblockFormation(Level level, BlockPos controllerPos, IMultiblockConfig config) {
        this.level = level;
        this.controllerPos = controllerPos;
        this.config = config;
    }
    
    /**
     * 检查是否可以形成多方块结构（不放置方块）
     * 
     * @return 如果可以形成返回 true
     */
    public boolean canForm() {
        if (level == null || controllerPos == null) {
            return false;
        }
        
        // 使用配置的 canForm 方法检测
        return config.canForm(level, controllerPos);
    }
    
    /**
     * 尝试形成多方块结构（带玩家提示）
     * 
     * @param player 触发玩家
     * @return 是否成功形成
     */
    public boolean tryForm(@Nullable Player player) {
        if (level == null || level.isClientSide) {
            return false;
        }
        
        // 1. 检测结构
        if (!canForm()) {
            // 检测失败，提示玩家
            if (player != null) {
                Component message = Component.translatable(getMessageKey("multiblock_blocked"));
                player.displayClientMessage(message, true);
            }
            return false;
        }
        
        // 2. 检测通过，放置方块
        placeStructure();
        
        // 3. 验证并注册多方块结构
        return validateAndRegister();
    }
    
    /**
     * 放置多方块结构（一次性放置所有方块）
     */
    public void placeStructure() {
        if (level == null || level.isClientSide) {
            return;
        }
        
        // 使用配置的 place 方法放置
        config.place(level, controllerPos);
    }
    
    /**
     * 验证并注册多方块结构
     * 
     * @return 是否成功
     */
    public boolean validateAndRegister() {
        if (level == null) {
            return false;
        }
        
        // 创建多方块数据并验证
        MultiblockData data = new MultiblockData(level, controllerPos);
        boolean formed = data.tryForm();
        
        if (formed) {
            // 发送更新包给客户端
            BlockState state = level.getBlockState(controllerPos);
            level.sendBlockUpdated(controllerPos, state, state, 3);
            return true;
        }
        
        return false;
    }
    
    /**
     * 填充缺失的方块（自动补全结构）
     * 与 tryForm 不同，这个方法会替换掉空位，而不是检测是否有阻挡
     * 
     * @param blockType 要放置的方块类型
     * @param componentCallback 用于设置新放置方块的控制器标志的回调
     */
    public void fillMissingBlocks(
        net.minecraft.world.level.block.Block blockType,
        java.util.function.BiConsumer<net.minecraft.world.level.block.entity.BlockEntity, Boolean> componentCallback
    ) {
        if (level == null || level.isClientSide) {
            return;
        }
        
        // 获取结构中所有位置
        var positions = config.getComponentPositions(controllerPos);
        
        for (BlockPos pos : positions) {
            // 跳过控制器位置（已经存在）
            if (pos.equals(controllerPos)) {
                continue;
            }
            
            // 检查该位置是否为空或可替换
            if (level.isEmptyBlock(pos) || level.getBlockState(pos).canBeReplaced()) {
                // 放置方块（不触发 onPlace 事件）
                BlockState stateToPlace = blockType.defaultBlockState();
                level.setBlock(pos, stateToPlace, 3); // flags=3 避免触发 onPlace
                
                // 将新方块的 isController 设为 false
                net.minecraft.world.level.block.entity.BlockEntity newBE = level.getBlockEntity(pos);
                if (newBE != null && componentCallback != null) {
                    componentCallback.accept(newBE, false);
                }
            }
        }
        
        // 填充完成后，重新验证
        validateAndRegister();
    }
    
    /**
     * 破坏多方块结构（连锁破坏所有结构方块）
     * 
     * @param level 世界
     * @param brokenPos 被破坏的方块位置（触发连锁破坏的位置）
     * @param dataToBreak 多方块数据
     */
    public static void breakMultiblock(Level level, BlockPos brokenPos, @Nullable MultiblockData dataToBreak) {
        if (level == null || level.isClientSide) {
            return;
        }
        
        // 如果没有传入数据，尝试从管理器获取
        if (dataToBreak == null) {
            dataToBreak = MultiblockManager.getMultiblockByPosition(level, brokenPos);
        }
        
        if (dataToBreak != null && dataToBreak.isFormed()) {
            // 获取所有组成方块的位置
            var blocks = dataToBreak.getBlocks();
            
            // 遍历并破坏所有方块
            for (BlockPos blockPos : blocks) {
                // 跳过已经被破坏的方块（Minecraft 已经处理了它的掉落物）
                // 其他方块设置为空气但不产生掉落物
                if (!blockPos.equals(brokenPos)) {
                    level.setBlock(blockPos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 2);
                }
            }
            
            // 最后破坏多方块数据
            dataToBreak.breakMultiblock();
        }
    }
    
    /**
     * 获取消息的翻译键
     */
    private static String getMessageKey(String key) {
        return "message." + MOD_ID + "." + key;
    }
}
