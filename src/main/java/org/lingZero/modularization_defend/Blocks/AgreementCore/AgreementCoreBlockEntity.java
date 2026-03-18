package org.lingZero.modularization_defend.Blocks.AgreementCore;

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
import org.lingZero.modularization_defend.AgreementCoreNetwork.NetworkMain;
import org.lingZero.modularization_defend.Blocks.Multiblock.IMultiblockComponent;
import org.lingZero.modularization_defend.Blocks.Multiblock.MultiblockData;
import org.lingZero.modularization_defend.Config;

import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import static org.lingZero.modularization_defend.Register.ModBlockEntities.AGREEMENT_CORE_BLOCK_ENTITY;

/**
 * 协议核心方块实体
 * 负责存储数据和状态管理
 * 实现 IEnergyNode 接口作为能源网络的核心节点
 */
public class AgreementCoreBlockEntity extends BlockEntity implements IMultiblockComponent, GeoBlockEntity {
    private int value;
    private MultiblockData multiblockData;
    private boolean isController = false; // 初始不是控制器，由 onPlace 设置
    private static final AgreementCoreHandler HANDLER = new AgreementCoreHandler();
    
    // GUI 数据
    private final ContainerData data = new SimpleContainerData(4);
    
    // 存储的主方块坐标（从 NBT 读取，用于重启后恢复）
    private BlockPos storedControllerPos = null;
    
    // GeckoLib 动画缓存
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    
    // NBT 持久化键
    private static final String NETWORK_NBT_KEY = "EnergyNetwork";
    private static final String NETWORK_NODES_KEY = "Nodes";
    private static final String NETWORK_CONNECTIONS_KEY = "Connections";
    private static final String NODE_POSITION_KEY = "Pos";
    private static final String NODE_TYPE_KEY = "Type";
    
    // 心跳配置 NBT 键
    private static final String HEARTBEAT_INTERVAL_KEY = "HeartbeatInterval";
    private static final String HEARTBEAT_TIMER_KEY = "HeartbeatTimer";
    
    // 路由网络节点地址
    private String nodeAddress = null;
    
    // 分级存储键
    private static final String DIRECT_NODES_KEY = "DirectNodes";  // 一级节点（直接连接）
    private static final String INDIRECT_NODES_KEY = "IndirectNodes";  // 二级节点（通过中继器连接）
    
    // 数据管理器（统一管理所有 NBT 操作）
    private final AgreementCoreDataManager dataManager;
    
    // 心跳机制相关字段
    private int heartbeatTimer = 0; // 心跳计时器
    private int heartbeatInterval = Config.getAgreementCoreHeartbeatInterval(); // 心跳间隔（tick）
    
    public AgreementCoreBlockEntity(BlockPos pos, BlockState state) {
        super(AGREEMENT_CORE_BLOCK_ENTITY.get(), pos, state);
        // 初始化 GUI 数据
        data.set(0, 0);      // 当前能量
        data.set(1, 10000);  // 最大能量
        data.set(2, 0);      // 电网状态（预留）
        data.set(3, 0);      // 升级状态（预留）
        
        // 初始化数据管理器
        this.dataManager = new AgreementCoreDataManager(this);
        
        // 初始化心跳计时器
        this.heartbeatTimer = 0;
        this.heartbeatInterval = Config.getAgreementCoreHeartbeatInterval();
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
            // 创建并验证多方块数据
            MultiblockData data = new MultiblockData(level, worldPosition);
            if (data.tryForm()) {
                // 验证成功，保存数据
                this.multiblockData = data;
                setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                
                // 将主方块坐标同步到所有结构方块
                syncControllerPosToAllBlocks(worldPosition);
                
                // 注册到路由网络
                registerToNetwork();
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
            if (level.getBlockEntity(pos) instanceof AgreementCoreBlockEntity blockEntity) {
                // 设置主方块坐标（通过 NBT）
                blockEntity.setControllerPosFromNBT(controllerPos);
            }
        }
    }
    
