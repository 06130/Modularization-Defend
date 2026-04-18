package org.lingZero.m_defend.entity.EntityTrace;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.lingZero.m_defend.util.DebugLogger;

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
            return new EntitySearchResult((Entity) null);
        }
        
        // 查找最近的符合条件的实体
        Entity nearestEntity = null;
        double nearestDistanceSq = Double.MAX_VALUE;
        double centerX = centerPos.getX() + 0.5;
        double centerY = centerPos.getY() + 0.5;
        double centerZ = centerPos.getZ() + 0.5;
        
        for (Entity entity : allEntities) {
            // 快速排除不符合条件的实体
            if (!filter.test(entity)) {
                continue;
            }
            
            // 使用距离平方进行比较，避免开方运算
            double distanceSq = getDistanceSquared(entity, centerX, centerY, centerZ);
            
            // 更新最近的实体
            if (distanceSq < nearestDistanceSq) {
                nearestDistanceSq = distanceSq;
                nearestEntity = entity;
            }
        }
        
        if (nearestEntity != null) {
            DebugLogger.debug("找到最近实体: %s, 距离: %.2f", 
                    nearestEntity.getType().getDescriptionId(), 
                    Math.sqrt(nearestDistanceSq));
        } else {
            DebugLogger.debug("未找到符合条件的实体");
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
        
        if (level.isClientSide()) {
            DebugLogger.warn("在客户端调用实体查找方法，这可能导致不一致的结果");
        }
        
        // 计算搜索区域的边界框
        AABB searchBox = createSearchBox(centerPos, radius, height);
        
        // 获取搜索区域内的所有实体
        List<Entity> allEntities = level.getEntities(null, searchBox);
        
        if (allEntities.isEmpty()) {
            return new EntitySearchResult(List.of());
        }
        
        // 过滤并计算距离
        double centerX = centerPos.getX() + 0.5;
        double centerY = centerPos.getY() + 0.5;
        double centerZ = centerPos.getZ() + 0.5;
        
        List<EntityWithDistance> filteredEntities = new ArrayList<>();
        
        for (Entity entity : allEntities) {
            if (filter.test(entity)) {
                double distanceSq = getDistanceSquared(entity, centerX, centerY, centerZ);
                filteredEntities.add(new EntityWithDistance(entity, distanceSq));
            }
        }
        
        // 按距离排序
        filteredEntities.sort(Comparator.comparingDouble(e -> e.distanceSq));
        
        // 提取实体列表
        List<Entity> resultEntities = new ArrayList<>(filteredEntities.size());
        for (EntityWithDistance ewd : filteredEntities) {
            resultEntities.add(ewd.entity);
        }
        
        DebugLogger.debug("找到 %d 个符合条件的实体", resultEntities.size());
        
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
     * 计算实体到中心点的距离平方
     * 
     * @param entity 实体
     * @param centerX 中心点 X 坐标
     * @param centerY 中心点 Y 坐标
     * @param centerZ 中心点 Z 坐标
     * @return 距离的平方
     */
    private static double getDistanceSquared(@NotNull Entity entity, double centerX, double centerY, double centerZ) {
        double dx = entity.getX() - centerX;
        double dy = entity.getY() - centerY;
        double dz = entity.getZ() - centerZ;
        return dx * dx + dy * dy + dz * dz;
    }
    
    /**
     * 内部类：实体与距离的包装类
     * 用于排序
     */
    private static class EntityWithDistance {
        final Entity entity;
        final double distanceSq;
        
        EntityWithDistance(Entity entity, double distanceSq) {
            this.entity = entity;
            this.distanceSq = distanceSq;
        }
    }
}
