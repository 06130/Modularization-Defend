package org.lingZero.m_defend.entity.EntityTrace;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

/**
 * 实体过滤器接口
 * 用于定义实体的筛选条件
 */
@FunctionalInterface
public interface EntityFilter {
    /**
     * 判断实体是否符合过滤条件
     * 
     * @param entity 待检测的实体
     * @return true 如果实体符合条件，false 否则
     */
    boolean test(@NotNull Entity entity);
}
