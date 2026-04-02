package org.lingZero.modularization_defend.Blocks.Multiblock.newMultiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.lingZero.modularization_defend.util.DebugLogger;

/**
 * 多方块放置类
 * 负责处理多方块结构的验证和放置流程
 * 
 * 设计原则：
 * 1. 单一职责：只负责放置流程控制，具体操作委托给其他类
 * 2. 开闭原则：通过回调接口支持扩展，对修改关闭
 * 3. 依赖倒置：依赖于抽象的 MultiblockNbtManager，不直接操作 NBT
 * 
 * @author ModularizationDefend Team
 */
public class MultiblockPlacer {
    
    /**
     * 放置配置
     * 控制放置行为的各种选项
     */
    public static class PlacementConfig {
        private final boolean consumeItem;      // 是否消耗物品
        private final boolean allowReplace;     // 是否允许替换方块
        private final boolean checkBuildHeight; // 是否检查建筑高度限制
        
        public PlacementConfig() {
            this(true, true, true);
        }
        
        public PlacementConfig(boolean consumeItem, boolean allowReplace, boolean checkBuildHeight) {
            this.consumeItem = consumeItem;
            this.allowReplace = allowReplace;
            this.checkBuildHeight = checkBuildHeight;
        }
        
        public boolean shouldConsumeItem() { return consumeItem; }
        public boolean shouldAllowReplace() { return allowReplace; }
        public boolean shouldCheckBuildHeight() { return checkBuildHeight; }
    }
    
    /**
     * 尝试放置多方块结构 (使用默认配置)
     * 
     * @param level 世界
     * @param player 玩家
     * @param hand 玩家手部
     * @param hitResult 点击结果
     * @param multiblockBlock 多方块物品对应的方块
     * @param structure 多方块结构定义
     * @param masterBlock 主方块
     * @return 交互结果
     */
    @NotNull
    public static InteractionResult tryPlaceMultiblock(
            @NotNull Level level,
            @NotNull Player player,
            @NotNull InteractionHand hand,
            @NotNull BlockHitResult hitResult,
            @NotNull Block multiblockBlock,
            @NotNull IMultiblockStructure structure,
            @NotNull Block masterBlock
    ) {
        return tryPlaceMultiblock(level, player, hand, hitResult, multiblockBlock, structure, masterBlock, new PlacementConfig());
    }
    
    /**
     * 尝试放置多方块结构 (可配置)
     * 
     * @param level 世界
     * @param player 玩家
     * @param hand 玩家手部
     * @param hitResult 点击结果
     * @param multiblockBlock 多方块物品对应的方块
     * @param structure 多方块结构定义
     * @param masterBlock 主方块
     * @param config 放置配置
     * @return 交互结果
     */
    @NotNull
    public static InteractionResult tryPlaceMultiblock(
            @NotNull Level level,
            @NotNull Player player,
            @NotNull InteractionHand hand,
            @NotNull BlockHitResult hitResult,
            @NotNull Block multiblockBlock,
            @NotNull IMultiblockStructure structure,
            @NotNull Block masterBlock,
            @NotNull PlacementConfig config
    ) {
        DebugLogger.debug("===== MultiblockPlacer.tryPlaceMultiblock 被调用 =====");
        DebugLogger.debug("配置：消耗物品=" + config.shouldConsumeItem() + ", 允许替换=" + config.shouldAllowReplace() + ", 检查高度=" + config.shouldCheckBuildHeight());
        
        BlockPos clickedPos = hitResult.getBlockPos();
        DebugLogger.debug("点击位置：" + clickedPos + ", 结构：" + structure.getName());
        
        // 1. 计算结构原点（根据点击的面）
        BlockPos origin = calculateOrigin(clickedPos, hitResult, structure);
        DebugLogger.debug("计算的结构原点：" + origin);
        
        // 2. 检测是否可以形成多方块
        if (!structure.canForm(level, origin)) {
            DebugLogger.warn("结构验证失败，无法在位置 " + origin + " 形成多方块");
            return InteractionResult.FAIL;
        }
        DebugLogger.debug("结构验证通过");
        
        // 3. 检查是否有足够的空间
        if (!canPlaceAt(level, origin, structure, config)) {
            DebugLogger.warn("空间检查失败，位置 " + origin + " 有方块阻挡");
            return InteractionResult.FAIL;
        }
        DebugLogger.debug("空间检查通过");
        
        // 4. 放置结构
        DebugLogger.info("开始放置结构...");
        if (!placeStructure(level, origin, structure, masterBlock)) {
            DebugLogger.error("结构放置失败");
            return InteractionResult.FAIL;
        }
        DebugLogger.info("结构放置成功！原点：" + origin);
        
        // 5. 如果成功放置，消耗一个物品
        if (config.shouldConsumeItem()) {
            ItemStack itemStack = player.getItemInHand(hand);
            if (!player.isCreative()) {
                itemStack.shrink(1);
                DebugLogger.debug("消耗一个物品，剩余：" + itemStack.getCount());
            }
        }
        
        DebugLogger.info("===== 多方块放置完成，返回 SUCCESS =====");
        return InteractionResult.SUCCESS;
    }
    
