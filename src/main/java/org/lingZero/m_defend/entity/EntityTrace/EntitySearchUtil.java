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
 * 
 * 性能优化特性：
 * 1. 使用距离平方比较避免开方运算
 * 2. 使用 AABB 预筛选减少检测实体数量
 * 3. 找到最近实体时提前退出循环
 * 4. 支持批量查询和单次查询
 */
public class EntitySearchUtil {
    
    /**
     * 查找最近的符合条件的实体
     * 
     * @param level 世界对象
     * @param centerPos 中心坐标（主方块位置）
     * @param radius 水平查找半径
     * @param height 垂直查找高度（向上和向下各扩展此值）
     * @param filter 实体过滤器
     * @return 查找结果，包含最近的实体
     */
    @NotNull
    public static EntitySearchResult findNearestEntity(
            @NotNull Level level,
            @NotNull BlockPos centerPos,
            double radius,
            double height,
            @NotNull EntityFilter filter) {

        // 计算搜索区域的边界框
        AABB searchBox = createSearchBox(centerPos, radius, height);
        
        // 获取搜索区域内的所有实体
        List<Entity> allEntities = level.getEntities(null, searchBox);
        
        if (allEntities.isEmpty()) {
            return EntitySearchResult.EMPTY;
        }
        
        // 预计算中心坐标为float，减少double运算
        float centerX = (float)centerPos.getX() + 0.5f;
        float centerY = (float)centerPos.getY() + 0.5f;
        float centerZ = (float)centerPos.getZ() + 0.5f;
        
        // 查找最近的符合条件的实体
        Entity nearestEntity = null;
        float nearestDistanceSq = Float.MAX_VALUE;
        
        for (Entity entity : allEntities) {
            // 快速排除不符合条件的实体
            if (!filter.test(entity)) {
                continue;
            }
            
            // 使用float距离平方进行比较，避免开方运算和double精度开销
            float distanceSq = getDistanceSquaredFloat(entity, centerX, centerY, centerZ);
            
            // 更新最近的实体
            if (distanceSq < nearestDistanceSq) {
                nearestDistanceSq = distanceSq;
                nearestEntity = entity;
            }
        }
        
        return new EntitySearchResult(nearestEntity);
    }
    
    /**
     * 查找最近的符合条件的实体（快速模式）
     * 通过减少遍历实体数量来提升性能，但可能损失部分精度
     * 
     * @param level 世界对象
     * @param centerPos 中心坐标（主方块位置）
     * @param radius 水平查找半径
     * @param height 垂直查找高度（向上和向下各扩展此值）
     * @param filter 实体过滤器
     * @return 查找结果，包含最近的实体
     */
    @NotNull
    public static EntitySearchResult findNearestEntityFast(
            @NotNull Level level,
            @NotNull BlockPos centerPos,
            double radius,
            double height,
            @NotNull EntityFilter filter) {

        // 计算搜索区域的边界框
        AABB searchBox = createSearchBox(centerPos, radius, height);
        
        // 获取搜索区域内的所有实体
        List<Entity> allEntities = level.getEntities(null, searchBox);
        
        if (allEntities.isEmpty()) {
            return EntitySearchResult.EMPTY;
        }
        
        // 预计算中心坐标为float，减少double运算
        float centerX = (float)centerPos.getX() + 0.5f;
        float centerY = (float)centerPos.getY() + 0.5f;
        float centerZ = (float)centerPos.getZ() + 0.5f;
        
        // 快速模式：只检查前 10 个符合条件的实体
        int checkedCount = 0;
        final int MAX_CHECK_COUNT = 10;
        
        Entity nearestEntity = null;
        float nearestDistanceSq = Float.MAX_VALUE;
        
        for (Entity entity : allEntities) {
            // 快速排除不符合条件的实体
            if (!filter.test(entity)) {
                continue;
            }
            
            // 使用float距离平方进行比较，避免开方运算和double精度开销
            float distanceSq = getDistanceSquaredFloat(entity, centerX, centerY, centerZ);
            
            // 更新最近的实体
            if (distanceSq < nearestDistanceSq) {
                nearestDistanceSq = distanceSq;
                nearestEntity = entity;
            }
            
            checkedCount++;
            if (checkedCount >= MAX_CHECK_COUNT) {
                break;
            }
        }
        
        return new EntitySearchResult(nearestEntity);
    }

