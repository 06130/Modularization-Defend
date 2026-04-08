package org.lingZero.m_defend.entity.EntityTrace;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * 实体查找结果封装类
 * 用于返回查找到的实体信息
 */
public class EntitySearchResult {
    
    private final List<Entity> entities;
    private final Entity nearestEntity;
    private final int totalCount;
    
    /**
     * 构造函数 - 返回单个最近实体
     * 
     * @param nearestEntity 最近的实体，可能为 null
     */
    public EntitySearchResult(@Nullable Entity nearestEntity) {
        this.nearestEntity = nearestEntity;
        this.entities = nearestEntity != null ? List.of(nearestEntity) : Collections.emptyList();
        this.totalCount = nearestEntity != null ? 1 : 0;
    }
    
    /**
     * 构造函数 - 返回实体列表
     * 
     * @param entities 符合条件的实体列表
     */
    public EntitySearchResult(@NotNull List<Entity> entities) {
        this.entities = Collections.unmodifiableList(entities);
        this.totalCount = entities.size();
        // 如果列表不为空，第一个元素即为最近的实体（已排序）
        this.nearestEntity = entities.isEmpty() ? null : entities.get(0);
    }
    
    /**
     * 获取最近的实体
     * 
     * @return 最近的实体，如果没有找到则返回 null
     */
    @Nullable
    public Entity getNearestEntity() {
        return nearestEntity;
    }
    
    /**
     * 获取所有符合条件的实体列表
     * 列表已按距离从近到远排序
     * 
     * @return 实体列表，只读
     */
    @NotNull
    public List<Entity> getAllEntities() {
        return entities;
    }
    
    /**
     * 获取符合条件的实体总数
     * 
     * @return 实体数量
     */
    public int getTotalCount() {
        return totalCount;
    }
    
    /**
     * 判断是否找到了实体
     * 
     * @return true 如果找到了至少一个实体
     */
    public boolean hasEntities() {
        return totalCount > 0;
    }
    
    /**
     * 判断是否未找到任何实体
     * 
     * @return true 如果没有找到任何实体
     */
    public boolean isEmpty() {
        return totalCount == 0;
    }
}
