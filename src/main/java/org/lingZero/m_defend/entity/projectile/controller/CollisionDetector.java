package org.lingZero.m_defend.entity.projectile.controller;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.lingZero.m_defend.util.BlockUtils;
import org.lingZero.m_defend.util.DebugLogger;

/**
 * 碰撞检测器
 * 负责检测子弹与方块和实体的碰撞
 * 
 * 职责：
 * 1. 执行射线检测检查前方路径
 * 2. 识别命中的方块类型
 * 3. 绕过 ModularizationDefend 模组方块
 * 4. 检测实体碰撞
 * 5. 返回碰撞结果
 */
public class CollisionDetector {
    
    private final Level level;
    private final Entity projectileEntity;
    private int ticksSinceSpawn;
    
    /**
     * 构造函数
     * 
     * @param level 世界实例
     * @param projectileEntity 子弹实体引用
     */
    public CollisionDetector(Level level, Entity projectileEntity) {
        this.level = level;
        this.projectileEntity = projectileEntity;
        this.ticksSinceSpawn = 0;
    }
    
    /**
     * 增加生成后的tick计数
     */
    public void incrementTick() {
        this.ticksSinceSpawn++;
    }
    
    /**
     * 检测从起点到终点的碰撞
     * 
     * @param from 起点位置
     * @param to 终点位置
     * @return 碰撞检测结果
     */
    public HitResult detectCollision(Vec3 from, Vec3 to) {
        // 执行射线检测
        HitResult hitResult = level.clip(
            new ClipContext(
                from,
                to,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                projectileEntity
            )
        );
        
        return hitResult;
    }
    
    /**
     * 检查是否应该忽略方块碰撞
     * 子弹生成后的第一个tick内忽略碰撞，避免被生成位置的方块阻挡
     * 
     * @param hitResult 碰撞结果
     * @return true 如果应该忽略
     */
    public boolean shouldIgnoreCollision(HitResult hitResult) {
        // 生成后的第一个tick内忽略方块碰撞
        if (ticksSinceSpawn <= 1 && hitResult.getType() == HitResult.Type.BLOCK) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 检查是否应该绕过该方块（模组方块）
     * 
     * @param blockState 方块状态
     * @return true 如果应该绕过
     */
    public boolean shouldBypassBlock(BlockState blockState) {
        return BlockUtils.isModBlock(blockState);
    }
    
    /**
     * 处理碰撞结果
     * 优化：提前返回，减少不必要的计算
     * 
     * @param hitResult 碰撞结果
     * @param currentPosition 当前位置
     * @param targetPosition 目标位置
     * @return 处理后的最终位置
     */
    public Vec3 handleCollision(HitResult hitResult, Vec3 currentPosition, Vec3 targetPosition) {
        // 如果应该忽略碰撞，直接移动到目标位置
        if (shouldIgnoreCollision(hitResult)) {
            return targetPosition;
        }
        
        // 处理方块碰撞
        if (hitResult instanceof BlockHitResult blockHitResult) {
            BlockState blockState = level.getBlockState(blockHitResult.getBlockPos());
            
            // 检查是否是模组方块（快速路径）
            if (BlockUtils.isModBlock(blockState)) {
                return targetPosition;
            }
            
            // 非模组方块，发生碰撞
            if (DebugLogger.isDebugEnabled()) {
                DebugLogger.debug("CollisionDetector: 击中方块 %s", 
                        blockState.getBlock().getDescriptionId());
            }
            return currentPosition; // 停留在碰撞前的位置
        }
        
        // 处理实体碰撞
        if (hitResult instanceof EntityHitResult) {
            return currentPosition;
        }
        
        // 未命中，移动到目标位置
        return targetPosition;
    }
    
    /**
     * 检查是否有有效碰撞（不考虑绕过的方块）
     * 
     * @param hitResult 碰撞结果
     * @return true 如果有有效碰撞
     */
    public boolean hasValidCollision(HitResult hitResult) {
        if (hitResult.getType() == HitResult.Type.MISS) {
            return false;
        }
        
        // 如果是方块碰撞，检查是否是模组方块
        if (hitResult instanceof BlockHitResult blockHitResult) {
            BlockState blockState = level.getBlockState(blockHitResult.getBlockPos());
            return !shouldBypassBlock(blockState);
        }
        
        // 实体碰撞总是有效的
        return hitResult.getType() == HitResult.Type.ENTITY;
    }
    
    /**
     * 获取当前tick数
     * 
     * @return tick数
     */
    public int getTicksSinceSpawn() {
        return ticksSinceSpawn;
    }
    
    /**
     * 重置tick计数
     */
    public void resetTicks() {
        this.ticksSinceSpawn = 0;
    }
}
