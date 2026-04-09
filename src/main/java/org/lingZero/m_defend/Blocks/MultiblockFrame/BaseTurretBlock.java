package org.lingZero.m_defend.Blocks.MultiblockFrame;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.lingZero.m_defend.DataComponents.TurretType;
import org.lingZero.m_defend.Items.GuidanceControl.GuidanceControlComponent;
import org.lingZero.m_defend.Items.TargetFilter.BaseTargetFilter;
import org.lingZero.m_defend.Items.TurretCore.frame.TurretCore;
import org.lingZero.m_defend.util.DebugLogger;

/**
 * 炮塔主方块抽象基类
 * 所有炮塔主方块都应继承此类
 */
public abstract class BaseTurretBlock extends Block implements EntityBlock {
    
    /**
     * 获取炮塔类型
     * 子类必须实现此方法以返回对应的炮塔类型
     *
     * @return 炮塔类型枚举
     */
    public abstract TurretType getTurretType();
    
    // 多方块结构尺寸配置（子类可重写）
    public int getStructureWidth() {
        return 0;   // 默认区域宽度（X轴和Z轴的半径，0表示1x1）
    }
    
    public int getStructureHeight() {
        return 2;   // 默认区域高度（Y轴）
    }
    
    /**
     * 获取炮塔掉落物品
     * 子类必须实现此方法以返回对应的炮塔物品
     *
     * @return 炮塔物品
     */
    protected abstract Item getTurretItem();
    
    public BaseTurretBlock(Properties properties) {
        super(properties);
    }
    
    /**
     * 玩家右键点击方块时的处理逻辑
     * 用于安装/替换炮塔核心和过滤器
     */
    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        // 只在服务端执行
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        
        // 获取炮塔 BlockEntity
        if (!(level.getBlockEntity(pos) instanceof BaseTurretBlockEntity turretBE)) {
            return InteractionResult.PASS;
        }
        
        // 获取玩家手持物品
        ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        
        // 如果手持物品为空，不处理
        if (heldItem.isEmpty()) {
            return InteractionResult.PASS;
        }
        
        // 检查是否为炮塔核心
        if (heldItem.getItem() instanceof TurretCore) {
            return handleTurretCoreInstallation(turretBE, player, heldItem);
        }
        
        // 检查是否为制导控制组件
        if (heldItem.getItem() instanceof GuidanceControlComponent) {
            return handleGuidanceControlInstallation(turretBE, player, heldItem);
        }
        
        // 检查是否为目标过滤器
        if (heldItem.getItem() instanceof BaseTargetFilter) {
            return handleFilterInstallation(turretBE, player, heldItem);
        }
        
