package org.lingZero.modularization_defend.Blocks.Multiblock.newMultiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 多方块右键 GUI 处理类
 * 处理玩家右键点击多方块时打开 GUI 的逻辑
 */
public class MultiblockGuiHandler {
    
    /**
     * 处理右键点击事件
     * 
     * @param level 世界
     * @param player 玩家
     * @param hand 手部
     * @param pos 点击的方块位置
     * @return 交互结果
     */
    @NotNull
    public static InteractionResult handleRightClick(@NotNull Level level, @NotNull Player player, 
                                                    @NotNull InteractionHand hand, @NotNull BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) {
            return InteractionResult.PASS;
        }
        
        CompoundTag tag = be.saveWithoutMetadata(level.registryAccess());
        if (tag == null) {
            return InteractionResult.PASS;
        }
        
        // 检查是否为多方块结构的一部分
        BlockPos masterPos = getMasterBlockPosition(level, pos, tag);
        if (masterPos == null) {
            return InteractionResult.PASS;
        }
        
        // 重定向到主方块并打开 GUI
        return openGuiForMaster(level, player, hand, masterPos);
    }
    
    /**
     * 获取主方块位置
     * @param level 世界
     * @param pos 当前方块位置
     * @param tag NBT 标签
     * @return 主方块位置，如果不是结构部分则返回 null
     */
    @Nullable
    private static BlockPos getMasterBlockPosition(@NotNull Level level, @NotNull BlockPos pos, @NotNull CompoundTag tag) {
        // 如果是主方块，直接返回当前位置
        if (IMultiblockBlockEntity.isMaster(tag)) {
            return pos;
        }
        
        // 否则从 NBT 读取主方块坐标
        BlockPos storedMasterPos = IMultiblockBlockEntity.readMasterPos(tag);
        if (storedMasterPos != null) {
            // 验证主方块是否存在
            BlockEntity masterBE = level.getBlockEntity(storedMasterPos);
            if (masterBE != null) {
                CompoundTag masterTag = masterBE.saveWithoutMetadata(level.registryAccess());
                if (masterTag != null && IMultiblockBlockEntity.isMaster(masterTag)) {
                    return storedMasterPos;
                }
            }
        }
        
        return null;
    }
    
    /**
     * 为主方块打开 GUI
     * @param level 世界
     * @param player 玩家
     * @param hand 手部
     * @param masterPos 主方块位置
     * @return 交互结果
     */
    @NotNull
    private static InteractionResult openGuiForMaster(@NotNull Level level, @NotNull Player player, 
                                                     @NotNull InteractionHand hand, @NotNull BlockPos masterPos) {
        // 这里应该调用具体的 GUI 打开逻辑
        // 由于不同多方块的 GUI 可能不同，这里提供一个回调接口
        
        BlockEntity masterBE = level.getBlockEntity(masterPos);
        if (masterBE == null) {
            return InteractionResult.FAIL;
        }
        
        CompoundTag tag = masterBE.saveWithoutMetadata(level.registryAccess());
        if (tag == null || !IMultiblockBlockEntity.isFormed(tag)) {
            return InteractionResult.FAIL;
        }
        
        // 触发 GUI 打开事件
        // 实际使用时需要在具体实现中注册监听器
        return InteractionResult.SUCCESS;
    }
    
    /**
     * GUI 提供者接口
     * 用于扩展不同的 GUI 实现
     */
    public interface GuiProvider {
        /**
         * 为玩家打开 GUI
         * @param level 世界
         * @param player 玩家
         * @param masterPos 主方块位置
         * @return 是否成功打开
         */
        boolean openGui(Level level, Player player, BlockPos masterPos);
        
        /**
         * 获取 GUI 标题
         * @param level 世界
         * @param masterPos 主方块位置
         * @return GUI 标题
         */
        String getGuiTitle(Level level, BlockPos masterPos);
    }
    
    /**
     * 默认的 GUI 提供者（需要具体实现）
     */
    private static GuiProvider defaultGuiProvider = new GuiProvider() {
        @Override
        public boolean openGui(Level level, Player player, BlockPos masterPos) {
            // TODO: 实现具体的 GUI 打开逻辑
            return false;
        }
        
        @Override
        public String getGuiTitle(Level level, BlockPos masterPos) {
            return "Multiblock GUI";
        }
    };
    
    /**
     * 设置自定义 GUI 提供者
     * @param provider GUI 提供者
     */
    public static void setGuiProvider(@NotNull GuiProvider provider) {
        defaultGuiProvider = provider;
    }
    
    /**
     * 获取当前的 GUI 提供者
     * @return GUI 提供者
     */
    @NotNull
    public static GuiProvider getGuiProvider() {
        return defaultGuiProvider;
    }
}
