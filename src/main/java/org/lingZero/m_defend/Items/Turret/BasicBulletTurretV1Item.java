package org.lingZero.m_defend.Items.Turret;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.lingZero.m_defend.Config;
import org.lingZero.m_defend.Register.ModBlocks;

import java.util.List;

/**
 * 一级实弹炮塔物品
 */
public class BasicBulletTurretV1Item extends BaseTurretItem {
    
    public BasicBulletTurretV1Item(Properties properties) {
        super(properties);
    }
    
    @Override
    protected Block getTurretBlock() {
        return ModBlocks.BASIC_BULLET_TURRET_V1_BLOCK.get();
    }
    
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, TooltipFlag flag) {
        // 显示炮塔基础属性（直接从 Config 读取）
        var stats = Config.TURRET.basicBulletV1;
        tooltip.add(Component.translatable("tooltip.modularization_defend.turret.base_fire_rate", stats.fireRate.get()));
        tooltip.add(Component.translatable("tooltip.modularization_defend.turret.base_damage", stats.damage.get()));
        tooltip.add(Component.translatable("tooltip.modularization_defend.turret.base_range", stats.range.get()));
        tooltip.add(Component.translatable("tooltip.modularization_defend.turret.energy_capacity", stats.energyCapacity.get()));
    }
}