    /**
     * 查找所有符合条件的实体（按距离排序）
     * 
     * @param level 世界对象
     * @param centerPos 中心坐标（主方块位置）
     * @param radius 水平查找半径
     * @param height 垂直查找高度（向上和向下各扩展此值）
     * @param filter 实体过滤器
     * @return 查找结果，包含所有符合条件的实体列表（按距离从近到远排序）
     */
    @NotNull
    public static EntitySearchResult findAllEntities(
            @NotNull Level level,
            @NotNull BlockPos centerPos,
            double radius,
            double height,
            @NotNull EntityFilter filter) {
        
        // 计算搜索区域的边界框
        AABB searchBox = createSearchBox(centerPos, radius, height);
        
        // 获取搜索区域内的所有实体
        List<Entity> allEntities = level.getEntities(null, searchBox);
        
        if (allEntities.isEmpty()) {
            return EntitySearchResult.EMPTY_LIST;
        }
        
        // 预计算中心坐标为float
        float centerX = (float)centerPos.getX() + 0.5f;
        float centerY = (float)centerPos.getY() + 0.5f;
        float centerZ = (float)centerPos.getZ() + 0.5f;
        
        // 过滤并计算距离
        List<EntityWithDistance> filteredEntities = new ArrayList<>();
        
        for (Entity entity : allEntities) {
            if (filter.test(entity)) {
                float distanceSq = getDistanceSquaredFloat(entity, centerX, centerY, centerZ);
                filteredEntities.add(new EntityWithDistance(entity, distanceSq));
            }
        }
        
        if (filteredEntities.isEmpty()) {
            return EntitySearchResult.EMPTY_LIST;
        }
        
        // 按距离排序
        filteredEntities.sort(Comparator.comparingDouble(e -> e.distanceSq));
        
        // 提取实体列表
        List<Entity> resultEntities = new ArrayList<>(filteredEntities.size());
        for (EntityWithDistance ewd : filteredEntities) {
            resultEntities.add(ewd.entity);
        }
        
        return new EntitySearchResult(resultEntities);
    }
    
    /**
     * 查找最近的符合条件的实体（使用自定义谓词）
     * 
     * @param level 世界对象
     * @param centerPos 中心坐标
     * @param radius 水平查找半径
     * @param height 垂直查找高度
     * @param predicate 自定义过滤谓词
     * @return 查找结果
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
     * 
     * @param level 世界对象
     * @param centerPos 中心坐标
     * @param radius 水平查找半径
     * @param height 垂直查找高度
     * @param predicate 自定义过滤谓词
     * @return 查找结果
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
    
    /**
     * 创建搜索区域的边界框
     * 
     * @param centerPos 中心位置
     * @param radius 水平半径
     * @param height 垂直高度
     * @return 边界框
     */
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
     * 计算实体到中心点的距离平方（使用float以提升性能）
     * 
     * @param entity 实体
     * @param centerX 中心点 X 坐标
     * @param centerY 中心点 Y 坐标
     * @param centerZ 中心点 Z 坐标
     * @return 距离的平方
     */
    private static float getDistanceSquaredFloat(@NotNull Entity entity, float centerX, float centerY, float centerZ) {
        float dx = (float)entity.getX() - centerX;
        float dy = (float)entity.getY() - centerY;
        float dz = (float)entity.getZ() - centerZ;
        return dx * dx + dy * dy + dz * dz;
    }
    
    /**
     * 内部类：实体与距离的包装类
     * 用于排序
     */
    private static class EntityWithDistance {
        final Entity entity;
        final float distanceSq;  // 使用float替代double提升性能
        
        EntityWithDistance(Entity entity, float distanceSq) {
            this.entity = entity;
            this.distanceSq = distanceSq;
        }
    }
}
