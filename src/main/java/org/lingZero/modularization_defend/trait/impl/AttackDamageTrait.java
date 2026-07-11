package org.lingZero.modularization_defend.trait.impl;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.lingZero.modularization_defend.trait.Trait;

import java.util.List;

/**
 * 攻击伤害词条——每级提升 2 点攻击伤害。
 * 展示完整的 Trait 子类化模式：覆写属性声明 + 自定义 hit 行为。
 */
public class AttackDamageTrait extends Trait {

    public AttackDamageTrait() {
        this.maxLevel = 10;
        this.color = 0xFF5555;
        this.descriptionKey = "trait.modularization_defend.attack_damage";
    }

    @Override
    public List<AttributeModifierConfig> getAttributeModifiers(int level) {
        return List.of(new AttributeModifierConfig(
                Attributes.ATTACK_DAMAGE, 2.0, AttributeModifier.Operation.ADD_VALUE));
    }

    @Override
    public void onHurtTarget(LivingEntity attacker, LivingEntity target, int level) {
        // 暴击粒子效果
        if (attacker.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.CRIT,
                    target.getX(), target.getEyeY(), target.getZ(),
                    10, 0.3, 0.3, 0.3, 0.0);
        }
    }
}
