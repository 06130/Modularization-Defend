package org.lingZero.modularization_defend.Blocks.AgreementCore;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.lingZero.modularization_defend.Blocks.Multiblock.newMultiblock.AbstractMultiblock;
import org.lingZero.modularization_defend.Blocks.Multiblock.newMultiblock.AbstractMultiblockBlockEntity;
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
 * 使用新的 Multiblock 框架实现
 */
public class AgreementCoreBlockEntity extends AbstractMultiblockBlockEntity implements GeoBlockEntity {
    private int value;
    
    // GUI 数据
    private final ContainerData data = new SimpleContainerData(4);
    
    // GeckoLib 动画缓存
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public AgreementCoreBlockEntity(BlockPos pos, BlockState state) {
        super(AGREEMENT_CORE_BLOCK_ENTITY.get(), pos, state);
        // 初始化 GUI 数据
        data.set(0, 0);      // 当前能量
        data.set(1, 10000);  // 最大能量
        data.set(2, 0);      // 电网状态（预留）
        data.set(3, 0);      // 升级状态（预留）
    }
    
    @Override
    @NotNull
    protected AbstractMultiblock getMultiblock() {
        return AgreementCoreMultiblockDef.getInstance();
    }
    
    @Override
    @NotNull
    protected MenuProvider createMenuProvider(@NotNull BlockPos pos) {
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
     * 是否形成多方块
     */
    @Override
    public boolean isMultiblockFormed() {
        // 直接返回字段值
        return multiblockFormed;
    }
    
    @Override
    public void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.value = tag.getInt("value");
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("value", this.value);
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
        // 每 tick 执行一次
        if (!level.isClientSide) {
            // 如果是主方块且没有成型，则尝试初始化多方块
            if (blockEntity.isController() && !blockEntity.isMultiblockFormed()) {
                blockEntity.initializeMultiblock();
            }
        }
    }

    // ==================== GeoBlockEntity 接口 =====================
    
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // 注册空控制器
        controllers.add(new AnimationController<>(this, this::idleController));
    }
    
    protected <E extends AgreementCoreBlockEntity> PlayState idleController(final AnimationState<E> state) {
        // 停止动画
        return PlayState.STOP;
    }
    
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
