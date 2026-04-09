package org.lingZero.m_defend.entity.projectile.core;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * 子弹命中结果封装类
 * 用于标准化返回子弹的检测结果
 */
public class ProjectileHitResult {
    
    private final boolean hit;
    @Nullable
    private final Vec3 hitPosition;
    @Nullable
    private final Entity hitEntity;
    @Nullable
    private final BlockState hitBlock;
    private final long flightTime;
    
    private ProjectileHitResult(boolean hit, @Nullable Vec3 hitPosition, 
                                @Nullable Entity hitEntity, @Nullable BlockState hitBlock,
                                long flightTime) {
        this.hit = hit;
        this.hitPosition = hitPosition;
        this.hitEntity = hitEntity;
        this.hitBlock = hitBlock;
        this.flightTime = flightTime;
    }
    
    /**
     * 创建未命中结果
     * 
     * @param flightTime 飞行时间（ticks）
     * @return 未命中结果
     */
    public static ProjectileHitResult miss(long flightTime) {
        return new ProjectileHitResult(false, null, null, null, flightTime);
    }
    
    /**
     * 创建击中实体结果
     * 
     * @param position 击中位置
     * @param entity 被击中的实体
     * @param flightTime 飞行时间（ticks）
     * @return 击中结果
     */
    public static ProjectileHitResult hitEntity(Vec3 position, Entity entity, long flightTime) {
        return new ProjectileHitResult(true, position, entity, null, flightTime);
    }
    
    /**
     * 创建击中方块结果
     * 
     * @param position 击中位置
     * @param blockState 被击中的方块状态
     * @param flightTime 飞行时间（ticks）
     * @return 击中结果
     */
    public static ProjectileHitResult hitBlock(Vec3 position, BlockState blockState, long flightTime) {
        return new ProjectileHitResult(true, position, null, blockState, flightTime);
    }
    
    /**
     * 检查是否击中目标
     * 
     * @return true 如果击中
     */
    public boolean isHit() {
        return hit;
    }
    
    /**
     * 获取击中位置
     * 
     * @return 击中位置的 Optional
     */
    public Optional<Vec3> getHitPosition() {
        return Optional.ofNullable(hitPosition);
    }
    
    /**
     * 获取被击中的实体
     * 
     * @return 被击中实体的 Optional
     */
    public Optional<Entity> getHitEntity() {
        return Optional.ofNullable(hitEntity);
    }
    
    /**
     * 获取被击中的方块状态
     * 
     * @return 被击中方块的 Optional
     */
    public Optional<BlockState> getHitBlock() {
        return Optional.ofNullable(hitBlock);
    }
    
    /**
     * 获取飞行时间
     * 
     * @return 飞行时间（ticks）
     */
    public long getFlightTime() {
        return flightTime;
    }
    
    @Override
    public String toString() {
        if (!hit) {
            return String.format("ProjectileHitResult{miss, flightTime=%d}", flightTime);
        }
        
        StringBuilder sb = new StringBuilder("ProjectileHitResult{hit");
        if (hitEntity != null) {
            sb.append(", entity=").append(hitEntity.getType().getDescriptionId());
        }
        if (hitBlock != null) {
            sb.append(", block=").append(hitBlock.getBlock().getDescriptionId());
        }
        if (hitPosition != null) {
            sb.append(", position=").append(hitPosition);
        }
        sb.append(", flightTime=").append(flightTime).append("}");
        return sb.toString();
    }
}
