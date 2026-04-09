package org.lingZero.m_defend.entity.projectile.config;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

/**
 * 子弹参数配置类
 * 封装子弹发射时的所有传入参数
 * 
 * 使用构建器模式创建实例
 */
public class ProjectileParameters {
    
    private final BlockPos sourcePosition;
    private final double speed;
    private final boolean affectedByGravity;
    private final double gravityCoefficient;
    @Nullable
    private final Entity guidedTarget;
    private final int maxLifetime;
    
    private ProjectileParameters(Builder builder) {
        this.sourcePosition = builder.sourcePosition;
        this.speed = builder.speed;
        this.affectedByGravity = builder.affectedByGravity;
        this.gravityCoefficient = builder.gravityCoefficient;
        this.guidedTarget = builder.guidedTarget;
        this.maxLifetime = builder.maxLifetime;
    }
    
    /**
     * 获取发射源位置（主方块坐标）
     * 
     * @return 源位置
     */
    public BlockPos getSourcePosition() {
        return sourcePosition;
    }
    
    /**
     * 获取速度值（标量）
     * 
     * @return 速度
     */
    public double getSpeed() {
        return speed;
    }
    
    /**
     * 检查是否受重力影响
     * 
     * @return true 如果受重力影响
     */
    public boolean isAffectedByGravity() {
        return affectedByGravity;
    }
    
    /**
     * 获取重力系数
     * 
     * @return 重力系数
     */
    public double getGravityCoefficient() {
        return gravityCoefficient;
    }
    
    /**
     * 获取制导目标实体
     * 
     * @return 制导目标，若未设置则返回 null
     */
    @Nullable
    public Entity getGuidedTarget() {
        return guidedTarget;
    }
    
    /**
     * 检查是否启用制导
     * 
     * @return true 如果有制导目标
     */
    public boolean hasGuidance() {
        return guidedTarget != null;
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
     * 创建构建器
     * 
     * @return 新的构建器实例
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * 参数构建器
     */
    public static class Builder {
        private BlockPos sourcePosition = BlockPos.ZERO;
        private double speed = 1.0;
        private boolean affectedByGravity = false;
        private double gravityCoefficient = 0.08;
        @Nullable
        private Entity guidedTarget = null;
        private int maxLifetime = 200;
        
        /**
         * 设置发射源位置
         * 
         * @param position 源位置
         * @return 构建器自身
         */
        public Builder source(BlockPos position) {
            this.sourcePosition = position;
            return this;
        }
        
        /**
         * 设置速度值
         * 
         * @param speed 速度（必须为正数）
         * @return 构建器自身
         */
        public Builder speed(double speed) {
            if (speed <= 0) {
                throw new IllegalArgumentException("Speed must be positive");
            }
            this.speed = speed;
            return this;
        }
        
        /**
         * 启用重力效果（使用默认重力系数 0.08）
         * 
         * @return 构建器自身
         */
        public Builder withGravity() {
            this.affectedByGravity = true;
            this.gravityCoefficient = 0.08;
            return this;
        }
        
        /**
         * 启用重力效果并指定重力系数
         * 
         * @param coefficient 重力系数（建议范围 0.01-0.2）
         * @return 构建器自身
         */
        public Builder withGravity(double coefficient) {
            if (coefficient <= 0) {
                throw new IllegalArgumentException("Gravity coefficient must be positive");
            }
            this.affectedByGravity = true;
            this.gravityCoefficient = coefficient;
            return this;
        }
        
        /**
         * 设置制导目标
         * 
         * @param target 目标实体
         * @return 构建器自身
         */
        public Builder guidedTarget(Entity target) {
            this.guidedTarget = target;
            return this;
        }
        
        /**
         * 设置最大寿命
         * 
         * @param ticks 寿命（ticks）
         * @return 构建器自身
         */
        public Builder lifetime(int ticks) {
            if (ticks <= 0) {
                throw new IllegalArgumentException("Lifetime must be positive");
            }
            this.maxLifetime = ticks;
            return this;
        }
        
        /**
         * 构建参数对象
         * 
         * @return 新的 ProjectileParameters 实例
         */
        public ProjectileParameters build() {
            return new ProjectileParameters(this);
        }
    }
    
    @Override
    public String toString() {
        return String.format("ProjectileParameters{source=%s, speed=%.2f, gravity=%b, gravityCoeff=%.3f, hasGuidance=%b, lifetime=%d}",
                sourcePosition, speed, affectedByGravity, gravityCoefficient, hasGuidance(), maxLifetime);
    }
}