    /**
     * 计算结构原点
     * @param clickedPos 点击的位置
     * @param hitResult 点击结果（包含面的信息）
     * @param structure 结构定义
     * @return 结构原点
     */
    @NotNull
    private static BlockPos calculateOrigin(@NotNull BlockPos clickedPos, 
                                           @NotNull BlockHitResult hitResult,
                                           @NotNull IMultiblockStructure structure) {
        // 1. 确定主方块应该出现的位置（点击位置 + 面的方向）
        Direction clickedFace = hitResult.getDirection();
        BlockPos masterPos = clickedPos.relative(clickedFace);
        
        DebugLogger.info("计算结构原点：");
        DebugLogger.info("  点击位置=" + clickedPos);
        DebugLogger.info("  点击面=" + clickedFace);
        DebugLogger.info("  主方块目标位置=" + masterPos);
        DebugLogger.info("  主方块相对坐标=" + structure.getMasterPosition());
        
        // 2. 反推原点：origin + masterRelative = masterPos
        //    origin = masterPos - masterRelative
        int offsetX = -structure.getMasterPosition().getX();
        int offsetY = -structure.getMasterPosition().getY();
        int offsetZ = -structure.getMasterPosition().getZ();
        
        BlockPos origin = masterPos.offset(offsetX, offsetY, offsetZ);
        DebugLogger.info("  偏移量=(" + offsetX + ", " + offsetY + ", " + offsetZ + ")");
        DebugLogger.info("  计算的原点=" + origin);
        DebugLogger.info("  验证：原点 + 主方块坐标 = " + origin.offset(structure.getMasterPosition()) + " (应该等于主方块目标位置)");
        
        return origin;
    }
    
