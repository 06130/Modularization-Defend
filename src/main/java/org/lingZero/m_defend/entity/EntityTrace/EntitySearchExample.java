package org.lingZero.m_defend.entity.EntityTrace;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 实体搜索系统使用示例
 * 
 * 本类展示了如何使用实体搜索系统的各种功能
 */
public class EntitySearchExample {
    
    /**
     * 示例1：查找最近的敌对生物
     */
    public static void example1_findNearestHostile(Level level, BlockPos pos) {
        // 方法1：使用便捷接口
        Entity nearestHostile = IEntitySearch.findNearestHostileMob(level, pos, 20.0);
        
        if (nearestHostile != null) {
            System.out.println("找到敌对生物: " + nearestHostile.getType().getDescriptionId());
            System.out.println("距离: " + nearestHostile.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()));
        }
    }
    
    /**
     * 示例2：查找范围内的所有玩家
     */
    public static void example2_findAllPlayers(Level level, BlockPos pos) {
        // 使用便捷接口获取所有玩家
        List<Entity> players = IEntitySearch.findAllPlayers(level, pos, 50.0);
        
        System.out.println("找到 " + players.size() + " 个玩家");
        for (Entity player : players) {
            System.out.println("玩家: " + player.getName().getString());
        }
    }
    
    /**
     * 示例3：使用自定义过滤器 - 查找特定的实体类型
     */
    public static void example3_customFilter(Level level, BlockPos pos) {
        // 创建只匹配僵尸的过滤器
        EntityFilter zombieFilter = EntityFilters.byEntityType(EntityType.ZOMBIE);
        
        // 查找最近的僵尸
        Entity nearestZombie = IEntitySearch.findNearest(
                level, pos, 30.0, 10.0, zombieFilter
        );
        
        if (nearestZombie != null) {
            System.out.println("找到僵尸，距离: " + nearestZombie.distanceToSqr(pos.getCenter()));
        }
    }
    
    /**
     * 示例4：组合过滤器 - 查找敌对生物或中立生物
     */
    public static void example4_combinedFilter(Level level, BlockPos pos) {
        // 组合多个过滤器（逻辑或）
        EntityFilter combinedFilter = EntityFilters.or(
                EntityFilters.hostileMobs(),
                EntityFilters.neutralMobs()
        );
        
        // 查找所有符合条件的实体
        List<Entity> entities = IEntitySearch.findAll(
                level, pos, 25.0, 15.0, combinedFilter
        );
        
        System.out.println("找到 " + entities.size() + " 个敌对或中立生物");
    }
    
    /**
     * 示例5：使用实体ID过滤器
     */
    public static void example5_entityIdFilter(Level level, BlockPos pos) {
        // 通过实体ID过滤
        EntityFilter creeperFilter = EntityFilters.byEntityId("minecraft:creeper");
        
        // 查找最近的苦力怕
        Entity nearestCreeper = IEntitySearch.findNearest(
                level, pos, 40.0, 20.0, creeperFilter
        );
        
        if (nearestCreeper != null) {
            System.out.println("发现苦力怕！快跑！");
        }
    }
    
    /**
     * 示例6：检查范围内是否有实体
     */
    public static void example6_checkExistence(Level level, BlockPos pos) {
        // 快速检查范围内是否有敌对生物
        boolean hasHostile = IEntitySearch.hasEntity(
                level, pos, 15.0, 10.0, EntityFilters.hostileMobs()
        );
        
        if (hasHostile) {
            System.out.println("警告：附近有敌对生物！");
        }
    }
    
    /**
     * 示例7：统计实体数量
     */
    public static void example7_countEntities(Level level, BlockPos pos) {
        // 统计范围内的友好生物数量
        int friendlyCount = IEntitySearch.countEntities(
                level, pos, 30.0, 15.0, EntityFilters.friendlyMobs()
        );
        
        System.out.println("附近有 " + friendlyCount + " 个友好生物");
    }
    
    /**
     * 示例8：使用完整的搜索结果对象
     */
    public static void example8_fullResult(Level level, BlockPos pos) {
        // 获取完整的搜索结果
        EntitySearchResult result = EntitySearchUtil.findAllEntities(
                level, pos, 50.0, 25.0, EntityFilters.hostileMobs()
        );
        
        // 检查结果
        if (result.hasEntities()) {
            System.out.println("找到 " + result.getTotalCount() + " 个敌对生物");
            
            // 获取最近的实体
            Entity nearest = result.getNearestEntity();
            System.out.println("最近的是: " + nearest.getType().getDescriptionId());
            
            // 获取所有实体列表
            List<Entity> all = result.getAllEntities();
            for (int i = 0; i < all.size(); i++) {
                System.out.println((i + 1) + ". " + all.get(i).getType().getDescriptionId());
            }
        } else {
            System.out.println("安全！没有发现敌对生物");
        }
    }
    
    /**
     * 示例9：复杂的组合过滤器
     */
    public static void example9_complexFilter(Level level, BlockPos pos) {
        // 创建复杂过滤器：敌对生物且不是苦力怕
        EntityFilter complexFilter = EntityFilters.and(
                EntityFilters.hostileMobs(),
                EntityFilters.not(EntityFilters.byEntityId("minecraft:creeper"))
        );
        
        // 或者：多个特定实体ID
        Set<String> targetIds = Set.of(
                "minecraft:zombie",
                "minecraft:skeleton",
                "minecraft:spider"
        );
        EntityFilter multiIdFilter = EntityFilters.byEntityIds(targetIds);
        
        List<Entity> targets = IEntitySearch.findAll(
                level, pos, 30.0, 15.0, multiIdFilter
        );
        
        System.out.println("找到 " + targets.size() + " 个目标实体");
    }
    
    /**
     * 示例10：在方块实体中使用（如炮塔）
     */
    public static void example10_turretUsage(Level level, BlockPos turretPos) {
        // 假设这是一个炮塔的tick方法
        
        // 查找最近的敌对生物作为目标
        Entity target = IEntitySearch.findNearestHostileMob(level, turretPos, 20.0);
        
        if (target != null) {
            // 找到目标，进行攻击
            double distance = target.distanceToSqr(turretPos.getCenter());
            if (distance <= 400.0) { // 20格半径
                System.out.println("炮塔瞄准目标: " + target.getName().getString());
                // 执行攻击逻辑...
            }
        }
    }
    
    /**
     * 示例11：使用实体追踪器 - 炮塔持续锁定目标
     */
    public static class TurretWithTracker {
        private EntityTracker tracker;
        private final Level level;
        private final BlockPos turretPos;
        
        public TurretWithTracker(Level level, BlockPos pos) {
            this.level = level;
            this.turretPos = pos;
            // 创建追踪器，搜索半径30格，高度15格
            this.tracker = IEntitySearch.createTracker(
                    level, pos, 30.0, 15.0, EntityFilters.hostileMobs()
            );
        }
        
        /**
         * 在每个 tick 调用此方法
         */
        public void tick() {
            // 更新追踪状态
            IEntityTracker.TrackingState state = tracker.update();
            
            switch (state) {
                case UNLOCKED -> {
                    // 未锁定，尝试锁定新目标
                    if (tracker.tryLock()) {
                        System.out.println("炮塔已锁定新目标");
                    }
                }
                case TRACKING -> {
                    // 正在追踪，获取目标并攻击
                    Entity target = tracker.getTrackedEntity();
                    if (target != null) {
                        double distance = target.distanceToSqr(turretPos.getCenter());
                        System.out.println("追踪目标中，距离: " + Math.sqrt(distance));
                        // 执行攻击逻辑...
                    }
                }
                case LOST -> {
                    // 目标丢失，尝试重新锁定
                    System.out.println("目标丢失，尝试重新锁定... (" + tracker.getLostTicks() + " ticks)");
                    if (!tracker.tryLock()) {
                        System.out.println("未能重新锁定，继续搜索...");
                    }
                }
                case INVALID -> {
                    // 追踪器失效，重新创建
                    System.out.println("追踪器失效，重置...");
                    tracker = IEntitySearch.createTracker(
                            level, turretPos, 30.0, 15.0, EntityFilters.hostileMobs()
                    );
                }
            }
        }
        
        /**
         * 手动释放当前目标
         */
        public void releaseTarget() {
            tracker.release();
            System.out.println("手动释放目标");
        }
    }
    
    /**
     * 示例12：使用追踪器 - AI 跟随玩家
     */
    public static class AIFollower {
        private EntityTracker tracker;
        private final Level level;
        
        public AIFollower(Level level, BlockPos startPos) {
            this.level = level;
            // 创建追踪玩家的追踪器
            this.tracker = IEntitySearch.createAndLockTracker(
                    level, startPos, 50.0, 25.0, EntityFilters.players()
            );
        }
        
        public void tick() {
            if (tracker.isTracking()) {
                Entity player = tracker.getTrackedEntity();
                if (player != null) {
                    // 跟随玩家
                    double distance = player.distanceToSqr(tracker.getSourcePos().getCenter());
                    if (distance > 100.0) { // 超过10格
                        System.out.println("玩家太远，开始跟随");
                        // 执行移动逻辑...
                    }
                }
            } else if (tracker.isLost()) {
                System.out.println("玩家丢失，等待重新出现...");
            } else {
                // 未锁定，尝试锁定
                tracker.tryLock();
            }
        }
    }
    
    /**
     * 示例13：多目标追踪系统
     */
    public static class MultiTargetTracker {
        private final List<EntityTracker> trackers;
        private final Level level;
        private final BlockPos centerPos;
        
        public MultiTargetTracker(Level level, BlockPos pos, int maxTargets) {
            this.level = level;
            this.centerPos = pos;
            this.trackers = new ArrayList<>();
            
            // 预创建多个追踪器
            for (int i = 0; i < maxTargets; i++) {
                trackers.add(IEntitySearch.createTracker(
                        level, pos, 40.0, 20.0, EntityFilters.hostileMobs()
                ));
            }
        }
        
        public void tick() {
            // 更新所有追踪器
            for (EntityTracker tracker : trackers) {
                IEntityTracker.TrackingState state = tracker.update();
                
                if (state == IEntityTracker.TrackingState.UNLOCKED ||
                    state == IEntityTracker.TrackingState.LOST) {
                    // 尝试锁定新目标
                    tracker.tryLock();
                }
            }
            
            // 统计当前追踪的目标数量
            long trackingCount = trackers.stream()
                    .filter(EntityTracker::isTracking)
                    .count();
            
            System.out.println("当前追踪 " + trackingCount + " 个目标");
        }
        
        /**
         * 获取所有正在追踪的目标
         */
        public List<Entity> getTrackedTargets() {
            return trackers.stream()
                    .filter(EntityTracker::isTracking)
                    .map(EntityTracker::getTrackedEntity)
                    .filter(java.util.Objects::nonNull)
                    .collect(java.util.stream.Collectors.toList());
        }

        public Level getLevel() {
            return level;
        }

        public BlockPos getCenterPos() {
            return centerPos;
        }
    }
    
    /**
     * 性能提示：
     * 1. 如果只需要判断是否存在，使用 hasEntity() 而不是 findAll()
     * 2. 如果只需要最近的实体，使用 findNearestXxx() 而不是 findAllXxx()
     * 3. 合理设置搜索范围，不要过大
     * 4. 避免在高频tick中频繁调用大范围搜索
     * 5. 考虑缓存搜索结果，特别是对于静态或变化缓慢的场景
     */
}
