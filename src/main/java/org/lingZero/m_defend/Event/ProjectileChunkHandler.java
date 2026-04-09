package org.lingZero.m_defend.Event;

import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import org.lingZero.m_defend.entity.projectile.core.AbstractProjectileEntity;
import org.lingZero.m_defend.util.DebugLogger;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 区块事件处理器
 * 处理区块加载/卸载事件，确保子弹实体正确清理
 */
public class ProjectileChunkHandler {
    
    /**
     * 追踪每个区块中的子弹实体
     * Key: ChunkPos, Value: 该区块中的子弹实体集合
     */
    private static final ConcurrentHashMap<ChunkPos, Set<AbstractProjectileEntity>> chunkProjectiles = new ConcurrentHashMap<>();
    
    /**
     * 注册子弹实体到区块追踪
     * 
     * @param projectile 子弹实体
     */
    public static void registerProjectileAsAbstract(AbstractProjectileEntity projectile) {
        if (projectile.level() == null) return;
        
        ChunkPos chunkPos = projectile.chunkPosition();
        chunkProjectiles
            .computeIfAbsent(chunkPos, k -> ConcurrentHashMap.newKeySet())
            .add(projectile);
    }
    
    /**
     * 从区块追踪中移除子弹实体
     * 
     * @param projectile 子弹实体
     */
    public static void unregisterProjectileAsAbstract(AbstractProjectileEntity projectile) {
        if (projectile.level() == null) return;
        
        ChunkPos chunkPos = projectile.chunkPosition();
        Set<AbstractProjectileEntity> projectiles = chunkProjectiles.get(chunkPos);
        if (projectiles != null) {
            projectiles.remove(projectile);
            if (projectiles.isEmpty()) {
                chunkProjectiles.remove(chunkPos);
            }
        }
    }
    
    /**
     * 更新子弹实体的区块追踪
     * 当子弹移动到新区块时调用
     * 
     * @param projectile 子弹实体
     * @param oldChunkPos 旧的区块位置
     */
    public static void updateProjectileChunkAsAbstract(AbstractProjectileEntity projectile, ChunkPos oldChunkPos) {
        ChunkPos newChunkPos = projectile.chunkPosition();
        
        if (!oldChunkPos.equals(newChunkPos)) {
            // 从旧区块移除
            unregisterProjectileFromChunk(projectile, oldChunkPos);
            // 添加到新区块
            registerProjectileToChunk(projectile, newChunkPos);
        }
    }
    
    private static void unregisterProjectileFromChunk(AbstractProjectileEntity projectile, ChunkPos chunkPos) {
        Set<AbstractProjectileEntity> projectiles = chunkProjectiles.get(chunkPos);
        if (projectiles != null) {
            projectiles.remove(projectile);
            if (projectiles.isEmpty()) {
                chunkProjectiles.remove(chunkPos);
            }
        }
    }
    
    private static void registerProjectileToChunk(AbstractProjectileEntity projectile, ChunkPos chunkPos) {
        chunkProjectiles
            .computeIfAbsent(chunkPos, k -> ConcurrentHashMap.newKeySet())
            .add(projectile);
    }
    
    /**
     * 区块卸载事件
     * 在区块卸载前清理其中的所有子弹实体
     */
    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            return;  // 只处理服务端
        }
        
        ChunkPos chunkPos = event.getChunk().getPos();
        Set<AbstractProjectileEntity> projectiles = chunkProjectiles.remove(chunkPos);
        
        if (projectiles != null && !projectiles.isEmpty()) {
            DebugLogger.debug("区块 %s 卸载，清理 %d 个子弹实体", chunkPos, projectiles.size());
            
            for (AbstractProjectileEntity projectile : projectiles) {
                if (projectile != null && !projectile.isRemoved()) {
                    projectile.discard();
                }
            }
            projectiles.clear();
        }
    }
    
    /**
     * 清理所有追踪数据
     * 通常在世界卸载时调用
     */
    public static void clearAll() {
        int totalEntities = chunkProjectiles.values().stream()
            .mapToInt(Set::size)
            .sum();
        
        DebugLogger.info("清理所有子弹实体追踪，共 %d 个实体", totalEntities);
        chunkProjectiles.clear();
    }
}
