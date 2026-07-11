package org.lingZero.modularization_defend.trait;

import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.List;

/**
 * 词条类型抽象基类。
 * 每个注册的 Trait 子类代表一种词条类型（如"力量"、"速度"），
 * 实体可以携带某种词条并拥有对应的等级。
 * <p>
 * 子类覆盖对应钩子方法即可实现自定义行为，所有钩子默认空实现。
 * 创建简单属性词条请使用 {@link TraitBuilder}。
 */
public abstract class Trait {
    /** 词条最大等级上限 */
    protected int maxLevel = 5;
    /** 词条显示颜色（RGB） */
    protected int color = 0xFFFFFF;
    /** 词条描述翻译键 */
    protected String descriptionKey;

    // ==================== 生命周期钩子 ====================

    /** 词条首次添加到实体时调用，或实体加载时触发 */
    public void onInit(LivingEntity entity, int level) {}

    /** 每个服务端 tick 调用 */
    public void onTick(LivingEntity entity, int level) {}

    /** 持有本词条的实体攻击目标时（伤害已造成后）调用 */
    public void onHurtTarget(LivingEntity attacker, LivingEntity target, int level) {}

    /** 持有本词条的实体被攻击时（计算伤害前）调用 */
    public void onAttacked(LivingEntity entity, DamageSource source, float amount, int level) {}

    /** 持有本词条的实体死亡时调用 */
    public void onDeath(LivingEntity entity, DamageSource source, int level) {}

    /** 词条从实体移除时调用（清理属性修饰器、效果等） */
    public void onRemove(LivingEntity entity) {}

    // ==================== 属性声明 ====================

    /**
     * 返回当前词条在指定等级下应施加的属性修饰器配置列表。
     * TraitHolder 负责读取此列表并管理实际的 AttributeModifier 生命周期。
     * 默认返回空列表。
     */
    public List<AttributeModifierConfig> getAttributeModifiers(int level) {
        return List.of();
    }

    // ==================== 访问器 ====================

    public int getMaxLevel() {
        return maxLevel;
    }

    public int getColor() {
        return color;
    }

    public String getDescriptionKey() {
        return descriptionKey;
    }

    // ==================== 内部记录类 ====================

    /**
     * 属性修饰器配置——Trait 只需声明"要什么属性"，
     * TraitHolder 负责 UUID 生成和实际应用/移除。
     */
    public record AttributeModifierConfig(
            Holder<Attribute> attribute,
            double perLevelValue,
            AttributeModifier.Operation operation
    ) {}
}
