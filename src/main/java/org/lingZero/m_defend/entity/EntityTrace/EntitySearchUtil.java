package org.lingZero.m_defend.entity.EntityTrace;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * 实体查找工具类
 * 提供高效的实体搜索功能，支持多种过滤条件和返回模式
 *<p>
 * 性能优化特性：
 * 1. 使用距离平方比较避免开方运算
 * 2. 使用 float 替代 double 减少运算和内存开销
 * 3. 使用 AABB 预筛选减少检测实体数量
 * 4. 查找最近实体时支持限制候选数量以提升性能
 * 5. 支持批量查询和单次查询
 */
public class EntitySearchUtil {

    /**
     * 查找最近的符合条件的实体
     */
    @NotNull
    public static EntitySearchResult findNearestEntity(
            @NotNull Level level,
            @NotNull BlockPos centerPos,
            double radius,
            double height,
            @NotNull EntityFilter filter) {
        return findNearestInternal(level, centerPos, radius, height, filter, Integer.MAX_VALUE);
    }

    /**
     * 查找最近的符合条件的实体（快速模式）
     * 只检查前 10 个符合条件的实体，适合高频率调用场景
     */
    @NotNull
    public static EntitySearchResult findNearestEntityFast(
            @NotNull Level level,
            @NotNull BlockPos centerPos,
            double radius,
            double height,
            @NotNull EntityFilter filter) {
        return findNearestInternal(level, centerPos, radius, height, filter, 10);
    }

    /**
     * 统一的最近实体搜索实现
     *
     * @param maxCheckCount 最大检查候选数，超过后提前退出
     */
    @NotNull
    private static EntitySearchResult findNearestInternal(
            @NotNull Level level,
            @NotNull BlockPos centerPos,
            double radius,
            double height,
            @NotNull EntityFilter filter,
            int maxCheckCount) {

        AABB searchBox = createSearchBox(centerPos, radius, height);
        List<Entity> allEntities = level.getEntities(null, searchBox);

        if (allEntities.isEmpty()) {
            return EntitySearchResult.EMPTY;
        }

        float centerX = (float) centerPos.getX() + 0.5f;
        float centerY = (float) centerPos.getY() + 0.5f;
        float centerZ = (float) centerPos.getZ() + 0.5f;

        Entity nearestEntity = null;
        float nearestDistanceSq = Float.MAX_VALUE;
        int checkedCount = 0;

        for (Entity entity : allEntities) {
            if (!filter.test(entity)) {
                continue;
            }

            float distanceSq = distanceSq(entity, centerX, centerY, centerZ);
            if (distanceSq < nearestDistanceSq) {
                nearestDistanceSq = distanceSq;
                nearestEntity = entity;
            }

            checkedCount++;
            if (checkedCount >= maxCheckCount) {
                break;
            }
        }

        return new EntitySearchResult(nearestEntity);
    }

    /**
     * 查找所有符合条件的实体（按距离从近到远排序）
     */
    @NotNull
    public static EntitySearchResult findAllEntities(
            @NotNull Level level,
            @NotNull BlockPos centerPos,
            double radius,
            double height,
            @NotNull EntityFilter filter) {

        AABB searchBox = createSearchBox(centerPos, radius, height);
        List<Entity> allEntities = level.getEntities(null, searchBox);

        if (allEntities.isEmpty()) {
            return EntitySearchResult.EMPTY_LIST;
        }

        float centerX = (float) centerPos.getX() + 0.5f;
        float centerY = (float) centerPos.getY() + 0.5f;
        float centerZ = (float) centerPos.getZ() + 0.5f;

        List<Entity> result = new ArrayList<>();
        for (Entity entity : allEntities) {
            if (filter.test(entity)) {
                result.add(entity);
            }
        }

        if (result.isEmpty()) {
            return EntitySearchResult.EMPTY_LIST;
        }

        // 按距离排序 — distanceSq 是纯 float 运算，重算比缓存更省内存
        result.sort(Comparator.comparingDouble(e -> distanceSq(e, centerX, centerY, centerZ)));

        return new EntitySearchResult(result);
    }

    /**
     * 查找最近的符合条件的实体（使用自定义谓词）
     */
    @NotNull
    public static EntitySearchResult findNearestEntityByPredicate(
            @NotNull Level level,
            @NotNull BlockPos centerPos,
            double radius,
            double height,
            @NotNull Predicate<Entity> predicate) {
        return findNearestEntity(level, centerPos, radius, height, predicate::test);
    }

    /**
     * 查找所有符合条件的实体（使用自定义谓词）
     */
    @NotNull
    public static EntitySearchResult findAllEntitiesByPredicate(
            @NotNull Level level,
            @NotNull BlockPos centerPos,
            double radius,
            double height,
            @NotNull Predicate<Entity> predicate) {
        return findAllEntities(level, centerPos, radius, height, predicate::test);
    }

    // ==================== 内部工具方法 ====================

    @NotNull
    private static AABB createSearchBox(@NotNull BlockPos centerPos, double radius, double height) {
        return new AABB(
                centerPos.getX() - radius,
                centerPos.getY() - height,
                centerPos.getZ() - radius,
                centerPos.getX() + radius + 1,
                centerPos.getY() + height + 1,
                centerPos.getZ() + radius + 1
        );
    }

    /**
     * 计算实体到中心点的距离平方（float，避免 double 运算和开方）
     */
    private static float distanceSq(@NotNull Entity entity, float cx, float cy, float cz) {
        float dx = (float) entity.getX() - cx;
        float dy = (float) entity.getY() - cy;
        float dz = (float) entity.getZ() - cz;
        return dx * dx + dy * dy + dz * dz;
    }
}