    /**
     * 从 NBT 设置主方块坐标（用于同步）
     * @param controllerPos 主方块坐标
     */
    public void setControllerPosFromNBT(BlockPos controllerPos) {
        this.storedControllerPos = controllerPos;
        // 标记为需要保存
        setChanged();
        // 通知客户端更新
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
    
    /**
     * 尝试形成多方块结构
     * 流程：1.检测 2.提示 3.放置
     * @param player 触发玩家
     * @return 是否成功形成
     */
    public boolean tryFormMultiblock(Player player) {
        if (level == null || level.isClientSide || !isController()) {
            return false;
        }
        
        // 委托给处理器处理
        return HANDLER.tryFormMultiblock(level, worldPosition, player);
    }
    
    /**
     * 形成多方块结构（包括自动填充缺失的方块）
     */
    public void formMultiblockWithPlacement() {
        if (level == null || level.isClientSide) {
            return;
        }
        
        // 先检测是否可以形成
        if (!HANDLER.canFormMultiblock(level, worldPosition)) {
            // 检测失败，不放置
            return;
        }
        
        // 检测通过，放置所有方块
        HANDLER.formMultiblockWithPlacement(level, worldPosition);
    }
    
    /**
     * 注册到路由网络
     */
    private void registerToNetwork() {
        if (level == null || level.isClientSide) {
            return;
        }
        
        // 构建节点地址
        nodeAddress = NetworkMain.buildNodeAddress(
            "modularization_defend",
            "agreement_core",
            worldPosition
        );
        
        // 注册节点
        NetworkMain.registerNode(nodeAddress, this);
        
        // 记录日志
        org.slf4j.LoggerFactory.getLogger("modularization_defend")
            .info("AgreementCore registered to network: {}", nodeAddress);
    }
    
    /**
     * 从路由网络注销
     */
    private void unregisterFromNetwork() {
        if (nodeAddress != null) {
            NetworkMain.unregisterNode(nodeAddress);
            nodeAddress = null;
        }
    }
    public void breakMultiblock() {
        if (level == null || level.isClientSide) {
            return;
        }
        
        // 从路由网络注销
        unregisterFromNetwork();
        
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
     * 获取主方块坐标
     */
    @Override
    public BlockPos getControllerPos() {
        // 优先从 multiblockData 获取
        if (multiblockData != null && multiblockData.isFormed()) {
            return multiblockData.getControllerPos();
        }
        // 如果 multiblockData 为空，使用存储的坐标（重启后）
        return storedControllerPos;
    }
    
    /**
     * 检查是否为控制器
     */
    public boolean isController() {
        return isController;
    }
    
    /**
     * 设置是否为控制器
     */
    public void setController(boolean controller) {
        isController = controller;
        // 只在需要时保存数据，避免频繁调用 setChanged
        if (level != null && !level.isClientSide) {
            // 标记为需要保存
            setChanged();
            // 通知世界更新这个方块，以便同步到客户端
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
    
    /**
     * 通过数据组件检查是否为控制器
     */
    public boolean isControllerViaComponent() {
        return isController; // 直接使用内部字段，因为数据组件方案有问题
    }
    
    /**
     * 当玩家右键点击方块时打开 GUI
     * 如果右键的不是主方块，会重定向到主方块
     */
    public InteractionResult use(Player player, InteractionHand hand) {
        if (level != null && !level.isClientSide) {
            // 获取主方块坐标
            BlockPos controllerPos = getControllerPos();
            
            if (controllerPos != null) {
                // 如果有主方块且不是当前方块，重定向到主方块
                if (!controllerPos.equals(worldPosition)) {
                    // 获取主方块实体
                    BlockEntity controllerBlockEntity = level.getBlockEntity(controllerPos);
                    if (controllerBlockEntity instanceof AgreementCoreBlockEntity controller) {
                        // 打开主方块的 GUI（使用主方块的位置和数据）
                        MenuProvider provider = controller.createMenuProviderAt(controllerPos);
                        player.openMenu(provider);
                        return InteractionResult.SUCCESS;
                    }
                }
            }
            
            // 如果是主方块，打开当前 GUI
            if (isController()) {
                MenuProvider provider = createMenuProvider();
                player.openMenu(provider);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.SUCCESS;
    }
    
    /**
     * 创建 GUI 菜单提供者
     */
    private MenuProvider createMenuProvider() {
        return createMenuProviderAt(worldPosition);
    }
    
    /**
     * 在指定位置创建 GUI 菜单提供者
     * @param pos 方块位置
     */
    private MenuProvider createMenuProviderAt(BlockPos pos) {
        return new MenuProvider() {
            @Override
            public AgreementCoreMenu createMenu(int containerId, net.minecraft.world.entity.player.Inventory playerInventory, net.minecraft.world.entity.player.Player player) {
                return new AgreementCoreMenu(containerId, playerInventory, pos, data);
            }
            
            @Override
            public net.minecraft.network.chat.Component getDisplayName() {
                return net.minecraft.network.chat.Component.translatable("gui.modularization_defend.agreement_core");
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
     * 更新 GUI 数据（每 tick 调用）
     */
    public void updateGuiData() {
        if (level != null && !level.isClientSide) {
            // TODO: 从协议网络系统获取实际能量值
            // 当前使用示例值
            int currentEnergy = data.get(0);
            if (currentEnergy < data.get(1)) {
                data.set(0, currentEnergy + 10); // 示例：每 tick 增加 10 FE
            }
        }
    }
    
    /**
     * 执行心跳逻辑
     * 根据配置的间隔定期执行，可用于：
     * - 同步网络状态
     * - 检测节点连接
     * - 更新能量传输
     * - 其他周期性任务
     */
    protected void onHeartbeat() {
        if (level == null || level.isClientSide()) return;
        
        // 记录日志用于调试
        org.slf4j.LoggerFactory.getLogger("modularization_defend")
            .debug("AgreementCore heartbeat at {}", worldPosition);
        
        // TODO: 在此处添加心跳时需要执行的逻辑
        // 例如：发送心跳数据包、同步网络状态等
    }
    
    /**
     * 设置心跳间隔
     * @param intervalTicks 间隔时间（tick），会自动限制在有效范围内
     */
    public void setHeartbeatInterval(int intervalTicks) {
        // 限制在有效范围内
        this.heartbeatInterval = Math.max(
            AgreementCoreConfig.MIN_HEARTBEAT_INTERVAL,
            Math.min(intervalTicks, AgreementCoreConfig.MAX_HEARTBEAT_INTERVAL)
        );
        
        // 标记为需要保存
        setChanged();
        
        // 记录日志
        org.slf4j.LoggerFactory.getLogger("modularization_defend")
            .info("AgreementCore heartbeat interval set to {} ticks ({} seconds)", 
                this.heartbeatInterval, this.heartbeatInterval / 20.0);
    }
    
    /**
     * 从配置文件加载心跳间隔
     * 在方块初始化时调用，使用最新的全局配置
     */
    public void loadHeartbeatIntervalFromConfig() {
        this.heartbeatInterval = Config.getAgreementCoreHeartbeatInterval();
    }
    
    /**
     * 获取当前心跳间隔
     * @return 心跳间隔（tick）
     */
    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }
    
    /**
     * 重置心跳计时器
     * 立即触发一次心跳
     */
    public void resetHeartbeatTimer() {
        this.heartbeatTimer = this.heartbeatInterval;
    }

    /**
     * 检查多方块是否已成型
     */
    public boolean isMultiblockFormed() {
        return multiblockData != null && multiblockData.isFormed();
    }
    
    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        
        // 使用数据管理器读取所有 NBT 数据
        dataManager.readFromNBT(tag, registries);
        
        // 重新加载后不立即初始化，而是在下一个 tick 再验证
        // 这样可以确保世界和管理器都已经完全加载
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        
        // 使用数据管理器写入所有 NBT 数据
        dataManager.writeToNBT(tag, registries);
    }
    
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        // 确保更新标签包含所有需要同步的数据
        CompoundTag tag = super.getUpdateTag(registries);
        
        // 使用数据管理器写入需要同步的数据
        dataManager.writeToNBT(tag, registries);
        
        return tag;
    }
    
    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
        // 返回数据包以同步到客户端
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }
    
    /**
     * 扩展渲染边界框以覆盖整个多方块结构
     * 这样只要结构中任意一个方块在视野内，整个结构都会渲染
     */
    public net.minecraft.world.phys.AABB getRenderBoundingBox() {
        // 创建一个非常大的边界框，确保覆盖整个视野范围
        // 协议核心结构：5x5x1 底座 + 3x3x12 柱体，总高度 13
        return new net.minecraft.world.phys.AABB(
            worldPosition.getX() - 64,
            worldPosition.getY() - 64,
            worldPosition.getZ() - 64,
            worldPosition.getX() + 66,  // 2 + 64
            worldPosition.getY() + 77,  // 13 + 64
            worldPosition.getZ() + 66   // 2 + 64
        );
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AgreementCoreBlockEntity blockEntity) {
        // 在每次 tick 中执行的操作
        if (!level.isClientSide) {
            // 如果是控制器且没有多方块数据，尝试初始化（用于重启后恢复）
            if (blockEntity.isController() && blockEntity.multiblockData == null) {
                blockEntity.initializeMultiblock();
            }
            
            // 处理延迟的重新验证
            if (blockEntity.multiblockData != null && blockEntity.multiblockData.needsRecheck()) {
                blockEntity.multiblockData.needsRecheck = false; // 重置标志
                // 重新验证结构
                blockEntity.initializeMultiblock();
            }
            
            // 更新 GUI 数据
            blockEntity.updateGuiData();
            
            // 心跳机制：计时器递增
            blockEntity.heartbeatTimer++;
            
            // 检查是否达到心跳间隔
            if (blockEntity.heartbeatTimer >= blockEntity.heartbeatInterval) {
                // 执行心跳逻辑
                blockEntity.onHeartbeat();
                // 重置计时器
                blockEntity.heartbeatTimer = 0;
            }
        }
    }

    // ==================== GeoBlockEntity 实现 ====================
    
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // 暂无动画，添加空控制器
        controllers.add(new AnimationController<>(this, this::idleController));
    }
    
    protected <E extends AgreementCoreBlockEntity> PlayState idleController(final AnimationState<E> state) {
        // 返回空闲状态（无动画）
        return PlayState.STOP;
    }
    
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
    
    // ==================== NBT 持久化方法 ====================
    
}
