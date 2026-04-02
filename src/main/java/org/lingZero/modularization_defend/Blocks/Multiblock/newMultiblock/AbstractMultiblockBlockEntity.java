package org.lingZero.modularization_defend.Blocks.Multiblock.newMultiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 多方块方块实体基类
 * <p>
 * 提供多方块通用功能：
 * - 主方块坐标管理
 * - 多方块成型状态管理
 * - NBT 持久化
 * - 结构验证
 * - GUI 重定向
 * <p>
 * 使用示例：
 * 1. 继承此抽象类
 * 2. 实现 getMultiblock() 返回对应的 AbstractMultiblock 实例
 * 3. 实现 createMenuProvider() 创建 GUI
 */
public abstract class AbstractMultiblockBlockEntity extends BlockEntity {
    
    // NBT 键
    protected static final String MULTIBLOCK_FORMED_KEY = "MultiblockFormed";
    protected static final String CONTROLLER_POS_KEY = IMultiblockBlockEntity.KEY_MASTER_POS;  // 使用 IMultiblockBlockEntity 的键名
    protected static final String IS_CONTROLLER_KEY = "IsController";
    
    // 多方块成型状态
    protected boolean multiblockFormed = false;
    
    // 是否是主方块
    protected boolean isController = false;
    
    // 存储的主方块坐标
    @Nullable
    protected BlockPos storedControllerPos = null;
    
    public AbstractMultiblockBlockEntity(@NotNull BlockEntityType<?> type, @NotNull BlockPos pos, @NotNull BlockState state) {
        super(type, pos, state);
    }
    
    /**
     * 获取对应的多方块实例
     * @return AbstractMultiblock 实例
     */
    @NotNull
    protected abstract AbstractMultiblock getMultiblock();
    
    /**
     * 创建 GUI 提供者
     * @param pos 位置
     * @return MenuProvider
     */
    @NotNull
    protected abstract MenuProvider createMenuProvider(@NotNull BlockPos pos);
    public void initializeMultiblock() {
        if (level != null && isController) {
            BlockPos origin = getStructureOrigin();
            
            // 使用框架验证结构
            boolean canForm = getMultiblock().getStructure().canForm(level, origin);
            if (canForm) {
                // 验证成功，标记为已成型
                setMultiblockFormed(true);
                
                // 将主方块坐标同步到所有结构方块
                syncControllerPosToAllBlocks(worldPosition);
            } else {
                // 验证失败，清除成型状态
                setMultiblockFormed(false);
            }
        }
    }
    
    /**
     * 获取结构原点
     * @return 结构原点
     */
    @NotNull
    protected BlockPos getStructureOrigin() {
        return worldPosition.offset(
            -getMultiblock().getStructure().getMasterPosition().getX(),
            -getMultiblock().getStructure().getMasterPosition().getY(),
            -getMultiblock().getStructure().getMasterPosition().getZ()
        );
    }
    
    /**
     * 将主方块坐标同步到所有结构方块
     * @param controllerPos 主方块坐标
     */
    protected void syncControllerPosToAllBlocks(@NotNull BlockPos controllerPos) {
        if (level == null) {
            return;
        }
        
        // 遍历所有结构方块（通过结构定义）
        for (BlockPos partPos : getMultiblock().getStructure().getPartPositions()) {
            BlockPos worldPos = getStructureOrigin().offset(partPos);
            BlockEntity be = level.getBlockEntity(worldPos);
            if (be instanceof AbstractMultiblockBlockEntity multiblockBE) {
                // 设置主方块坐标
                multiblockBE.setControllerPosFromNBT(controllerPos);
            }
        }
    }
    
