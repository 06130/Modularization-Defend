package org.lingZero.modularization_defend.level;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.lingZero.modularization_defend.Block.example.BlueDoorBlockEntity;
import org.lingZero.modularization_defend.Block.example.RedDoorBlockEntity;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 门注册表——按维度记录已加载的红门/蓝门主方块位置（内存态，随 BE 加载/卸载自动增删）。
 * 关卡控制器通过它按门 ID 查找生成点（红门）与漏怪终点（蓝门）。
 */
public final class DoorRegistry {

    public enum DoorType { RED, BLUE }

    private static final Map<ResourceKey<Level>, Map<DoorType, Set<BlockPos>>> DOORS = new HashMap<>();

    private DoorRegistry() {}

    public static void register(Level level, DoorType type, BlockPos pos) {
        DOORS.computeIfAbsent(level.dimension(), k -> new EnumMap<>(DoorType.class))
                .computeIfAbsent(type, k -> new HashSet<>())
                .add(pos.immutable());
    }

    public static void unregister(Level level, DoorType type, BlockPos pos) {
        var byType = DOORS.get(level.dimension());
        if (byType != null) {
            var positions = byType.get(type);
            if (positions != null) positions.remove(pos);
        }
    }

    /**
     * 在 center 周围 maxDistance 范围内查找指定 ID 的门，返回最近的主方块位置。
     * 找不到返回 null。
     */
    @Nullable
    public static BlockPos findDoor(ServerLevel level, DoorType type, int doorId,
                                    BlockPos center, double maxDistance) {
        var byType = DOORS.get(level.dimension());
        if (byType == null) return null;
        var positions = byType.get(type);
        if (positions == null) return null;

        BlockPos nearest = null;
        double nearestDistSq = maxDistance * maxDistance;
        for (BlockPos pos : positions) {
            double distSq = pos.distSqr(center);
            if (distSq > nearestDistSq) continue;
            if (getDoorId(level, type, pos) == doorId) {
                nearest = pos;
                nearestDistSq = distSq;
            }
        }
        return nearest;
    }

    private static int getDoorId(ServerLevel level, DoorType type, BlockPos pos) {
        var be = level.getBlockEntity(pos);
        if (type == DoorType.RED && be instanceof RedDoorBlockEntity red) return red.getDoorId();
        if (type == DoorType.BLUE && be instanceof BlueDoorBlockEntity blue) return blue.getDoorId();
        return -1;
    }
}
