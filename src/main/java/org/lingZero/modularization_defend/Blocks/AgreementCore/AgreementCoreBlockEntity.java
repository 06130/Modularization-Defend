package org.lingZero.modularization_defend.Blocks.AgreementCore;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
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
import org.lingZero.modularization_defend.EnergyNetwork.*;

import java.util.List;
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
public class AgreementCoreBlockEntity extends BlockEntity implements IMultiblockComponent, GeoBlockEntity, IEnergyNode {
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
    
    // 能源网络相关
    private final EnergyBuffer energyBuffer;
    private long lastScanTime = 0;
    private static final long SCAN_INTERVAL = 5000L; // 5 秒扫描间隔
    
    // NBT 持久化键
    private static final String NETWORK_NBT_KEY = "EnergyNetwork";
    private static final String NETWORK_NODES_KEY = "Nodes";
    private static final String NETWORK_CONNECTIONS_KEY = "Connections";
    private static final String NODE_POSITION_KEY = "Pos";
    private static final String NODE_TYPE_KEY = "Type";
    
    // 分级存储键
    private static final String DIRECT_NODES_KEY = "DirectNodes";  // 一级节点（直接连接）
    private static final String INDIRECT_NODES_KEY = "IndirectNodes";  // 二级节点（通过中继器连接）
    
    public AgreementCoreBlockEntity(BlockPos pos, BlockState state) {
        super(AGREEMENT_CORE_BLOCK_ENTITY.get(), pos, state);
        // 初始化 GUI 数据
        data.set(0, 0);      // 当前能量
        data.set(1, 10000);  // 最大能量
        data.set(2, 0);      // 电网状态（预留）
        data.set(3, 0);      // 升级状态（预留）
        
        // 初始化能源网络组件
        this.energyBuffer = new EnergyBuffer(1_000_000L); // 1M FE 容量
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
     * 尝试形成多方块结构（Mekanism 风格）
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
     * 破坏多方块结构（连锁破坏所有结构方块）
     */
    public void breakMultiblock() {
        if (level == null || level.isClientSide) {
            return;
        }
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
            
            // 如果是主方块或没有主方块，打开当前 GUI
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
     * 检查多方块是否已成型
     */
    public boolean isMultiblockFormed() {
        return multiblockData != null && multiblockData.isFormed();
    }
    
    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.value = tag.getInt("value");
        this.isController = tag.getBoolean("isController");
        // 读取存储的主方块坐标
        if (tag.contains("ControllerPos", 10)) { // 10 = CompoundTag
            CompoundTag controllerPosTag = tag.getCompound("ControllerPos");
            this.storedControllerPos = new BlockPos(
                controllerPosTag.getInt("X"),
                controllerPosTag.getInt("Y"),
                controllerPosTag.getInt("Z")
            );
        }
        // 重新加载后不立即初始化，而是在下一个 tick 再验证
        // 这样可以确保世界和管理器都已经完全加载
        
        // 加载能源网络数据（重启后恢复）
        if (tag.contains(NETWORK_NBT_KEY, 10)) { // 10 = CompoundTag
            loadNetworkFromNBT(tag.getCompound(NETWORK_NBT_KEY), registries);
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("value", this.value);
        tag.putBoolean("isController", this.isController);
        // 保存存储的主方块坐标
        if (storedControllerPos != null) {
            CompoundTag controllerPosTag = new CompoundTag();
            controllerPosTag.putInt("X", storedControllerPos.getX());
            controllerPosTag.putInt("Y", storedControllerPos.getY());
            controllerPosTag.putInt("Z", storedControllerPos.getZ());
            tag.put("ControllerPos", controllerPosTag);
        }
        
        // 保存能源网络数据（用于重启后恢复）
        saveNetworkToNBT(tag, registries);
    }
    
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        // 确保更新标签包含 isController、ControllerPos 和网络数据
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putBoolean("isController", this.isController);
        if (storedControllerPos != null) {
            CompoundTag controllerPosTag = new CompoundTag();
            controllerPosTag.putInt("X", storedControllerPos.getX());
            controllerPosTag.putInt("Y", storedControllerPos.getY());
            controllerPosTag.putInt("Z", storedControllerPos.getZ());
            tag.put("ControllerPos", controllerPosTag);
        }
        
        // 同步网络数据到客户端
        saveNetworkToNBT(tag, registries);
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
            
            // 处理延迟的重新验证（Mekanism 风格）
            if (blockEntity.multiblockData != null && blockEntity.multiblockData.needsRecheck()) {
                blockEntity.multiblockData.needsRecheck = false; // 重置标志
                // 重新验证结构
                blockEntity.initializeMultiblock();
            }
            
            // 更新 GUI 数据
            blockEntity.updateGuiData();
            
            // 能源网络扫描（每 5 秒一次）
            blockEntity.tryNetworkScan();
        }
    }
    