        return InteractionResult.PASS;
    }
    
    /**
     * 处理炮塔核心的安装/替换
     * 
     * @param turretBE 炮塔 BlockEntity
     * @param player 玩家
     * @param heldItem 手持物品
     * @return 交互结果
     */
    private InteractionResult handleTurretCoreInstallation(BaseTurretBlockEntity turretBE, Player player, ItemStack heldItem) {
        // 获取当前核心槽的物品
        ItemStack currentCore = turretBE.coreItem(null);
        
        if (currentCore.isEmpty()) {
            // 槽位为空，直接安装
            ItemStack toInstall = heldItem.copy();
            toInstall.setCount(1);
            turretBE.coreItem(toInstall);
            
            // 消耗一个物品
            heldItem.shrink(1);
            
            DebugLogger.info("玩家 %s 安装了炮塔核心", player.getName().getString());
            player.sendSystemMessage(Component.translatable("message.modularization_defend.turret_core.installed")
                    .withStyle(style -> style.withColor(0x00ff00)));
        } else {
            // 槽位已有物品，替换
            ItemStack toInstall = heldItem.copy();
            toInstall.setCount(1);
            
            // 设置新核心
            turretBE.coreItem(toInstall);
            
            // 返还旧核心给玩家
            if (!player.getInventory().add(currentCore)) {
                // 如果背包满了，掉落在地上
                player.drop(currentCore, false);
            }
            
            // 消耗手持物品
            heldItem.shrink(1);
            
            DebugLogger.info("玩家 %s 替换了炮塔核心", player.getName().getString());
            player.sendSystemMessage(Component.translatable("message.modularization_defend.turret_core.replaced")
                    .withStyle(style -> style.withColor(0xffff00)));
        }
        
        return InteractionResult.SUCCESS;
    }
    
    /**
     * 处理目标过滤器的安装/替换
     * 
     * @param turretBE 炮塔 BlockEntity
     * @param player 玩家
     * @param heldItem 手持物品
     * @return 交互结果
     */
    private InteractionResult handleFilterInstallation(BaseTurretBlockEntity turretBE, Player player, ItemStack heldItem) {
        // 获取当前过滤器槽的物品
        ItemStack currentFilter = turretBE.targetSelectorItem(null);
        
        if (currentFilter.isEmpty()) {
            // 槽位为空，直接安装
            ItemStack toInstall = heldItem.copy();
            toInstall.setCount(1);
            turretBE.targetSelectorItem(toInstall);
            
            // 消耗一个物品（即使手持多个也只消耗一个）
            heldItem.shrink(1);
            
            DebugLogger.info("玩家 %s 安装了目标过滤器", player.getName().getString());
            player.sendSystemMessage(Component.translatable("message.modularization_defend.target_filter.installed")
                    .withStyle(style -> style.withColor(0x00ff00)));
        } else {
            // 槽位已有物品，替换
            ItemStack toInstall = heldItem.copy();
            toInstall.setCount(1);
            
            // 设置新过滤器
            turretBE.targetSelectorItem(toInstall);
            
            // 返还旧过滤器给玩家
            if (!player.getInventory().add(currentFilter)) {
                // 如果背包满了，掉落在地上
                player.drop(currentFilter, false);
            }
            
            // 消耗手持物品
            heldItem.shrink(1);
            
            DebugLogger.info("玩家 %s 替换了目标过滤器", player.getName().getString());
            player.sendSystemMessage(Component.translatable("message.modularization_defend.target_filter.replaced")
                    .withStyle(style -> style.withColor(0xffff00)));
        }
        
        return InteractionResult.SUCCESS;
    }
    
    /**
     * 处理制导控制组件的安装/替换
     * 
     * @param turretBE 炮塔 BlockEntity
     * @param player 玩家
     * @param heldItem 手持物品
     * @return 交互结果
     */
    private InteractionResult handleGuidanceControlInstallation(BaseTurretBlockEntity turretBE, Player player, ItemStack heldItem) {
        // 获取当前制导控制组件槽的物品
        ItemStack currentGuidance = turretBE.guidanceControlItem(null);
        
        if (currentGuidance.isEmpty()) {
            // 槽位为空，直接安装
            ItemStack toInstall = heldItem.copy();
            toInstall.setCount(1);
            turretBE.guidanceControlItem(toInstall);
            
            // 消耗一个物品
            heldItem.shrink(1);
            
            DebugLogger.info("玩家 %s 安装了制导控制组件", player.getName().getString());
            player.sendSystemMessage(Component.translatable("message.modularization_defend.guidance_control.installed")
                    .withStyle(style -> style.withColor(0x00ff00)));
        } else {
            // 槽位已有物品，替换
            ItemStack toInstall = heldItem.copy();
            toInstall.setCount(1);
            
            // 设置新组件
            turretBE.guidanceControlItem(toInstall);
            
            // 返还旧组件给玩家
            if (!player.getInventory().add(currentGuidance)) {
                // 如果背包满了，掉落在地上
                player.drop(currentGuidance, false);
            }
            
            // 消耗手持物品
            heldItem.shrink(1);
            
            DebugLogger.info("玩家 %s 替换了制导控制组件", player.getName().getString());
            player.sendSystemMessage(Component.translatable("message.modularization_defend.guidance_control.replaced")
                    .withStyle(style -> style.withColor(0xffff00)));
        }
        
        return InteractionResult.SUCCESS;
    }
    
    /**
     * 方块被破坏时调用
     * 注意：不再自动拆除结构，需要使用拆除工具
     */
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            super.onRemove(state, level, pos, newState, movedByPiston);
        }
    }
    
    /**
     * 使用拆除工具拆除整个炮塔结构
     *
     * @param level 世界实例
     * @param mainBlockPos 主方块坐标
     * @param player 玩家实例
     */
    public void dismantleStructure(Level level, BlockPos mainBlockPos, Player player) {
        if (level.isClientSide) {
            return;
        }
        
        DebugLogger.info("开始拆除炮塔结构 at: " + mainBlockPos);
        
        // 获取炮塔 BlockEntity 并弹出所有存储的物品
        if (level.getBlockEntity(mainBlockPos) instanceof BaseTurretBlockEntity turretBE) {
            dropAllStoredItems(level, mainBlockPos, turretBE);
        }
        
        // 破坏所有附属方块
        destroyAffiliateBlocks(level, mainBlockPos);
        
        // 破坏主方块并掉落物品
        level.destroyBlock(mainBlockPos, false); // 不自动掉落，手动控制
        
        // 手动掉落炮塔物品
        ItemStack itemStack = new ItemStack(getTurretItem());
        popResource(level, mainBlockPos, itemStack);
        
        DebugLogger.info("炮塔结构拆除完成，掉落物品: " + getTurretItem().getDescriptionId());
    }
    
    /**
     * 仅弹出炮塔中存储的所有物品，不破坏炮塔
     *
     * @param level 世界实例
     * @param mainBlockPos 主方块坐标
     * @param player 玩家实例
     * @return true 如果成功弹出物品
     */
    public boolean ejectStoredItems(Level level, BlockPos mainBlockPos, Player player) {
        if (level.isClientSide) {
            return false;
        }
        
        // 获取炮塔 BlockEntity
        if (level.getBlockEntity(mainBlockPos) instanceof BaseTurretBlockEntity turretBE) {
            dropAllStoredItems(level, mainBlockPos, turretBE);
            
            DebugLogger.info("已弹出炮塔存储的物品 at: " + mainBlockPos);
            return true;
        }
        
        return false;
    }
    
    /**
     * 弹出炮塔中存储的所有物品并清空槽位
     * 
     * @param level 世界实例
     * @param pos 炮塔位置
     * @param turretBE 炮塔 BlockEntity
     */
    private void dropAllStoredItems(Level level, BlockPos pos, BaseTurretBlockEntity turretBE) {
        int droppedCount = 0;
        
        // 遍历所有槽位
        for (int slot = 0; slot < turretBE.itemHandler.getSlots(); slot++) {
            ItemStack stack = turretBE.itemHandler.getStackInSlot(slot);
            
            if (!stack.isEmpty()) {
                // 弹出物品到世界中
                popResource(level, pos, stack.copy());
                droppedCount++;
                
                DebugLogger.debug("弹出物品: %s x%d (槽位 %d)", 
                    stack.getItem().getDescriptionId(), 
                    stack.getCount(), 
                    slot);
                
                // 清空槽位
                turretBE.setItemInSlot(slot, ItemStack.EMPTY);
            }
        }
        
        if (droppedCount > 0) {
            DebugLogger.info("共弹出 %d 个槽位的物品", droppedCount);
            // 标记数据已更改，需要保存
            turretBE.setChanged();
        }
    }
    
    /**
     * 破坏所有附属方块
     *
     * @param level 世界实例
     * @param mainBlockPos 主方块坐标
     */
    private void destroyAffiliateBlocks(Level level, BlockPos mainBlockPos) {
        int width = getStructureWidth();
        int height = getStructureHeight();
        
        // 计算结构的基准位置（底层）
        BlockPos basePos = mainBlockPos.offset(0, -(height - 1), 0);
        
        // 遍历区域内的所有位置
        for (int x = -width; x <= width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = -width; z <= width; z++) {
                    BlockPos checkPos = basePos.offset(x, y, z);
                    
                    // 跳过主方块位置
                    if (checkPos.equals(mainBlockPos)) {
                        continue;
                    }
                    
                    // 检查是否为附属方块
                    if (level.getBlockState(checkPos).getBlock() instanceof AffiliateBlock) {
                        level.destroyBlock(checkPos, false); // 不掉落物品
                        DebugLogger.debug("破坏附属方块 at: " + checkPos);
                    }
                }
            }
        }
        
        DebugLogger.info("附属方块破坏完成");
    }
}
