package org.lingZero.modularization_defend.trait;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 词条快速构建器——用几行代码创建属性修改类词条。
 *
 * <pre>{@code
 * TraitBuilder.create()
 *     .attribute(Attributes.ATTACK_DAMAGE, Operation.ADD_VALUE, 2.0)
 *     .attribute(Attributes.MAX_HEALTH, Operation.ADD_VALUE, 4.0)
 *     .maxLevel(10)
 *     .color(0xFF5555)
 *     .descriptionKey("trait.modularization_defend.powerful")
 *     .build();
 * }</pre>
 *
 * 对于更复杂的词条（需要自定义 onHurtTarget 等），请直接继承 {@link Trait}。
 */
public class TraitBuilder {
    private final List<Trait.AttributeModifierConfig> attributes = new ArrayList<>();
    private int maxLevel = 5;
    private int color = 0xFFFFFF;
    private String descriptionKey;
    private BiConsumer<LivingEntity, Integer> onTickCallback;
    private Consumer<LivingEntity> onRemoveCallback;

    private TraitBuilder() {}

    public static TraitBuilder create() {
        return new TraitBuilder();
    }

    /** 添加属性修饰器，perLevelValue 会被乘以词条等级作为最终值 */
    public TraitBuilder attribute(Holder<Attribute> attr, AttributeModifier.Operation op, double perLevelValue) {
        attributes.add(new Trait.AttributeModifierConfig(attr, perLevelValue, op));
        return this;
    }

    public TraitBuilder maxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
        return this;
    }

    public TraitBuilder color(int color) {
        this.color = color;
        return this;
    }

    public TraitBuilder descriptionKey(String key) {
        this.descriptionKey = key;
        return this;
    }

    /** 自定义 tick 回调（属性修改之外的额外行为） */
    public TraitBuilder onTick(BiConsumer<LivingEntity, Integer> callback) {
        this.onTickCallback = callback;
        return this;
    }

    /** 自定义移除回调（额外清理逻辑） */
    public TraitBuilder onRemove(Consumer<LivingEntity> callback) {
        this.onRemoveCallback = callback;
        return this;
    }

    /** 构建匿名 Trait 子类实例 */
    public Trait build() {
        final List<Trait.AttributeModifierConfig> attrs = List.copyOf(attributes);
        final BiConsumer<LivingEntity, Integer> tick = onTickCallback;
        final Consumer<LivingEntity> remove = onRemoveCallback;

        return new Trait() {
            {
                this.maxLevel = TraitBuilder.this.maxLevel;
                this.color = TraitBuilder.this.color;
                this.descriptionKey = TraitBuilder.this.descriptionKey;
            }

            @Override
            public List<AttributeModifierConfig> getAttributeModifiers(int level) {
                return attrs;
            }

            @Override
            public void onTick(LivingEntity entity, int level) {
                if (tick != null) tick.accept(entity, level);
            }

            @Override
            public void onRemove(LivingEntity entity) {
                if (remove != null) remove.accept(entity);
            }
        };
    }
}