    /**
     * 检查是否可以在指定位置放置结构
     * @param level 世界
     * @param origin 结构原点
     * @param structure 结构定义
     * @param config 放置配置
     * @return 是否可以放置
     */
    private static boolean canPlaceAt(@NotNull Level level, @NotNull BlockPos origin, 
                                     @NotNull IMultiblockStructure structure, 
                                     @NotNull PlacementConfig config) {
        for (BlockPos partPos : structure.getPartPositions()) {
            BlockPos worldPos = origin.offset(partPos);
            BlockState state = level.getBlockState(worldPos);
            
            // 检查是否在可放置高度范围内
            if (config.shouldCheckBuildHeight()) {
                if (worldPos.getY() < level.getMinBuildHeight() || worldPos.getY() >= level.getMaxBuildHeight()) {
                    return false;
                }
            }
            
            // 检查是否为空气或可替换的方块
            if (config.shouldAllowReplace()) {
                if (!state.canBeReplaced() && !state.isAir()) {
                    return false;
                }
            } else {
                if (!state.isAir()) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * 放置结构
     * @param level 世界
     * @param origin 结构原点
     * @param structure 结构定义
     * @param masterBlock 主方块
     * @return 是否成功放置
     */
    private static boolean placeStructure(@NotNull Level level, @NotNull BlockPos origin, 
                                         @NotNull IMultiblockStructure structure, 
                                         @NotNull Block masterBlock) {
        DebugLogger.info("===== 开始放置结构 =====");
        
        // 计算主方块的世界坐标
        BlockPos masterPos = origin.offset(structure.getMasterPosition());
        DebugLogger.info("结构原点：" + origin + ", 主方块位置：" + masterPos);
        
        int placedCount = 0;
        int masterCount = 0;
        int partCount = 0;
        
        for (BlockPos partPos : structure.getPartPositions()) {
            BlockPos worldPos = origin.offset(partPos);
            Block blockToPlace = masterBlock;
            
            if (blockToPlace == null) {
                DebugLogger.warn("主方块为空，跳过位置：" + worldPos);
                continue;
            }
            
            // 1. 先放置方块
            DebugLogger.info("[放置步骤 1] 放置方块：" + worldPos + ", 方块类型：" + blockToPlace.getName().getString());
            BlockState state = blockToPlace.defaultBlockState();
            level.setBlockAndUpdate(worldPos, state);
            placedCount++;
            
            // 2. 立即获取 BlockEntity 并设置 NBT 数据
            DebugLogger.info("[放置步骤 2] 获取 BlockEntity: " + worldPos);
            BlockEntity be = level.getBlockEntity(worldPos);
            if (be != null) {
                DebugLogger.info("[放置步骤 3] BlockEntity 获取成功，类型：" + be.getClass().getSimpleName());
                
                if (structure.isMaster(partPos)) {
                    // 设置主方块 NBT
                    DebugLogger.info("[放置步骤 4] 此方块是主方块，准备设置完整 NBT");
                    setupMasterBlockData(be, structure, worldPos);
                    masterCount++;
                    DebugLogger.info("[放置完成] 主方块 NBT 已设置：" + worldPos);
                } else {
                    // 设置结构方块 NBT
                    DebugLogger.info("[放置步骤 4] 此方块是结构方块，准备写入主方块坐标：" + masterPos);
                    setupPartBlockData(be, structure, masterPos);
                    partCount++;
                    DebugLogger.info("[放置完成] 结构方块 NBT 已设置：" + worldPos + " -> 主方块：" + masterPos);
                }
            } else {
                DebugLogger.error("[放置错误] BlockEntity 为空！位置：" + worldPos);
            }
        }
        
        DebugLogger.info("===== 结构放置完成 =====");
        DebugLogger.info("统计 - 总方块数：" + placedCount + ", 主方块：" + masterCount + ", 结构方块：" + partCount);
        return true;
    }
    
    /**
     * 设置主方块的 NBT 数据
     * @param blockEntity 方块实体
     * @param structure 结构定义
     * @param masterPos 主方块坐标
     */
    private static void setupMasterBlockData(@NotNull BlockEntity blockEntity,
                                            @NotNull IMultiblockStructure structure,
                                            @NotNull BlockPos masterPos) {
        CompoundTag tag = new CompoundTag();
        
        // 写入完整的 NBT 数据
        IMultiblockBlockEntity.setStructureName(tag, structure.getName());
        IMultiblockBlockEntity.writeMasterPos(tag, masterPos);
        IMultiblockBlockEntity.markAsMaster(tag);
        IMultiblockBlockEntity.setFormed(tag);
        
        // 保存 NBT 并标记变更
        blockEntity.loadWithComponents(tag, blockEntity.getLevel().registryAccess());
        blockEntity.setChanged();
        
        // 如果是 AbstractMultiblockBlockEntity，还需要设置字段
        if (blockEntity instanceof AbstractMultiblockBlockEntity multiblockBE) {
            multiblockBE.setController(true);  // 设置为主方块
            multiblockBE.setMultiblockFormed(true);  // 标记为已成型
        }
        
        DebugLogger.debug("[NBT 设置] 主方块 NBT: " + tag);
    }
    
    /**
     * 设置结构方块的 NBT 数据
     * @param blockEntity 方块实体
     * @param structure 结构定义
     * @param masterPos 主方块坐标
     */
    private static void setupPartBlockData(@NotNull BlockEntity blockEntity,
                                          @NotNull IMultiblockStructure structure,
                                          @NotNull BlockPos masterPos) {
        CompoundTag tag = new CompoundTag();
        
        // 写入完整的 NBT 数据（包括结构名称和成型状态）
        IMultiblockBlockEntity.setStructureName(tag, structure.getName());
        IMultiblockBlockEntity.writeMasterPos(tag, masterPos);
        IMultiblockBlockEntity.setFormed(tag);
        
        // 保存 NBT 并标记变更
        blockEntity.loadWithComponents(tag, blockEntity.getLevel().registryAccess());
        blockEntity.setChanged();
        
        // 如果是 AbstractMultiblockBlockEntity，还需要设置字段
        if (blockEntity instanceof AbstractMultiblockBlockEntity multiblockBE) {
            multiblockBE.setControllerPosFromNBT(masterPos);  // 设置主方块坐标
            multiblockBE.setMultiblockFormed(true);  // 标记为已成型
            // isController 默认为 false，不需要显式设置
        }
        
        DebugLogger.debug("[NBT 设置] 结构方块 NBT: " + tag);
    }
    
    /**
     * 检测接口实现回调
     */
    public interface PlacementCallback {
        void onPlacementSuccess(Level level, BlockPos origin, IMultiblockStructure structure);
        void onPlacementFailure(Level level, BlockPos origin, IMultiblockStructure structure, String reason);
    }
}