    /**
     * 当区块加载完成时调用，用于恢复多方块结构
     */
    public void onChunkLoaded() {
        // 不需要特殊处理，tick 方法会自动检测并初始化
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
    
    // ==================== IEnergyNode 实现 ====================
    
    /**
     * 尝试执行网络扫描（每 5 秒一次）
     */
    private void tryNetworkScan() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastScanTime >= SCAN_INTERVAL) {
            performNetworkScan();
            lastScanTime = currentTime;
        }
    }
    
    /**
     * 执行网络扫描
     */
    private void performNetworkScan() {
        if (!isController() || level == null) {
            return;
        }
        
        // 获取或创建网络
        EnergyNetworkManager manager = EnergyNetworkManager.getInstance();
        EnergyNetwork network = manager.getNetworkByPosition(worldPosition);
        
        if (network == null) {
            // 创建新网络
            network = manager.createNetwork(worldPosition);
            network.setProtocolCore(this);
            manager.registerNetwork(network);
        }
        
        // 执行扫描
        if (network.isValid()) {
            network.scanNetwork();
            
            // 更新 GUI 数据
            data.set(0, (int) Math.min(energyBuffer.getEnergy(), Integer.MAX_VALUE));
        }
    }
    
    @Override
    public BlockPos getNodePosition() {
        return worldPosition;
    }
    
    @Override
    public Level getLevel() {
        return level;
    }
    
    @Override
    public EnergyBuffer getEnergyBuffer() {
        return energyBuffer;
    }
    
    @Override
    public void setEnergyBuffer(long energy) {
        energyBuffer.setEnergy(energy);
    }
    
    @Override
    public long getMaxCapacity() {
        return energyBuffer.getMaxCapacity();
    }
    
    @Override
    public long getEnergyDemand() {
        // 协议核心本身不消耗能量
        return 0;
    }
    
    @Override
    public long receiveEnergy(long amount, IEnergyNode from) {
        return energyBuffer.addEnergy(amount);
    }
    
    @Override
    public long extractEnergy(long amount, IEnergyNode to) {
        return energyBuffer.extractEnergy(amount);
    }
    
    @Override
    public void onNetworkScan() {
        // 扫描时填满缓冲区
        energyBuffer.fill();
    }
    
    @Override
    public boolean isValid() {
        return level != null && !level.isClientSide && isController();
    }
    
    @Override
    public NodeType getNodeType() {
        return NodeType.PROTOCOL_CORE;
    }
    
    // ==================== NBT 持久化方法 ====================
    
    /**
     * 保存网络数据到 NBT（分级存储优化）
     */
    private void saveNetworkToNBT(CompoundTag tag, HolderLookup.Provider registries) {
        if (!isController() || level == null) {
            return;
        }
        
        EnergyNetworkManager manager = EnergyNetworkManager.getInstance();
        EnergyNetwork network = manager.getNetworkByPosition(worldPosition);
        
        if (network == null) {
            return;
        }
        
        CompoundTag networkTag = new CompoundTag();
        
        // 只保存一级节点（直接连接到协议核心的节点）
        ListTag directNodesTag = new ListTag();
        IEnergyNode protocolCore = network.getProtocolCore();
        if (protocolCore != null) {
            List<IEnergyNode> directNeighbors = network.getNeighbors(protocolCore);
            for (IEnergyNode node : directNeighbors) {
                CompoundTag nodeTag = new CompoundTag();
                BlockPos pos = node.getNodePosition();
                nodeTag.putInt("X", pos.getX());
                nodeTag.putInt("Y", pos.getY());
                nodeTag.putInt("Z", pos.getZ());
                nodeTag.putString("Type", node.getNodeType().name());
                
                // 保存能量缓冲区数据
                EnergyBuffer buffer = node.getEnergyBuffer();
                nodeTag.putLong("Energy", buffer.getEnergy());
                
                directNodesTag.add(nodeTag);
            }
        }
        networkTag.put(DIRECT_NODES_KEY, directNodesTag);
        
        // 二级节点信息由一级节点自己保存（分布式存储）
        // 这里只保存元数据
        networkTag.putInt("TotalNodes", network.getNodeCount());
        networkTag.putLong("LastScanTime", network.getLastScanTime());
        
        // 保存统计数据
        networkTag.putLong("TotalEnergy", network.getTotalNetworkEnergy());
        networkTag.putLong("TotalCapacity", network.getTotalNetworkCapacity());
        
        tag.put(NETWORK_NBT_KEY, networkTag);
    }
    
    /**
     * 从 NBT 加载网络数据（分级恢复）
     */
    private void loadNetworkFromNBT(CompoundTag networkTag, HolderLookup.Provider registries) {
        if (level == null || level.isClientSide) {
            return;
        }
        
        EnergyNetworkManager manager = EnergyNetworkManager.getInstance();
        EnergyNetwork network = manager.createNetwork(worldPosition);
        
        // 设置协议核心
        network.setProtocolCore(this);
        
        // 加载一级节点（直接连接的节点）
        if (networkTag.contains(DIRECT_NODES_KEY, 9)) { // 9 = ListTag
            ListTag directNodesTag = networkTag.getList(DIRECT_NODES_KEY, 10); // 10 = CompoundTag
            
            for (int i = 0; i < directNodesTag.size(); i++) {
                CompoundTag nodeTag = directNodesTag.getCompound(i);
                BlockPos pos = new BlockPos(
                    nodeTag.getInt("X"),
                    nodeTag.getInt("Y"),
                    nodeTag.getInt("Z")
                );
                
                // 跳过协议核心（已经设置）
                if (pos.equals(worldPosition)) {
                    continue;
                }
                
                // 获取节点类型
                String typeStr = nodeTag.getString("Type");
                IEnergyNode.NodeType nodeType = IEnergyNode.NodeType.valueOf(typeStr);
                
                // 尝试找到该位置的方块实体
                if (level.isLoaded(pos)) {
                    BlockEntity blockEntity = level.getBlockEntity(pos);
                    if (blockEntity instanceof IEnergyNode node) {
                        // 验证节点类型
                        if (node.getNodeType() == nodeType && node.isValid()) {
                            network.addNode(node);
                            network.connectNodes(this, node);
                            network.connectNodes(node, this);
                            
                            // 恢复能量缓冲区
                            EnergyBuffer buffer = node.getEnergyBuffer();
                            long savedEnergy = nodeTag.getLong("Energy");
                            buffer.setEnergy(savedEnergy);
                        }
                    }
                }
            }
        }
        
        // 二级节点不需要在这里加载
        // 它们会在网络扫描时通过一级节点自动发现和重建
        
        // 恢复统计数据
        if (networkTag.contains("TotalEnergy")) {
            // 暂时不恢复，会在下次扫描时重新计算
        }
        
        // 注册网络
        manager.registerNetwork(network);
    }
}
