package org.lingZero.modularization_defend.Blocks.ElectricityRepeater;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.lingZero.modularization_defend.Blocks.Multiblock.IMultiblockComponent;
import org.lingZero.modularization_defend.Blocks.Multiblock.MultiblockData;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import static org.lingZero.modularization_defend.Register.ModBlockEntities.Electricity_Repeater_BLOCK_ENTITY;

/**
 * 电力中继器方块实体
 * 负责存储数据和状态管理
 */
public class ElectricityRepeaterBlockEntity extends BlockEntity implements IMultiblockComponent, GeoBlockEntity {
    private int value;
    private MultiblockData multiblockData;
    private boolean isController = false; // 初始不是控制器，由 onPlace 设置
    private static final ElectricityRepeaterHandler HANDLER = new ElectricityRepeaterHandler();
    
    // GUI 数据
    private final ContainerData data = new SimpleContainerData(4);
    
    // 存储的主方块坐标（从 NBT 读取，用于重启后恢复）
    private BlockPos storedControllerPos = null;
    
    // GeckoLib 动画缓存
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    
    // NBT 持久化键（用于二级节点存储）
    private static final String NETWORK_NBT_KEY = "EnergyNetwork";
    private static final String CHILD_NODES_KEY = "ChildNodes";
    private static final String PARENT_NODE_KEY = "ParentNode";  // 父节点信息
    
    public ElectricityRepeaterBlockEntity(BlockPos pos, BlockState state) {
        super(Electricity_Repeater_BLOCK_ENTITY.get(), pos, state);
        // 初始化 GUI 数据
        data.set(0, 0);      // 当前能量
        data.set(1, 10000);  // 最大能量
        data.set(2, 0);      // 电网状态（预留）
        data.set(3, 0);      // 升级状态（预留）
    }
    
    /**
     * 设置多方块数据
     */
    @Override
    public void setMultiblockData(MultiblockData multiblockData) {
        this.multiblockData = multiblockData;
    }
    
    /**
     * 当方块被移除时调用
     */
    public void blockRemoved() {
        if (level != null && !level.isClientSide && isMultiblockFormed()) {
            // 标记多方块需要重新检查（虽然即将被破坏，但为了安全起见）
            if (multiblockData != null) {
                multiblockData.setNeedsRecheck();
            }
        }
    }
    
    /**
     * 当邻居方块发生变化时调用
     */
    public void onNeighborChange(BlockPos neighborPos) {
        if (level != null && !level.isClientSide && multiblockData != null && multiblockData.isFormed()) {
            // 如果变化的位置不是结构的一部分，则需要重新验证
            if (!multiblockData.containsBlock(neighborPos)) {
                multiblockData.setNeedsRecheck();
            }
        }
    }
    
    /**
     * 初始化多方块数据（只验证，不放置方块）
     */
    @Override
    public void initializeMultiblock() {
        if (level != null && isController) {
            // 创建并验证多方块数据（使用电力中继器专用类）
            MultiblockData data = new ElectricityRepeaterMultiblockData(level, worldPosition);
            if (data.tryForm()) {
                // 验证成功，保存数据
                this.multiblockData = data;
                setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                
                // 将主方块坐标同步到所有结构方块
                syncControllerPosToAllBlocks(worldPosition);
            } else {
                // 验证失败，清除旧数据（防止重启后数据不一致）
                this.multiblockData = null;
                setChanged();
            }
        }
    }
    
    /**
     * 将主方块坐标同步到所有结构方块
     * @param controllerPos 主方块坐标
     */
    private void syncControllerPosToAllBlocks(BlockPos controllerPos) {
        if (level == null || multiblockData == null) {
            return;
        }
        
        // 遍历所有结构方块
        for (BlockPos pos : multiblockData.getBlocks()) {
            if (level.getBlockEntity(pos) instanceof ElectricityRepeaterBlockEntity blockEntity) {
                // 设置主方块坐标（通过 NBT）
                blockEntity.setControllerPosFromNBT(controllerPos);
            }
        }
    }
    
