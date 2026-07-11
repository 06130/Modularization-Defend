package org.lingZero.modularization_defend.Block.example;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.lingZero.modularization_defend.Block.ModBlockEntities;
import org.lingZero.modularization_defend.Block.bounding.IBoundingBlock;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 蓝门多方块结构的BlockEntity。
 * 实现GeoBlockEntity以提供GeckoLib动画视觉效果，灵感来源于明日方舟"蓝门"，
 * 同时保留原有的点击计数和无碰撞特性。
 */
public class BlueDoorBlockEntity extends BlockEntity implements IBoundingBlock, GeoBlockEntity {

    /** GeckoLib动画实例缓存（客户端侧自动管理） */
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private int clickCount;

    public BlueDoorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BLUE_DOOR.get(), pos, state);
    }

    // ==================== GeckoLib动画控制器 ====================

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(
                this, "idleController", 0,
                state -> {
                    state.getController().setAnimation(
                            RawAnimation.begin().thenLoop("animation.bluedoor.idle"));
                    return PlayState.CONTINUE;
                }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // ==================== 服务端Tick ====================

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlueDoorBlockEntity be) {
    }

    // ==================== 点击计数（保留原有功能） ====================

    public int getClickCount() {
        return clickCount;
    }

    public void incrementClickCount() {
        clickCount++;
        setChanged();
    }

    // ==================== NBT持久化 ====================

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("ClickCount", clickCount);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        clickCount = tag.getInt("ClickCount");
    }
}
