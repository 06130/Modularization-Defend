package org.lingZero.m_defend.entity.EntityTrace;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 实体搜索便捷接口
 * 提供简化的静态方法，方便快速调用实体搜索功能
 */
public interface IEntitySearch {
    
    /**
     * 查找最近的敌对生物
     * 
     * @param level 世界对象
     * @param centerPos 中心坐标
     * @param radius 查找半径
     * @return 最近的敌对生物，未找到返回 null
     */
    @Nullable
    static Entity findNearestHostileMob(Level level, BlockPos centerPos, double radius) {
        return EntitySearchUtil.findNearestEntity(
                level, centerPos, radius, radius, EntityFilters.hostileMobs()
        ).getNearestEntity();
    }
    
    /**
     * 查找最近的玩家
     * 
     * @param level 世界对象
     * @param centerPos 中心坐标
     * @param radius 查找半径
     * @return 最近的玩家，未找到返回 null
     */
    @Nullable
    static Entity findNearestPlayer(Level level, BlockPos centerPos, double radius) {
        return EntitySearchUtil.findNearestEntity(
                level, centerPos, radius, radius, EntityFilters.players()
        ).getNearestEntity();
    }
    
    /**
     * 查找最近的友好生物
     * 
     * @param level 世界对象
     * @param centerPos 中心坐标
     * @param radius 查找半径
     * @return 最近的友好生物，未找到返回 null
     */
    @Nullable
    static Entity findNearestFriendlyMob(Level level, BlockPos centerPos, double radius) {
        return EntitySearchUtil.findNearestEntity(
                level, centerPos, radius, radius, EntityFilters.friendlyMobs()
        ).getNearestEntity();
    }
    
    /**
     * 查找指定范围内的所有敌对生物
     * 
     * @param level 世界对象
     * @param centerPos 中心坐标
     * @param radius 查找半径
     * @return 所有敌对生物列表（按距离排序）
     */
    @NotNull
    static List<Entity> findAllHostileMobs(Level level, BlockPos centerPos, double radius) {
        return EntitySearchUtil.findAllEntities(
                level, centerPos, radius, radius, EntityFilters.hostileMobs()
        ).getAllEntities();
    }
    
    /**
     * 查找指定范围内的所有玩家
     * 
     * @param level 世界对象
     * @param centerPos 中心坐标
     * @param radius 查找半径
     * @return 所有玩家列表（按距离排序）
     */
    @NotNull
    static List<Entity> findAllPlayers(Level level, BlockPos centerPos, double radius) {
        return EntitySearchUtil.findAllEntities(
                level, centerPos, radius, radius, EntityFilters.players()
        ).getAllEntities();
    }
    
    /**
     * 使用自定义过滤器查找最近的实体
     * 
     * @param level 世界对象
     * @param centerPos 中心坐标
     * @param radius 水平查找半径
     * @param height 垂直查找高度
     * @param filter 实体过滤器
     * @return 最近的实体，未找到返回 null
     */
    @Nullable
    static Entity findNearest(Level level, BlockPos centerPos, double radius, double height, EntityFilter filter) {
        return EntitySearchUtil.findNearestEntity(
                level, centerPos, radius, height, filter
        ).getNearestEntity();
    }
    
    /**
     * 使用自定义过滤器查找所有实体
     * 
     * @param level 世界对象
     * @param centerPos 中心坐标
     * @param radius 水平查找半径
     * @param height 垂直查找高度
     * @param filter 实体过滤器
     * @return 所有符合条件的实体列表（按距离排序）
     */
    @NotNull
    static List<Entity> findAll(Level level, BlockPos centerPos, double radius, double height, EntityFilter filter) {
        return EntitySearchUtil.findAllEntities(
                level, centerPos, radius, height, filter
        ).getAllEntities();
    }
    
    /**
     * 检查指定范围内是否存在符合条件的实体
     * 
     * @param level 世界对象
     * @param centerPos 中心坐标
     * @param radius 查找半径
     * @param height 查找高度
     * @param filter 实体过滤器
     * @return true 如果存在符合条件的实体
     */
    static boolean hasEntity(Level level, BlockPos centerPos, double radius, double height, EntityFilter filter) {
        return !EntitySearchUtil.findNearestEntity(
                level, centerPos, radius, height, filter
        ).isEmpty();
    }
    
    /**
     * 统计指定范围内符合条件的实体数量
     * 
     * @param level 世界对象
     * @param centerPos 中心坐标
     * @param radius 查找半径
     * @param height 查找高度
     * @param filter 实体过滤器
     * @return 实体数量
     */
    static int countEntities(Level level, BlockPos centerPos, double radius, double height, EntityFilter filter) {
        return EntitySearchUtil.findAllEntities(
                level, centerPos, radius, height, filter
        ).getTotalCount();
    }
    
    /**
     * 创建实体追踪器
     * 用于持续追踪已锁定的实体
     * 
     * @param level 世界对象
     * @param centerPos 中心坐标
     * @param radius 水平搜索半径
     * @param height 垂直搜索高度
     * @param filter 实体过滤器
     * @return 新创建的实体追踪器
     */
    @NotNull
    static EntityTracker createTracker(Level level, BlockPos centerPos, double radius, double height, EntityFilter filter) {
        return new EntityTracker(level, centerPos, radius, height, filter);
    }
    
    /**
     * 创建并自动锁定目标的追踪器
     * 
     * @param level 世界对象
     * @param centerPos 中心坐标
     * @param radius 水平搜索半径
     * @param height 垂直搜索高度
     * @param filter 实体过滤器
     * @return 已锁定目标的追踪器，如果未找到目标则返回未锁定状态的追踪器
     */
    @NotNull
    static EntityTracker createAndLockTracker(Level level, BlockPos centerPos, double radius, double height, EntityFilter filter) {
        EntityTracker tracker = new EntityTracker(level, centerPos, radius, height, filter);
        tracker.tryLock();
        return tracker;
    }
}
