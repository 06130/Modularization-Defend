package org.lingZero.m_defend.entity.projectile.controller;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.lingZero.m_defend.util.DebugLogger;

/**
 * 制导系统
 * 负责处理子弹的目标追踪逻辑
 * 
 * 职责：
 * 1. 判断是否需要制导
 * 2. 计算朝向目标的方向向量
 * 3. 平滑调整运动轨迹
 * 4. 处理目标丢失或死亡的情况
 */
public class GuidanceSystem {
    
    // 制导强度系数（0-1之间），值越大转向越快
    private static final double GUIDANCE_STRENGTH = 0.15;
    // 性能优化：制导更新间隔（ticks），减少计算频率
    private static final int GUIDANCE_UPDATE_INTERVAL = 5;
    
    @Nullable
    private Entity target;
    private boolean guidanceEnabled;
    private int lostTargetTicks;
    private static final int MAX_LOST_TICKS = 20; // 目标丢失后最多继续制导的tick数
    
    // 性能优化：缓存上次制导计算的结果
    private int lastGuidanceTick = 0;
    private Vec3 cachedDirection = Vec3.ZERO;
    
    /**
     * 构造函数
     */
    public GuidanceSystem() {
        this.target = null;
        this.guidanceEnabled = false;
        this.lostTargetTicks = 0;
    }
    
    /**
     * 设置制导目标
     * 
     * @param target 目标实体
     */
    public void setTarget(@Nullable Entity target) {
        this.target = target;
        if (target != null) {
            this.guidanceEnabled = true;
            this.lostTargetTicks = 0;
            DebugLogger.debug("GuidanceSystem: 设置制导目标=%s", target.getType().getDescriptionId());
        } else {
            this.guidanceEnabled = false;
        }
    }
    
    /**
     * 检查是否应该应用制导
     * 
     * @return true 如果应该应用制导
     */
    public boolean shouldApplyGuidance() {
        if (!guidanceEnabled || target == null) {
            return false;
        }
        
        // 检查目标是否仍然有效
        if (!target.isAlive()) {
            lostTargetTicks++;
            if (lostTargetTicks > MAX_LOST_TICKS) {
                DebugLogger.debug("GuidanceSystem: 目标死亡超过 %d ticks，禁用制导", MAX_LOST_TICKS);
                guidanceEnabled = false;
                return false;
            }
            return true; // 短期内仍尝试制导
        }
        
        // 重置丢失计数
        lostTargetTicks = 0;
        return true;
    }
    
    /**
     * 应用制导逻辑，计算新的运动方向
     * 优化：减少计算频率，缓存结果
     * 
     * @param currentMotion 当前速度向量
     * @param currentPos 当前位置
     * @param currentTick 当前tick数
     * @return 应用制导后的新速度向量
     */
    public Vec3 applyGuidance(Vec3 currentMotion, Vec3 currentPos, int currentTick) {
        if (!shouldApplyGuidance() || target == null) {
            return currentMotion;
        }
        
        // 性能优化：每隔几tick才重新计算制导
        if (currentTick - lastGuidanceTick < GUIDANCE_UPDATE_INTERVAL && cachedDirection.lengthSqr() > 0) {
            // 使用缓存的方向
            double speed = currentMotion.length();
            return cachedDirection.multiply(speed, speed, speed);
        }
        
        // 计算到目标的方向向量
        Vec3 targetPos = target.position();
        Vec3 toTarget = targetPos.subtract(currentPos);
        double distanceSqr = toTarget.lengthSqr();
        
        // 如果距离太近，直接飞向目标
        if (distanceSqr < 0.25) { // 0.5^2
            DebugLogger.debug("GuidanceSystem: 接近目标，距离=%.2f", Math.sqrt(distanceSqr));
            return currentMotion;
        }
        
        Vec3 targetDirection = toTarget.normalize();
        Vec3 currentDirection = currentMotion.normalize();
        
        // 混合当前方向和目標方向
        Vec3 newDirection = currentDirection.add(
                targetDirection.x * GUIDANCE_STRENGTH,
                targetDirection.y * GUIDANCE_STRENGTH,
                targetDirection.z * GUIDANCE_STRENGTH
        ).normalize();
        
        // 缓存结果
        this.cachedDirection = newDirection;
        this.lastGuidanceTick = currentTick;
        
        // 保持原有速度大小
        double speed = currentMotion.length();
        Vec3 newMotion = newDirection.multiply(speed, speed, speed);
        
        return newMotion;
    }
    
    /**
     * 获取当前制导目标
     * 
     * @return 目标实体，若未设置则返回 null
     */
    @Nullable
    public Entity getTarget() {
        return target;
    }
    
    /**
     * 检查是否启用了制导
     * 
     * @return true 如果启用制导
     */
    public boolean isGuidanceEnabled() {
        return guidanceEnabled;
    }
    
    /**
     * 禁用制导
     */
    public void disableGuidance() {
        this.guidanceEnabled = false;
        this.target = null;
        DebugLogger.debug("GuidanceSystem: 制导已禁用");
    }
    
    /**
     * 清除目标并重置状态
     */
    public void clearTarget() {
        this.target = null;
        this.guidanceEnabled = false;
        this.lostTargetTicks = 0;
    }
    
    /**
     * 获取目标丢失的tick数
     * 
     * @return 丢失tick数
     */
    public int getLostTargetTicks() {
        return lostTargetTicks;
    }
}
