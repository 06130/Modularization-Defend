package org.lingZero.m_defend.entity.EntityTrace;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * 预定义的实体过滤器工厂类
 * 提供常用的实体过滤器创建方法
 */
public class EntityFilters {
    
    /**
     * 创建玩家过滤器
     * 
     * @return 只匹配玩家的过滤器
     */
    public static EntityFilter players() {
        return entity -> entity instanceof Player;
    }
    
    /**
     * 创建敌对生物过滤器
     * 包括僵尸、骷髅、苦力怕等攻击性生物
     * 
     * @return 只匹配敌对生物的过滤器
     */
    public static EntityFilter hostileMobs() {
        return entity -> entity.getType().getCategory() == MobCategory.MONSTER;
    }
    
    /**
     * 创建友好生物过滤器
     * 包括牛、羊、猪等被动生物
     * 
     * @return 只匹配友好生物的过滤器
     */
    public static EntityFilter friendlyMobs() {
        return entity -> entity.getType().getCategory() == MobCategory.CREATURE;
    }
    
    /**
     * 创建中立生物过滤器
     * 包括狼、蜜蜂等在特定情况下会攻击的生物
     * 
     * @return 只匹配中立生物的过滤器
     */
    public static EntityFilter neutralMobs() {
        return entity -> entity.getType().getCategory() == MobCategory.AMBIENT ||
                        entity.getType().getCategory() == MobCategory.WATER_AMBIENT ||
                        entity.getType().getCategory() == MobCategory.UNDERGROUND_WATER_CREATURE ||
                        entity.getType().getCategory() == MobCategory.WATER_CREATURE;
    }
    
    /**
     * 创建基于实体标签的过滤器
     * 
     * @param tagKey 实体标签
     * @return 匹配指定标签的过滤器
     */
    public static EntityFilter byTag(@NotNull TagKey<EntityType<?>> tagKey) {
        return entity -> entity.getType().is(tagKey);
    }
    
    /**
     * 创建基于实体ID的过滤器
     * 
     * @param entityId 实体ID字符串，格式为 "namespace:path"
     * @return 匹配指定实体ID的过滤器
     */
    public static EntityFilter byEntityId(@NotNull String entityId) {
        ResourceLocation location = ResourceLocation.parse(entityId);
        return entity -> BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).equals(location);
    }
    
    /**
     * 创建基于多个实体ID的过滤器
     * 
     * @param entityIds 实体ID集合
     * @return 匹配任一指定实体ID的过滤器
     */
    public static EntityFilter byEntityIds(@NotNull Set<String> entityIds) {
        Set<ResourceLocation> locations = entityIds.stream()
                .map(ResourceLocation::parse)
                .collect(java.util.stream.Collectors.toSet());
        return entity -> locations.contains(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()));
    }
    
    /**
     * 创建基于实体类型的过滤器
     * 
     * @param entityType 实体类型
     * @return 匹配指定实体类型的过滤器
     */
    public static EntityFilter byEntityType(@NotNull EntityType<?> entityType) {
        return entity -> entity.getType() == entityType;
    }
    
    /**
     * 组合多个过滤器（逻辑与）
     * 所有过滤器都必须通过
     * 
     * @param filters 过滤器数组
     * @return 组合后的过滤器
     */
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
    
    /**
     * 组合多个过滤器（逻辑或）
     * 任一过滤器通过即可
     * 
     * @param filters 过滤器数组
     * @return 组合后的过滤器
     */
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
    
    /**
     * 取反过滤器
     * 
     * @param filter 原过滤器
     * @return 取反后的过滤器
     */
    public static EntityFilter not(@NotNull EntityFilter filter) {
        return entity -> !filter.test(entity);
    }
}
