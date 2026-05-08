package org.lingZero.m_defend.entity.EntityTrace;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 预定义的实体过滤器工厂类
 * 提供常用的实体过滤器创建方法
 */
public class EntityFilters {

    // ==================== 缓存的静态过滤器常量 ====================
    // 避免热路径上每次调用都创建新 lambda（每座炮塔每2秒触发一次搜索）

    public static final EntityFilter PLAYERS = entity -> entity instanceof Player;

    public static final EntityFilter HOSTILE_MOBS = entity ->
            entity.getType().getCategory() == MobCategory.MONSTER;

    public static final EntityFilter FRIENDLY_MOBS = entity ->
            entity.getType().getCategory() == MobCategory.CREATURE;

    public static final EntityFilter NEUTRAL_MOBS = entity -> {
        MobCategory category = entity.getType().getCategory();
        return category == MobCategory.AMBIENT
                || category == MobCategory.WATER_AMBIENT
                || category == MobCategory.UNDERGROUND_WATER_CREATURE
                || category == MobCategory.WATER_CREATURE;
    };

    // ==================== 工厂方法（返回缓存的常量） ====================

    public static EntityFilter players() {
        return PLAYERS;
    }

    public static EntityFilter hostileMobs() {
        return HOSTILE_MOBS;
    }

    public static EntityFilter friendlyMobs() {
        return FRIENDLY_MOBS;
    }

    public static EntityFilter neutralMobs() {
        return NEUTRAL_MOBS;
    }

    // ==================== 需要参数捕获的动态过滤器 ====================

    public static EntityFilter byTag(@NotNull TagKey<EntityType<?>> tagKey) {
        return entity -> entity.getType().is(tagKey);
    }

    public static EntityFilter byEntityId(@NotNull String entityId) {
        ResourceLocation location = ResourceLocation.parse(entityId);  // 只解析一次
        return entity -> BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()) == location;
    }

    public static EntityFilter byEntityIds(@NotNull Set<String> entityIds) {
        Set<ResourceLocation> locations = entityIds.stream()
                .map(ResourceLocation::parse)
                .collect(Collectors.toSet());
        return entity -> locations.contains(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()));
    }

    public static EntityFilter byEntityType(@NotNull EntityType<?> entityType) {
        return entity -> entity.getType() == entityType;
    }

    // ==================== 逻辑组合器 ====================

    public static EntityFilter and(@NotNull EntityFilter... filters) {
        return entity -> {
            for (EntityFilter filter : filters) {
                if (!filter.test(entity)) {
                    return false;
                }
            }
            return true;
        };
    }

    public static EntityFilter or(@NotNull EntityFilter... filters) {
        return entity -> {
            for (EntityFilter filter : filters) {
                if (filter.test(entity)) {
                    return true;
                }
            }
            return false;
        };
    }

    public static EntityFilter not(@NotNull EntityFilter filter) {
        return entity -> !filter.test(entity);
    }
}