    /**
     * 从 NBT 设置主方块坐标
     * @param controllerPos 主方块坐标
     */
    public void setControllerPosFromNBT(@NotNull BlockPos controllerPos) {
        this.storedControllerPos = controllerPos;
        setChanged();
        
        // 通知客户端更新
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
    
    /**
     * 尝试形成多方块
     * @return 是否成功形成多方块
     */
    public boolean tryFormMultiblock() {
        if (level == null || level.isClientSide || !isController()) {
            return false;
        }
        
        initializeMultiblock();
        return isMultiblockFormed();
    }
    
    /**
     * 破坏多方块结构
     */
    public void breakMultiblock() {
        if (level == null || level.isClientSide) {
            return;
        }
        
        getMultiblock().destroyMultiblock(level, worldPosition);
        setChanged();
    }
    
    /**
     * 当方块被移除时调用
     */
    public void blockRemoved() {
        if (level != null && !level.isClientSide && isMultiblockFormed()) {
            breakMultiblock();
        }
    }
    
    /**
     * 当邻居方块发生变化时调用
     * @param neighborPos 邻居方块位置
     */
    public void onNeighborChange(BlockPos neighborPos) {
        // 默认实现：如果多方块已成型，可以在这里处理邻居变化的影响
        // 子类可以重写此方法来自定义逻辑
        if (level != null && !level.isClientSide && isMultiblockFormed()) {
            // TODO: 如果需要检测邻居变化对结构的影响，可以在这里添加逻辑
            // 新框架会自动处理大部分情况
        }
    }
    
    /**
     * 处理玩家右键点击
     * @param player 玩家
     * @param hand 手部
     * @return 交互结果
     */
    @NotNull
    public InteractionResult use(@NotNull Player player, @NotNull InteractionHand hand) {
        if (level != null && !level.isClientSide) {
            // 获取主方块坐标
            BlockPos controllerPos = getControllerPos();
            
            if (controllerPos != null && !controllerPos.equals(worldPosition)) {
                // 如果是子方块则打开主方块的 GUI
                BlockEntity controllerBlockEntity = level.getBlockEntity(controllerPos);
                if (controllerBlockEntity instanceof AbstractMultiblockBlockEntity controllerBE) {
                    MenuProvider provider = controllerBE.createMenuProvider(controllerPos);
                    player.openMenu(provider);
                    return InteractionResult.SUCCESS;
                }
            }
            
            // 如果是主方块则打开自己的 GUI
            if (isController()) {
                MenuProvider provider = createMenuProvider(worldPosition);
                player.openMenu(provider);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.SUCCESS;
    }
    
    /**
     * 获取主方块坐标
     * @return 主方块坐标
     */
    @Nullable
    public BlockPos getControllerPos() {
        return storedControllerPos;
    }
    
    /**
     * 是否是主方块
     * @return 是否是主方块
     */
    public boolean isController() {
        return isController;
    }
    
    /**
     * 设置是否是主方块
     * @param controller 是否是主方块
     */
    public void setController(boolean controller) {
        isController = controller;
        setChanged();
        
        // 通知客户端更新
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
    
    /**
     * 是否形成多方块
     * @return 是否形成多方块
     */
    public boolean isMultiblockFormed() {
        return multiblockFormed;
    }
    
    /**
     * 设置多方块成型状态
     * @param formed 是否成型
     */
    protected void setMultiblockFormed(boolean formed) {
        this.multiblockFormed = formed;
        setChanged();
        
        // 通知客户端更新
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
    
    @Override
    public void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        
        // 读取多方块数据
        this.isController = tag.getBoolean(IS_CONTROLLER_KEY);
        this.multiblockFormed = tag.getBoolean(MULTIBLOCK_FORMED_KEY);
        
        // 从 NBT 读取主方块坐标 (使用 IMultiblockBlockEntity 的统一方法)
        if (tag.contains(IMultiblockBlockEntity.KEY_MASTER_POS, 10)) { // 10 = CompoundTag
            this.storedControllerPos = IMultiblockBlockEntity.readMasterPos(tag);
        }
    }
    
    @Override
    public void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        
        // 保存多方块数据
        tag.putBoolean(IS_CONTROLLER_KEY, this.isController);
        tag.putBoolean(MULTIBLOCK_FORMED_KEY, this.multiblockFormed);
        
        // 将主方块坐标写入 NBT (使用 IMultiblockBlockEntity 的统一方法)
        // 确保所有结构方块都保存主方块坐标，以便破坏时能够正确重定向
        BlockPos masterPosToSave = storedControllerPos;
        if (masterPosToSave == null && isController) {
            // 如果是主方块但没有存储坐标，使用当前位置
            masterPosToSave = worldPosition;
        }
        if (masterPosToSave != null) {
            IMultiblockBlockEntity.writeMasterPos(tag, masterPosToSave);
        }
    }
    
    @Override
    @NotNull
    public CompoundTag getUpdateTag(@NotNull HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putBoolean(IS_CONTROLLER_KEY, this.isController);
        
        if (storedControllerPos != null) {
            IMultiblockBlockEntity.writeMasterPos(tag, storedControllerPos);
        }
        
        return tag;
    }
}
