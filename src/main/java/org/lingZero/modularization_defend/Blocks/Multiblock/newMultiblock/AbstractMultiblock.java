package org.lingZero.modularization_defend.Blocks.Multiblock.newMultiblock;

import net.minecraft.core.BlockPos;
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
import org.jetbrains.annotations.Nullable;
import org.lingZero.modularization_defend.Event.MultiblockEvents;
import org.lingZero.modularization_defend.Register.ModMultiblockStructures;
import org.lingZero.modularization_defend.util.DebugLogger;

/**
 * 多方块抽象基类
 * 提供快速创建多方块的基础功能和接口
 * <p>
 * 使用示例：
 * 1. 继承此抽象类
 * 2. 实现 createStructure() 方法定义结构
 * 3. 实现 getMasterBlock() 返回主方块
 * 4. 注册到游戏中
 */
public abstract class AbstractMultiblock {
    
    private final String multiblockId;
    private IMultiblockStructure cachedStructure;
    private boolean isRegistered = false;  // 防止重复注册
    
    public AbstractMultiblock(@NotNull String multiblockId) {
        this.multiblockId = multiblockId;
        // 自动注册到事件处理器和注册表（只在首次创建实例时）
        // 注意：由于单例模式，这里只会在第一次调用 getInstance() 时执行一次
        if (!isRegistered) {
            registerToFramework();
            isRegistered = true;
        }
    }
    
    /**
     * 注册到框架（事件处理器 + NeoForge 注册表）
     * 
     * 设计目标：
     * 1. 自动化注册流程，减少手动操作
     * 2. 确保每个多方块实例都被正确注册
     * 3. 避免重复注册（通过 isRegistered 标志）
     * 
     * 注册内容：
     * 1. 事件处理器（MultiblockEvents）
     *    - 用于拦截玩家右键点击事件
     *    - 检测手持物品是否为多方块物品
     *    - 触发放置逻辑
     * 
     * 2. NeoForge 注册表（ModMultiblockStructures）
     *    - 使用 DeferredRegister 机制
     *    - 提供全局查询接口
     *    - 支持懒加载和缓存
     * 
     * 技术要点：
     * - 此方法只在构造函数中调用一次
     * - 由于单例模式，每个多方块类只有一个实例
     * - 注册时机：在创建单例实例时自动完成
     * 
     * @see MultiblockEvents#registerMultiblock(AbstractMultiblock)
     * @see ModMultiblockStructures
     */
    private void registerToFramework() {
        // 1. 注册到事件处理器（用于右键检测）
        MultiblockEvents.registerMultiblock(this);
        DebugLogger.debug("已注册到事件处理器：" + multiblockId);
        
        // 2. 注册到 NeoForge 注册表（用于缓存和查询）
        // 注意：这里不直接操作 DeferredRegister，而是通过 ModMultiblockStructures 的工具方法
        // 具体注册逻辑由 ModMultiblockStructures 管理
        DebugLogger.debug("已标记为待注册到 NeoForge 注册表：" + multiblockId);
    }
    
    /**
     * 获取多方块 ID
     */
    @NotNull
    public String getMultiblockId() {
        return multiblockId;
    }
    
    /**
     * 创建多方块结构定义
     * @return 多方块结构
     */
    @NotNull
    protected abstract IMultiblockStructure createStructure();
    
    /**
     * 获取缓存的结构（避免重复创建）
     */
    @NotNull
    public IMultiblockStructure getStructure() {
        if (cachedStructure == null) {
            cachedStructure = createStructure();
        }
        return cachedStructure;
    }
    
    /**
     * 获取主方块
     * @return 主方块
     */
    @NotNull
    protected abstract Block getMasterBlock();
    
    /**
     * 获取主方块物品（公共方法，用于外部访问）
     * @return 主方块对应的物品
     */
    @NotNull
    public net.minecraft.world.item.Item getMasterItem() {
        return getMasterBlock().asItem();
    }
    
    /**
     * 获取结构方块（用于填充结构）
     * @return 结构方块
     */
    @NotNull
    protected abstract Block getPartBlock();
    
    /**
     * 处理右键点击事件（放置多方块）
     * 
     * @param level 世界
     * @param player 玩家
     * @param hand 手部
     * @param hitResult 点击结果
     * @return 交互结果
     */
    @NotNull
    public InteractionResult onRightClickPlace(@NotNull Level level, @NotNull Player player, 
                                               @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        DebugLogger.debug("===== AbstractMultiblock.onRightClickPlace 被调用 =====");
        ItemStack itemStack = player.getItemInHand(hand);
        
        // 检查玩家是否手持正确的物品
        if (!isValidPlacementItem(itemStack)) {
            DebugLogger.debug("物品验证失败，返回 PASS");
            return InteractionResult.PASS;
        }
        
        DebugLogger.debug("物品验证通过，准备放置多方块");
        DebugLogger.debug("主方块：" + getMasterBlock().getName().getString() + ", 结构名称：" + getStructure().getName());
        
        // 使用 MultiblockPlacer 进行放置
        var result = MultiblockPlacer.tryPlaceMultiblock(
            level,
            player,
            hand,
            hitResult,
            getMasterBlock(),
            getStructure(),
            getMasterBlock()
        );
        
        DebugLogger.debug("MultiblockPlacer 返回结果：" + result);
        return result;
    }
    
