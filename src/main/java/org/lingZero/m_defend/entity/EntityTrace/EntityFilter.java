package org.lingZero.m_defend.entity.EntityTrace;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

/**
 * 实体过滤器接口
 */
@FunctionalInterface
public interface EntityFilter {
    boolean test(@NotNull Entity entity);
}
