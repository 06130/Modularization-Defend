package org.lingZero.m_defend.Items;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.lingZero.m_defend.Register.ModEntities;
import org.lingZero.m_defend.entity.TriggerArrow;

/**
 * 触发箭矢 —— 可放入原版弓的弹药槽发射
 */
public class TriggerArrowItem extends ArrowItem {

    public TriggerArrowItem(Properties properties) {
        super(properties);
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack ammo, LivingEntity shooter, @Nullable ItemStack weapon) {
        return TriggerArrow.create(ModEntities.TRIGGER_ARROW.get(), level, shooter, ammo.copyWithCount(1));
    }
}