    /**
     * 处理右键点击事件（打开 GUI）
     * 
     * @param level 世界
     * @param player 玩家
     * @param hand 手部
     * @param pos 方块位置
     * @return 交互结果
     */
    @NotNull
    public InteractionResult onRightClickGui(@NotNull Level level, @NotNull Player player, 
                                            @NotNull InteractionHand hand, @NotNull BlockPos pos) {
        return MultiblockGuiHandler.handleRightClick(level, player, hand, pos);
    }
    
    /**
     * 处理方块破坏事件
     * 
     * @param level 世界
     * @param pos 被破坏的位置
     * @param player 玩家（可能为 null）
     * @return 是否成功处理
     */
    public boolean onBreak(@NotNull Level level, @NotNull BlockPos pos, @Nullable Player player) {
        return MultiblockBreaker.breakMultiblock(level, pos, player);
    }
    
    /**
     * 处理方块放置事件（在具体 Multiblock Block 的 onPlace 中调用）
     * 注意：此方法仅用于验证已放置的方块，实际的放置由 MultiblockPlacer 处理
     * 
     * @param level 世界
     * @param pos 方块位置
     * @param state 方块状态
     */
    public void onPlace(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state) {
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null && blockEntity instanceof AbstractMultiblockBlockEntity mbbe) {
                // 检查是否已经是主方块且已成型
                if (mbbe.isController() && mbbe.isMultiblockFormed()) {
                    // 已经成型，不需要再次处理
                    return;
                }
                
                // 计算结构原点
                BlockPos origin = calculateOrigin(pos);
                
                // 检查是否可以形成多方块
                if (getStructure().canForm(level, origin)) {
                    // 验证通过，标记为已成型（不重新放置方块）
                    mbbe.setMultiblockFormed(true);
                    mbbe.setChanged();
                    level.sendBlockUpdated(pos, state, state, 3);
                }
            }
        }
    }
    
    /**
     * 处理方块破坏事件（在具体 Multiblock Block 的 onRemove 中调用）
     * 
     * @param level 世界
     * @param pos 方块位置
     * @param player 玩家（可能为 null）
     */
    public void onRemove(@NotNull Level level, @NotNull BlockPos pos, @Nullable Player player) {
        if (!level.isClientSide) {
            // 使用 MultiblockBreaker 破坏整个结构
            MultiblockBreaker.breakMultiblock(level, pos, player);
        }
    }
    
    /**
     * 计算结构原点
     * @param clickedPos 点击的位置
     * @return 结构原点
     */
    @NotNull
    private BlockPos calculateOrigin(@NotNull BlockPos clickedPos) {
        int offsetX = -getStructure().getMasterPosition().getX();
        int offsetZ = -getStructure().getMasterPosition().getZ();
        return clickedPos.offset(offsetX, -getStructure().getMasterPosition().getY(), offsetZ);
    }
    
    /**
     * 放置整个多方块结构
     * @param level 世界
     * @param origin 结构原点
     * @param masterBE 主方块实体
     * @return 是否成功放置
     */
    private boolean placeEntireStructure(@NotNull Level level, @NotNull BlockPos origin, @NotNull BlockEntity masterBE) {
        IMultiblockStructure structure = getStructure();
        Block masterBlock = getMasterBlock();
        
        // 注意：此方法假设已经通过 canPlaceAt() 检查，直接放置即可
        // 如果检查失败，应该在 MultiblockPlacer 中返回 FAIL，不会调用到这里
        
        // 放置所有结构方块
        for (BlockPos partPos : structure.getPartPositions()) {
            BlockPos worldPos = origin.offset(partPos);
            Block blockToPlace = structure.isMaster(partPos) ? masterBlock : masterBlock;
            
            if (blockToPlace == null) {
                DebugLogger.warn("方块为空，跳过位置：" + worldPos);
                continue;
            }
            
            BlockState state = blockToPlace.defaultBlockState();
            level.setBlockAndUpdate(worldPos, state);
            
            // 设置 NBT
            BlockEntity be = level.getBlockEntity(worldPos);
            if (be != null) {
                if (structure.isMaster(partPos)) {
                    // 主方块
                    setupMasterBlockNbt(be, worldPos);
                } else {
                    // 结构方块
                    setupPartBlockNbt(be, worldPos, origin.offset(structure.getMasterPosition()));
                }
            }
        }
        
        return true;
    }
    
    /**
     * 检查是否可以在指定位置放置结构
     * @param level 世界
     * @param origin 结构原点
     * @return 是否可以放置
     */
    private boolean canPlaceAt(@NotNull Level level, @NotNull BlockPos origin) {
        for (BlockPos partPos : getStructure().getPartPositions()) {
            BlockPos worldPos = origin.offset(partPos);
            BlockState state = level.getBlockState(worldPos);
            
            // 检查是否为空气或可替换的方块
            if (!state.canBeReplaced() && !state.isAir()) {
                return false;
            }
            
            // 检查是否在可放置高度范围内
            if (worldPos.getY() < level.getMinBuildHeight() || worldPos.getY() >= level.getMaxBuildHeight()) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 设置主方块的 NBT
     * @param blockEntity 方块实体
     * @param masterPos 主方块坐标
     */
    private void setupMasterBlockNbt(@NotNull BlockEntity blockEntity, @NotNull BlockPos masterPos) {
        CompoundTag tag = new CompoundTag();
        
        // 写入完整的 NBT 数据
        IMultiblockBlockEntity.setStructureName(tag, getStructure().getName());
        IMultiblockBlockEntity.writeMasterPos(tag, masterPos);
        IMultiblockBlockEntity.markAsMaster(tag);
        IMultiblockBlockEntity.setFormed(tag);
        
        blockEntity.loadWithComponents(tag, blockEntity.getLevel().registryAccess());
        
        // 同时设置 BlockEntity 的 isController 字段，确保 saveAdditional 能正确保存
        if (blockEntity instanceof AbstractMultiblockBlockEntity multiblockBE) {
            multiblockBE.setController(true);
            multiblockBE.setMultiblockFormed(true);
        }
    }
    
    /**
     * 设置结构方块的 NBT（存储主方块坐标）
     * @param blockEntity 方块实体
     * @param partPos 结构方块坐标
     * @param masterPos 主方块坐标
     */
    private void setupPartBlockNbt(@NotNull BlockEntity blockEntity, @NotNull BlockPos partPos, @NotNull BlockPos masterPos) {
        CompoundTag tag = new CompoundTag();
        
        // 只写入主方块坐标
        IMultiblockBlockEntity.writeMasterPos(tag, masterPos);
        
        blockEntity.loadWithComponents(tag, blockEntity.getLevel().registryAccess());
        
        // 确保非主方块的 isController 为 false
        if (blockEntity instanceof AbstractMultiblockBlockEntity multiblockBE) {
            multiblockBE.setController(false);
        }
    }
    
    /**
     * 验证物品是否为有效的放置物品
     * @param itemStack 物品堆
     * @return 是否有效
     */
    protected boolean isValidPlacementItem(@NotNull ItemStack itemStack) {
        // 默认实现：检查物品是否为此多方块的物品形式
        // 子类可以重写此方法来自定义判断逻辑
        return true;
    }
    
    /**
     * 初始化主方块的 NBT 数据
     * @param blockEntity 主方块实体
     * @param masterPos 主方块坐标
     */
    protected void initializeMasterNbt(@NotNull BlockEntity blockEntity, @NotNull BlockPos masterPos) {
        CompoundTag tag = new CompoundTag();
        
        // 写入完整的 NBT 数据
        IMultiblockBlockEntity.setStructureName(tag, getStructure().getName());
        IMultiblockBlockEntity.writeMasterPos(tag, masterPos);
        IMultiblockBlockEntity.markAsMaster(tag);
        IMultiblockBlockEntity.setFormed(tag);
        
        blockEntity.loadWithComponents(tag, blockEntity.getLevel().registryAccess());
        blockEntity.setChanged();
    }
    
    /**
     * 初始化结构方块的 NBT 数据
     * @param blockEntity 结构方块实体
     * @param partPos 结构方块坐标
     * @param masterPos 主方块坐标
     */
    protected void initializePartNbt(@NotNull BlockEntity blockEntity, @NotNull BlockPos partPos, @NotNull BlockPos masterPos) {
        CompoundTag tag = new CompoundTag();
        
        // 只写入主方块坐标
        IMultiblockBlockEntity.writeMasterPos(tag, masterPos);
        
        blockEntity.loadWithComponents(tag, blockEntity.getLevel().registryAccess());
        blockEntity.setChanged();
    }
    
    /**
     * 检查多方块是否已成型
     * @param level 世界
     * @param masterPos 主方块位置
     * @return 是否已成型
     */
    public boolean isFormed(@NotNull Level level, @NotNull BlockPos masterPos) {
        BlockEntity be = level.getBlockEntity(masterPos);
        if (be == null) {
            return false;
        }
        
        CompoundTag tag = be.saveWithoutMetadata(level.registryAccess());
        return tag != null && IMultiblockBlockEntity.isFormed(tag);
    }
    
    /**
     * 销毁多方块结构（清除所有 NBT 数据）
     * @param level 世界
     * @param masterPos 主方块位置
     */
    public void destroyMultiblock(@NotNull Level level, @NotNull BlockPos masterPos) {
        // 破坏整个结构
        MultiblockBreaker.breakMultiblock(level, masterPos, null);
    }
    
    /**
     * 刷新多方块状态
     * @param level 世界
     * @param masterPos 主方块位置
     */
    public void refreshMultiblock(@NotNull Level level, @NotNull BlockPos masterPos) {
        // 重新验证结构完整性
        if (!getStructure().canForm(level, masterPos.offset(
            -getStructure().getMasterPosition().getX(),
            -getStructure().getMasterPosition().getY(),
            -getStructure().getMasterPosition().getZ()
        ))) {
            destroyMultiblock(level, masterPos);
        }
    }
}
