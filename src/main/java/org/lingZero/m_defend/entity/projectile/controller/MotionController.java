package org.lingZero.m_defend.entity.projectile.controller;

import net.minecraft.world.phys.Vec3;
import org.lingZero.m_defend.entity.projectile.config.ProjectileParameters;
import org.lingZero.m_defend.util.DebugLogger;

/**
 * 运动控制器
 * 负责处理子弹的移动逻辑和超时检测
 * 
 * 职责：
 * 1. 根据速度和方向计算每tick的移动向量
 * 2. 应用重力效果（如果启用）
 * 3. 维护寿命计时器
 * 4. 检测并报告超时状态
 */
public class MotionController {
    
    private final double speed;
    private final boolean affectedByGravity;
    private final double gravityCoefficient;
    private final int maxLifetime;
    
    private int currentLifetime;
    private Vec3 currentDirection;
    private boolean expired;
    
    /**
     * 构造函数
     * 
     * @param params 子弹参数配置
     */
    public MotionController(ProjectileParameters params) {
        this.speed = params.getSpeed();
        this.affectedByGravity = params.isAffectedByGravity();
        this.gravityCoefficient = params.getGravityCoefficient();
        this.maxLifetime = params.getMaxLifetime();
        this.currentLifetime = maxLifetime;
        this.currentDirection = Vec3.ZERO;
        this.expired = false;
    }
    
    /**
     * 初始化运动方向
     * 
     * @param direction 归一化的方向向量
     */
    public void initializeDirection(Vec3 direction) {
        this.currentDirection = direction.normalize();
        if (DebugLogger.isDebugEnabled()) {
            DebugLogger.debug("MotionController 初始化: 方向=%s, 速度=%.2f", 
                    currentDirection.toString(), speed);
        }
    }
    
    /**
     * 计算下一tick的移动向量
     * 优化：减少不必要的对象创建
     * 
     * @param currentMotion 当前速度向量
     * @return 应用物理后的新速度向量
     */
    public Vec3 calculateMovement(Vec3 currentMotion) {
        if (expired) {
            return Vec3.ZERO;
        }
        
        // 如果没有初始方向，使用当前的运动方向
        if (currentDirection.lengthSqr() == 0 && currentMotion.lengthSqr() > 0) {
            currentDirection = currentMotion.normalize();
        }
        
        // 保持基础速度（复用对象）
        double speedX = currentDirection.x * speed;
        double speedY = currentDirection.y * speed;
        double speedZ = currentDirection.z * speed;
        
        // 应用重力
        if (affectedByGravity) {
            speedY -= gravityCoefficient;
        }
        
        return new Vec3(speedX, speedY, speedZ);
    }
    
    /**
     * 减少寿命并检查是否超时
     * 
     * @return true 如果已超时
     */
    public boolean tick() {
        if (expired) {
            return true;
        }
        
        currentLifetime--;
        
        if (currentLifetime <= 0) {
            expired = true;
            if (DebugLogger.isDebugEnabled()) {
                DebugLogger.debug("MotionController: 子弹超时，剩余寿命=%d", currentLifetime);
            }
            return true;
        }
        
        return false;
    }
    
    /**
     * 检查是否已超时
     * 
     * @return true 如果已超时
     */
    public boolean isExpired() {
        return expired;
    }
    
    /**
     * 获取剩余寿命
     * 
     * @return 剩余寿命（ticks）
     */
    public int getRemainingLifetime() {
        return Math.max(0, currentLifetime);
    }
    
    /**
     * 获取最大寿命
     * 
     * @return 最大寿命（ticks）
     */
    public int getMaxLifetime() {
        return maxLifetime;
    }
    
    /**
     * 获取当前运动方向
     * 
     * @return 方向向量
     */
    public Vec3 getCurrentDirection() {
        return currentDirection;
    }
    
    /**
     * 更新运动方向（用于制导系统）
     * 
     * @param newDirection 新的方向向量
     */
    public void updateDirection(Vec3 newDirection) {
        if (!expired && newDirection.lengthSqr() > 0) {
            this.currentDirection = newDirection.normalize();
        }
    }
    
    /**
     * 强制标记为过期
     */
    public void markAsExpired() {
        this.expired = true;
        this.currentLifetime = 0;
    }
    
    /**
     * 获取已飞行时间
     * 
     * @return 已飞行时间（ticks）
     */
    public long getFlightTime() {
        return maxLifetime - currentLifetime;
    }
}
