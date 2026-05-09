package org.lingZero.m_defend.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;
import org.lingZero.m_defend.Event.TriggerArrowHitBlockEvent;
import org.lingZero.m_defend.Event.TriggerArrowHitEntityEvent;

/**
 * 触发箭矢 —— 可被原版弓发射，击中实体/方块时触发回调
 */
public class TriggerArrow extends AbstractArrow {

    public TriggerArrow(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
    }

    /** 工厂方法，设置 owner 和 pickupItemStack */
    public static TriggerArrow create(EntityType<? extends AbstractArrow> entityType, Level level,
                                      LivingEntity shooter, ItemStack pickupItem) {
        TriggerArrow arrow = new TriggerArrow(entityType, level);
        arrow.setOwner(shooter);
        arrow.setPickupItemStack(pickupItem);
        return arrow;
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult result) {
        super.onHitEntity(result);
        onTriggerEntity(result.getEntity());
    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult result) {
        super.onHitBlock(result);
        onTriggerBlock(result.getBlockPos());
    }

    /** 击中实体触发器，向 NeoForge 事件总线投递 {@link TriggerArrowHitEntityEvent} */
    protected void onTriggerEntity(Entity target) {
        NeoForge.EVENT_BUS.post(new TriggerArrowHitEntityEvent(this, target));
    }

    /** 击中方块触发器，向 NeoForge 事件总线投递 {@link TriggerArrowHitBlockEvent} */
    protected void onTriggerBlock(BlockPos pos) {
        NeoForge.EVENT_BUS.post(new TriggerArrowHitBlockEvent(this, pos));
    }

    @Override
    protected @NotNull ItemStack getDefaultPickupItem() {
        return ItemStack.EMPTY;
    }
}