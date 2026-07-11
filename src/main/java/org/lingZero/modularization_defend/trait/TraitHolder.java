package org.lingZero.modularization_defend.trait;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * 词条持有者——附着在 LivingEntity 上的 Attachment 数据对象。
 * 内部维护词条 ID → 等级的映射，以及已应用的属性修饰器 ID 列表。
 * <p>
 * 属性修饰器的 ID 集中由此类管理，使用确定性 ResourceLocation 确保无重复。
 */
public class TraitHolder {

    /** 词条 ID → 等级 */
    private final Map<ResourceLocation, Integer> traits = new LinkedHashMap<>();

    /** traitId → 已应用的修饰器条目列表 */
    private final Map<ResourceLocation, List<ModifierEntry>> modifierEntries = new HashMap<>();

    /** 修饰器条目：记录修饰器ID和对应的属性，方便移除时定位 */
    private record ModifierEntry(ResourceLocation modifierId, Holder<Attribute> attribute) {}

    // ==================== 词条增删 ====================

    /**
     * 为实体添加词条。
     *
     * @param entity  目标实体
     * @param traitId 词条注册名
     * @param trait   词条实例
     * @param level   词条等级（会被夹紧到 [1, trait.getMaxLevel()]）
     */
    public void addTrait(LivingEntity entity, ResourceLocation traitId, Trait trait, int level) {
        int clamped = Math.max(1, Math.min(level, trait.getMaxLevel()));
        traits.put(traitId, clamped);
        applyModifiers(entity, traitId, trait, clamped);
        trait.onInit(entity, clamped);
    }

    /**
     * 从实体移除单个词条并清理关联的属性修饰器。
     */
    public void removeTrait(LivingEntity entity, ResourceLocation traitId, @Nullable Trait trait) {
        traits.remove(traitId);
        removeModifiers(entity, traitId);
        if (trait != null) {
            trait.onRemove(entity);
        }
    }

    /**
     * 清除所有词条并移除属性修饰器。
     * 调用者需要提供 Registry<Trait> 以触发各词条的 onRemove 钩子。
     */
    public void clear(LivingEntity entity, Registry<Trait> registry) {
        for (var entry : new ArrayList<>(traits.entrySet())) {
            Trait trait = registry.get(entry.getKey());
            if (trait != null) {
                trait.onRemove(entity);
            }
            removeModifiers(entity, entry.getKey());
        }
        traits.clear();
    }

    // ==================== 查询 ====================

    /** 获取词条等级，无此词条返回 0 */
    public int getLevel(ResourceLocation traitId) {
        return traits.getOrDefault(traitId, 0);
    }

    /** 是否拥有该词条 */
    public boolean hasTrait(ResourceLocation traitId) {
        return traits.containsKey(traitId);
    }

    /** 获取所有词条（不可变视图） */
    public Set<Map.Entry<ResourceLocation, Integer>> getAllTraits() {
        return Collections.unmodifiableSet(traits.entrySet());
    }

    /** 词条总数 */
    public int traitCount() {
        return traits.size();
    }

    // ==================== 属性修饰器内部管理 ====================

    private void applyModifiers(LivingEntity entity, ResourceLocation traitId, Trait trait, int level) {
        List<Trait.AttributeModifierConfig> configs = trait.getAttributeModifiers(level);
        if (configs.isEmpty()) return;

        List<ModifierEntry> entries = new ArrayList<>();
        for (var config : configs) {
            double value = config.perLevelValue() * level;

            // 获取属性注册名作为修饰器ID的一部分
            ResourceLocation attrKey = config.attribute().unwrapKey()
                    .map(ResourceKey::location)
                    .orElse(ResourceLocation.fromNamespaceAndPath("unknown", "attr"));

            // 用 "trait/<traitId路径>/<属性路径>" 生成确定性修饰器ID
            ResourceLocation modifierId = ResourceLocation.fromNamespaceAndPath(
                    traitId.getNamespace(),
                    "trait/" + traitId.getPath() + "/" + attrKey.getPath()
            );

            AttributeModifier modifier = new AttributeModifier(
                    modifierId, value, config.operation()
            );

            @Nullable AttributeInstance attrInst = entity.getAttribute(config.attribute());
            if (attrInst != null) {
                // 先移除旧修饰器再添加新的（处理等级更新场景）
                attrInst.removeModifier(modifierId);
                attrInst.addPermanentModifier(modifier);
                entries.add(new ModifierEntry(modifierId, config.attribute()));
            }
        }
        modifierEntries.put(traitId, entries);
    }

    private void removeModifiers(LivingEntity entity, ResourceLocation traitId) {
        List<ModifierEntry> entries = modifierEntries.remove(traitId);
        if (entries == null) return;

        for (var entry : entries) {
            @Nullable AttributeInstance attrInst = entity.getAttribute(entry.attribute());
            if (attrInst != null) {
                attrInst.removeModifier(entry.modifierId());
            }
        }
    }
}