    /**
     * 从 NBT 设置主方块坐标
     * @param controllerPos 主方块坐标
     */
    public void setControllerPosFromNBT(BlockPos controllerPos) {
        this.storedControllerPos = controllerPos;
        // 标记为已更改（保存数据）
        setChanged();
        // 通知客户端更新（同步数据）
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
    
    /**
     * 尝试形成多方块（通过 Mekanism 的探测器）
     * 使用条件：1.世界存在 2.在主世界 3.是控制器
     * @param player 玩家
     * @return 是否成功形成多方块
     */
    public boolean tryFormMultiblock(Player player) {
        if (level == null || level.isClientSide || !isController()) {
            return false;
        }
        
        // 使用处理器进行多方块搭建（无需额外方块）
        return HANDLER.tryFormMultiblock(level, worldPosition, player);
    }
    
    /**
     * 形成多方块结构（通过方块放置自动检测）
     */
    public void formMultiblockWithPlacement() {
        if (level == null || level.isClientSide) {
            return;
        }
        
        // 使用处理器进行多方块拆解
        HANDLER.formMultiblockWithPlacement(level, worldPosition);
    }
    
    /**
     * 破坏多方块结构（拆分为多个独立方块）
     */
    public void breakMultiblock() {
        if (level == null || level.isClientSide) {
            return;
        }
        
        // 使用处理器进行多方块拆解
        HANDLER.breakMultiblock(level, worldPosition, multiblockData);
        setChanged();
    }
    
    /**
     * 获取多方块数据
     */
    public MultiblockData getMultiblockData() {
        return multiblockData;
    }
    
    /**
     * 获取主方块坐标（优先从 multiblockData 获取）
     */
    @Override
    public BlockPos getControllerPos() {
        // 优先从 multiblockData 获取
        if (multiblockData != null && multiblockData.isFormed()) {
            return multiblockData.getControllerPos();
        }
        // 如果 multiblockData 不存在则返回缓存的主方块坐标（重启后）
        return storedControllerPos;
    }
    
    /**
     * 是否是主方块
     */
    public boolean isController() {
        return isController;
    }
    
    /**
     * 设置是否是主方块
     */
    public void setController(boolean controller) {
        isController = controller;
        // 更新方块状态并通知客户端（调用 setChanged）
        if (level != null && !level.isClientSide) {
            // 标记为已更改（保存数据）
            setChanged();
            // 通知客户端更新（同步数据）
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
    
    /**
     * 通过组件检查是否是主方块
     */
    public boolean isControllerViaComponent() {
        return isController; // 返回 isController 字段（由组件控制）
    }
    
    /**
     * 打开 GUI（通过玩家右键点击）
     * 如果是子方块则打开主方块的 GUI
     * 如果是主方块则打开自己的 GUI
     */
    public InteractionResult use(Player player, InteractionHand hand) {
        if (level != null && !level.isClientSide) {
            // 获取主方块坐标
            BlockPos controllerPos = getControllerPos();
            
            if (controllerPos != null) {
                // 如果是子方块则打开主方块的 GUI
                if (!controllerPos.equals(worldPosition)) {
                    // 获取主方块实体
                    BlockEntity controllerBlockEntity = level.getBlockEntity(controllerPos);
                    if (controllerBlockEntity instanceof ElectricityRepeaterBlockEntity controller) {
                        // 打开主方块的 GUI
                        MenuProvider provider = controller.createMenuProviderAt(controllerPos);
                        player.openMenu(provider);
                        return InteractionResult.SUCCESS;
                    }
                }
            }
            
            // 如果是主方块则打开自己的 GUI
            if (isController()) {
                MenuProvider provider = createMenuProvider();
                player.openMenu(provider);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.SUCCESS;
    }
    
    /**
     * 创建 GUI 提供者
     */
    private MenuProvider createMenuProvider() {
        return createMenuProviderAt(worldPosition);
    }
    
    /**
     * 创建 GUI 提供者（指定位置）
     * @param pos 位置
     */
    private MenuProvider createMenuProviderAt(BlockPos pos) {
        return new MenuProvider() {
            @Override
            public ElectricityRepeaterMenu createMenu(int containerId, net.minecraft.world.entity.player.Inventory playerInventory, net.minecraft.world.entity.player.Player player) {
                return new ElectricityRepeaterMenu(containerId, playerInventory, pos, data);
            }
            
            @Override
            public net.minecraft.network.chat.Component getDisplayName() {
                return net.minecraft.network.chat.Component.translatable("gui.modularization_defend.electricity_repeater");
            }
        };
    }
    
    /**
     * 获取 GUI 数据
     */
    public ContainerData getGuiData() {
        return data;
    }
    
    /**
     * 更新 GUI 数据（每 tick 调用一次）
     */
    public void updateGuiData() {
        if (level != null && !level.isClientSide) {
            // TODO: 实现能量消耗逻辑
            // 获取当前能量
            int currentEnergy = data.get(0);
            if (currentEnergy < data.get(1)) {
                data.set(0, currentEnergy + 10); // 每 tick 增加 10 FE
            }
        }
    }

    /**
     * 是否形成多方块
     */
    public boolean isMultiblockFormed() {
        return multiblockData != null && multiblockData.isFormed();
    }
    
    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.value = tag.getInt("value");
        this.isController = tag.getBoolean("isController");
        // 从 NBT 读取主方块坐标
        if (tag.contains("ControllerPos", 10)) { // 10 = CompoundTag
            CompoundTag controllerPosTag = tag.getCompound("ControllerPos");
            this.storedControllerPos = new BlockPos(
                controllerPosTag.getInt("X"),
                controllerPosTag.getInt("Y"),
                controllerPosTag.getInt("Z")
            );
        }
        // 从 NBT 读取子节点信息（如果存在）
        // TODO: 实现子节点逻辑
        // TODO: 实现 tick 逻辑
        
        // 从 NBT 读取子节点信息（如果存在）
        // 已删除 EnergyNetwork 相关代码
        // 此方法暂时为空，等待重写
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("value", this.value);
        tag.putBoolean("isController", this.isController);
        // 将主方块坐标写入 NBT
        if (storedControllerPos != null) {
            CompoundTag controllerPosTag = new CompoundTag();
            controllerPosTag.putInt("X", storedControllerPos.getX());
            controllerPosTag.putInt("Y", storedControllerPos.getY());
            controllerPosTag.putInt("Z", storedControllerPos.getZ());
            tag.put("ControllerPos", controllerPosTag);
        }
        
        // 将子节点信息写入 NBT（如果存在）
        if (isController()) {
            saveChildrenToNBT(tag);
        }
    }
    
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        // 组织更新数据包（包含 isController 和 ControllerPos）
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putBoolean("isController", this.isController);
        if (storedControllerPos != null) {
            CompoundTag controllerPosTag = new CompoundTag();
            controllerPosTag.putInt("X", storedControllerPos.getX());
            controllerPosTag.putInt("Y", storedControllerPos.getY());
            controllerPosTag.putInt("Z", storedControllerPos.getZ());
            tag.put("ControllerPos", controllerPosTag);
        }
        return tag;
    }
    
    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
        // 返回更新数据包
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }
    
    /**
     * 获取渲染边界框
     * 用于渲染时确定需要渲染的区域
     */
    public net.minecraft.world.phys.AABB getRenderBoundingBox() {
        // 返回一个较大的边界框（64 块半径）
        // 用于渲染时确定需要渲染的区域
        return new net.minecraft.world.phys.AABB(
            worldPosition.getX() - 64,
            worldPosition.getY() - 64,
            worldPosition.getZ() - 64,
            worldPosition.getX() + 66,  // 2 + 64
            worldPosition.getY() + 74,  // 10 + 64
            worldPosition.getZ() + 66   // 2 + 64
        );
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ElectricityRepeaterBlockEntity blockEntity) {
        // 每 tick 执行一次
        if (!level.isClientSide) {
            // 如果是主方块且没有多方块数据，则初始化多方块
            if (blockEntity.isController() && blockEntity.multiblockData == null) {
                blockEntity.initializeMultiblock();
            }
            
            // 如果多方块需要重新验证，则重新验证
            if (blockEntity.multiblockData != null && blockEntity.multiblockData.needsRecheck()) {
                blockEntity.multiblockData.needsRecheck = false; // 重置标志
                // 重新验证多方块
                blockEntity.initializeMultiblock();
            }
            
            // 更新 GUI 数据
            blockEntity.updateGuiData();
        }
    }

    
    // ==================== GeoBlockEntity 接口 =====================
    
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // 注册空控制器
        controllers.add(new AnimationController<>(this, this::idleController));
    }
    
    protected <E extends ElectricityRepeaterBlockEntity> PlayState idleController(final AnimationState<E> state) {
        // 停止动画
        return PlayState.STOP;
    }
    
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // ==================== NBT 持久化 ====================
    
    /**
     * 将子节点信息保存到 NBT
     */
    private void saveChildrenToNBT(CompoundTag tag) {
        // 已删除 EnergyNetwork 相关代码
        // 此方法暂时为空，等待重写
    }
    
    /**
     * 从 NBT 读取子节点信息
     */
    private void loadChildrenFromNBT(CompoundTag networkTag) {
        // 已删除 EnergyNetwork 相关代码
        // 此方法暂时为空，等待重写
    }
}
