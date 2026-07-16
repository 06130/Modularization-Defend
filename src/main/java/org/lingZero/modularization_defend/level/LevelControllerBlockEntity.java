package org.lingZero.modularization_defend.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.lingZero.modularization_defend.Block.ModBlockEntities;
import org.lingZero.modularization_defend.Block.example.BlueDoorBlockEntity;
import org.lingZero.modularization_defend.Item.ProgramStorageCardItem;
import org.lingZero.modularization_defend.nodegraph.exec.ILevelHost;
import org.lingZero.modularization_defend.util.DataCompressUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 关卡控制器 BlockEntity——关卡系统的数据载体与运行时宿主。
 *
 * <p>持有节点图 NBT（磁盘存储走 GZIP 压缩），维护关卡状态机
 * （IDLE → RUNNING → VICTORY/DEFEAT），运行时每 tick 驱动
 * {@link LevelGraphRuntime}，并以 {@link ILevelHost} 身份承接动作节点的副作用。</p>
 */
public class LevelControllerBlockEntity extends BlockEntity implements ILevelHost {

    /** 消息广播与奖励发放的玩家搜索半径 */
    private static final double PLAYER_RANGE = 64.0;
    /** 从控制器查找红门/蓝门的最大距离 */
    private static final double DOOR_SEARCH_RANGE = 64.0;
    /** 敌人寻路移动速度倍率 */
    private static final double NAV_SPEED = 1.1;

    /** 图 NBT（内存中保持未压缩，便于编辑器读取与网络同步） */
    private CompoundTag graphTag = new CompoundTag();
    private LevelState state = LevelState.IDLE;
    private long runTicks;
    private int waveCount;
    /** 累计漏怪数（进入蓝门的敌人数） */
    private int leaks;
    /** 本关生成且仍在追踪的实体 UUID */
    private final Set<UUID> spawnedEntities = new LinkedHashSet<>();
    /** 追踪实体的目标蓝门主方块位置 */
    private final Map<UUID, BlockPos> entityTargets = new HashMap<>();
    /** 待续任务：延迟节点的定时恢复 */
    private final List<PendingResume> pendingResumes = new ArrayList<>();
    /** 待续任务：间隔生成中的波次 */
    private final List<PendingWave> pendingWaves = new ArrayList<>();
    /** 已完成 AI 改造的实体（内存态；存档重载后实体 AI 会还原为原生，需重新改造） */
    private final Set<UUID> aiApplied = new HashSet<>();

    /** 延迟恢复任务：ticks 归零后从 nodeUid 节点的 portId 端口恢复执行链 */
    private static class PendingResume {
        String nodeUid;
        String portId;
        int ticks;
    }

    /** 间隔波次任务：每 interval tick 生成 1 只，remaining 归零后恢复执行链 */
    private static class PendingWave {
        String entityId;
        int remaining;
        int interval;
        int ticksUntilNext;
        int redDoorId;
        int blueDoorId;
        String nodeUid;
        String portId;
    }

    private final LevelGraphRuntime runtime = new LevelGraphRuntime();

    public LevelControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LEVEL_CONTROLLER.get(), pos, state);
    }

    // ==================== tick ====================

    public static void serverTick(Level level, BlockPos pos, BlockState blockState, LevelControllerBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        if (be.state == LevelState.RUNNING) {
            be.runTicks++;
            int alive = be.updateSpawnedEntities(serverLevel);
            // 波次清空：已生成过敌人、全部消灭、且没有仍在间隔生成中的波次
            boolean waveCleared = be.waveCount > 0 && alive == 0 && be.pendingWaves.isEmpty();

            be.runtime.tick(be, level.registryAccess(), be.runTicks / 20f, be.waveCount, alive, waveCleared, be.leaks);
            be.processPendingTasks(serverLevel);

            // 定期落盘，保证计时器/边沿状态在存档重载后可续跑
            if (be.runTicks % 20 == 0) be.setChanged();
        } else if (be.state == LevelState.VICTORY || be.state == LevelState.DEFEAT) {
            // 终态仍处理延迟恢复，支持"关卡结果 > 延迟 > 复位关卡"的自动复位链
            be.processPendingResumes(serverLevel);
        }
    }

    /**
     * 追踪实体维护：清理死亡实体、漏怪判定（进入目标蓝门）、确保实体只保留前往蓝门的 AI。
     * 返回存活的追踪实体数。
     */
    private int updateSpawnedEntities(ServerLevel serverLevel) {
        var iterator = spawnedEntities.iterator();
        while (iterator.hasNext()) {
            UUID uuid = iterator.next();
            Entity entity = serverLevel.getEntity(uuid);
            if (entity == null || !entity.isAlive()) {
                iterator.remove();
                entityTargets.remove(uuid);
                aiApplied.remove(uuid);
                continue;
            }
            BlockPos target = entityTargets.get(uuid);
            if (target == null) continue;

            // 漏怪判定：实体碰撞箱进入蓝门结构范围
            if (entity.getBoundingBox().intersects(BlueDoorBlockEntity.STRUCTURE_AABB.move(target))) {
                entity.discard();
                iterator.remove();
                entityTargets.remove(uuid);
                aiApplied.remove(uuid);
                leaks++;
                broadcast(Component.translatable(
                        "block.modularization_defend.level_controller.leak", leaks));
                continue;
            }

            // 存档重载后实体的原生 AI 会还原，需重新改造
            if (!aiApplied.contains(uuid) && entity instanceof Mob mob) {
                applyDoorOnlyAi(mob, target);
                aiApplied.add(uuid);
            }
        }
        return spawnedEntities.size();
    }

    /** 清空怪物原生 AI（行为与仇恨），只保留前往蓝门的 Goal */
    private void applyDoorOnlyAi(Mob mob, BlockPos target) {
        mob.goalSelector.removeAllGoals(goal -> true);
        mob.targetSelector.removeAllGoals(goal -> true);
        mob.setPersistenceRequired();
        mob.goalSelector.addGoal(0, new MoveToBlueDoorGoal(mob, target, NAV_SPEED));
    }

    /** 处理待续任务：间隔波次逐只生成、延迟到期恢复。到期的执行链恢复统一收集后执行，避免恢复中调度新任务导致并发修改 */
    private void processPendingTasks(ServerLevel serverLevel) {
        List<String[]> dueResumes = new ArrayList<>();

        var waveIt = pendingWaves.iterator();
        while (waveIt.hasNext()) {
            PendingWave wave = waveIt.next();
            if (wave.ticksUntilNext > 0) {
                wave.ticksUntilNext--;
                continue;
            }
            boolean ok = spawnIntervalUnit(serverLevel, wave);
            if (ok) wave.remaining--;
            if (!ok || wave.remaining <= 0) {
                waveIt.remove();
                dueResumes.add(new String[]{wave.nodeUid, wave.portId});
            } else {
                wave.ticksUntilNext = wave.interval;
            }
        }

        collectDueResumes(dueResumes);
        runResumes(serverLevel, dueResumes);
    }

    /** 仅处理延迟恢复（终态下使用，不再生成间隔波次） */
    private void processPendingResumes(ServerLevel serverLevel) {
        List<String[]> dueResumes = new ArrayList<>();
        collectDueResumes(dueResumes);
        runResumes(serverLevel, dueResumes);
    }

    private void collectDueResumes(List<String[]> dueResumes) {
        var resumeIt = pendingResumes.iterator();
        while (resumeIt.hasNext()) {
            PendingResume task = resumeIt.next();
            if (--task.ticks <= 0) {
                resumeIt.remove();
                dueResumes.add(new String[]{task.nodeUid, task.portId});
            }
        }
    }

    private void runResumes(ServerLevel serverLevel, List<String[]> dueResumes) {
        for (String[] resume : dueResumes) {
            runtime.resumeChain(this, serverLevel.registryAccess(), resume[0], resume[1]);
        }
    }

    /** 生成间隔波次的单只敌人。门或实体 ID 失效时广播错误并返回 false（波次将被取消） */
    private boolean spawnIntervalUnit(ServerLevel serverLevel, PendingWave wave) {
        var typeOpt = EntityType.byString(wave.entityId);
        if (typeOpt.isEmpty()) {
            broadcast(Component.translatable(
                    "block.modularization_defend.level_controller.invalid_entity", wave.entityId));
            return false;
        }
        BlockPos redPos = DoorRegistry.findDoor(serverLevel, DoorRegistry.DoorType.RED,
                wave.redDoorId, worldPosition, DOOR_SEARCH_RANGE);
        if (redPos == null) {
            broadcast(Component.translatable(
                    "block.modularization_defend.level_controller.red_door_missing", wave.redDoorId));
            return false;
        }
        BlockPos bluePos = DoorRegistry.findDoor(serverLevel, DoorRegistry.DoorType.BLUE,
                wave.blueDoorId, worldPosition, DOOR_SEARCH_RANGE);
        if (bluePos == null) {
            broadcast(Component.translatable(
                    "block.modularization_defend.level_controller.blue_door_missing", wave.blueDoorId));
            return false;
        }
        spawnTracked(serverLevel, typeOpt.get(), redPos, bluePos);
        return true;
    }

    // ==================== 关卡状态机 ====================

    public LevelState getLevelState() {
        return state;
    }

    /** 右键切换：待机→启动，运行/终态→复位 */
    public void toggleLevel(Player player) {
        if (level == null || level.isClientSide) return;
        switch (state) {
            case IDLE -> {
                if (!runtime.hasGraph()) {
                    player.sendSystemMessage(Component.translatable(
                            "block.modularization_defend.level_controller.no_graph"));
                    return;
                }
                startLevel();
                player.sendSystemMessage(Component.translatable(
                        "block.modularization_defend.level_controller.started"));
            }
            case RUNNING, VICTORY, DEFEAT -> {
                resetLevel();
                player.sendSystemMessage(Component.translatable(
                        "block.modularization_defend.level_controller.reset"));
            }
        }
    }

    private void startLevel() {
        discardSpawned();
        runTicks = 0;
        waveCount = 0;
        leaks = 0;
        pendingResumes.clear();
        pendingWaves.clear();
        runtime.resetState();
        state = LevelState.RUNNING;
        markUpdated();
    }

    private void resetLevel() {
        discardSpawned();
        runTicks = 0;
        waveCount = 0;
        leaks = 0;
        pendingResumes.clear();
        pendingWaves.clear();
        runtime.resetState();
        state = LevelState.IDLE;
        markUpdated();
    }

    /** 移除本关生成的剩余实体 */
    private void discardSpawned() {
        if (level instanceof ServerLevel serverLevel) {
            for (UUID uuid : spawnedEntities) {
                Entity entity = serverLevel.getEntity(uuid);
                if (entity != null) entity.discard();
            }
        }
        spawnedEntities.clear();
        entityTargets.clear();
        aiApplied.clear();
    }

    // ==================== ILevelHost（动作节点副作用） ====================

    @Override
    public void spawnWave(String entityId, int count, int redDoorId, int blueDoorId) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        var typeOpt = EntityType.byString(entityId);
        if (typeOpt.isEmpty()) {
            broadcast(Component.translatable(
                    "block.modularization_defend.level_controller.invalid_entity", entityId));
            return;
        }
        BlockPos redPos = DoorRegistry.findDoor(serverLevel, DoorRegistry.DoorType.RED,
                redDoorId, worldPosition, DOOR_SEARCH_RANGE);
        if (redPos == null) {
            broadcast(Component.translatable(
                    "block.modularization_defend.level_controller.red_door_missing", redDoorId));
            return;
        }
        BlockPos bluePos = DoorRegistry.findDoor(serverLevel, DoorRegistry.DoorType.BLUE,
                blueDoorId, worldPosition, DOOR_SEARCH_RANGE);
        if (bluePos == null) {
            broadcast(Component.translatable(
                    "block.modularization_defend.level_controller.blue_door_missing", blueDoorId));
            return;
        }

        waveCount++;
        for (int i = 0; i < count; i++) {
            spawnTracked(serverLevel, typeOpt.get(), redPos, bluePos);
        }
        setChanged();
    }

    /** 在红门底面 3x3 内随机落点生成一只敌人并纳入追踪，目标设为指定蓝门 */
    private void spawnTracked(ServerLevel serverLevel, EntityType<?> type, BlockPos redPos, BlockPos bluePos) {
        var random = serverLevel.getRandom();
        BlockPos spawnPos = redPos.offset(random.nextInt(3) - 1, 0, random.nextInt(3) - 1);
        Entity entity = type.spawn(serverLevel, spawnPos, MobSpawnType.COMMAND);
        if (entity != null) {
            spawnedEntities.add(entity.getUUID());
            entityTargets.put(entity.getUUID(), bluePos);
            if (entity instanceof Mob mob) {
                applyDoorOnlyAi(mob, bluePos);
                aiApplied.add(entity.getUUID());
            }
        }
    }

    @Override
    public void scheduleResume(String nodeUid, String portId, int delayTicks) {
        PendingResume task = new PendingResume();
        task.nodeUid = nodeUid;
        task.portId = portId;
        task.ticks = Math.max(1, delayTicks);
        pendingResumes.add(task);
        setChanged();
    }

    @Override
    public void scheduleIntervalWave(String entityId, int count, int intervalTicks,
                                     int redDoorId, int blueDoorId, String resumeNodeUid, String resumePortId) {
        PendingWave wave = new PendingWave();
        wave.entityId = entityId;
        wave.remaining = count;
        wave.interval = Math.max(1, intervalTicks);
        wave.ticksUntilNext = 0;
        wave.redDoorId = redDoorId;
        wave.blueDoorId = blueDoorId;
        wave.nodeUid = resumeNodeUid;
        wave.portId = resumePortId;
        pendingWaves.add(wave);
        waveCount++;
        setChanged();
    }

    @Override
    public void broadcastMessage(String message) {
        broadcast(Component.literal(message));
    }

    @Override
    public void giveReward(String itemId, int count) {
        ResourceLocation id = ResourceLocation.tryParse(itemId);
        if (id == null) return;
        Item item = BuiltInRegistries.ITEM.get(id);
        if (item == Items.AIR) return;
        for (ServerPlayer player : nearbyPlayers()) {
            player.getInventory().placeItemBackInInventory(new ItemStack(item, count));
        }
    }

    @Override
    public void setLevelResult(boolean victory) {
        if (state != LevelState.RUNNING) return;
        state = victory ? LevelState.VICTORY : LevelState.DEFEAT;
        broadcast(Component.translatable(victory
                ? "block.modularization_defend.level_controller.victory"
                : "block.modularization_defend.level_controller.defeat"));
        markUpdated();
    }

    @Override
    public void resetLevelFromGraph(boolean restart) {
        resetLevel();
        broadcast(Component.translatable("block.modularization_defend.level_controller.reset"));
        if (restart && runtime.hasGraph()) {
            startLevel();
            broadcast(Component.translatable("block.modularization_defend.level_controller.started"));
        }
    }

    private void broadcast(Component message) {
        nearbyPlayers().forEach(player -> player.sendSystemMessage(message));
    }

    private List<ServerPlayer> nearbyPlayers() {
        if (!(level instanceof ServerLevel serverLevel)) return List.of();
        Vec3 center = Vec3.atCenterOf(worldPosition);
        return serverLevel.players().stream()
                .filter(player -> player.distanceToSqr(center) < PLAYER_RANGE * PLAYER_RANGE)
                .toList();
    }

    // ==================== 图数据管理 ====================

    public CompoundTag getGraphTag() {
        return graphTag;
    }

    public boolean hasGraph() {
        return !graphTag.isEmpty();
    }

    /** 写入新图（编辑器保存或存储卡导入）。运行中修改图会复位关卡 */
    public void setGraph(CompoundTag tag) {
        graphTag = tag != null ? tag : new CompoundTag();
        runtime.setGraphData(graphTag);
        if (state == LevelState.RUNNING) {
            resetLevel();
        } else {
            markUpdated();
        }
    }

    /** 从程序存储卡导入图，成功返回 true */
    public boolean importGraphFromCard(ItemStack stack) {
        if (!ProgramStorageCardItem.hasGraphData(stack)) return false;
        setGraph(ProgramStorageCardItem.getGraphTag(stack).copy());
        return true;
    }

    /** 标记脏并同步到客户端（图数据供编辑器打开时读取） */
    private void markUpdated() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    // ==================== NBT 持久化 ====================

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        if (!graphTag.isEmpty()) {
            tag.putByteArray("GraphData", DataCompressUtil.compress(tagToBytes(graphTag)));
        }
        tag.putString("State", state.name());
        tag.putLong("RunTicks", runTicks);
        tag.putInt("WaveCount", waveCount);
        tag.putInt("Leaks", leaks);
        ListTag spawned = new ListTag();
        spawnedEntities.forEach(uuid -> spawned.add(NbtUtils.createUUID(uuid)));
        tag.put("Spawned", spawned);
        ListTag targets = new ListTag();
        entityTargets.forEach((uuid, pos) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("Entity", uuid);
            entry.putLong("Door", pos.asLong());
            targets.add(entry);
        });
        tag.put("EntityTargets", targets);
        ListTag resumeList = new ListTag();
        for (PendingResume task : pendingResumes) {
            CompoundTag entry = new CompoundTag();
            entry.putString("Node", task.nodeUid);
            entry.putString("Port", task.portId);
            entry.putInt("Ticks", task.ticks);
            resumeList.add(entry);
        }
        tag.put("PendingResumes", resumeList);
        ListTag waveList = new ListTag();
        for (PendingWave wave : pendingWaves) {
            CompoundTag entry = new CompoundTag();
            entry.putString("Entity", wave.entityId);
            entry.putInt("Remaining", wave.remaining);
            entry.putInt("Interval", wave.interval);
            entry.putInt("Next", wave.ticksUntilNext);
            entry.putInt("RedDoor", wave.redDoorId);
            entry.putInt("BlueDoor", wave.blueDoorId);
            entry.putString("Node", wave.nodeUid);
            entry.putString("Port", wave.portId);
            waveList.add(entry);
        }
        tag.put("PendingWaves", waveList);
        tag.put("Runtime", runtime.saveState());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        graphTag = tag.contains("GraphData")
                ? bytesToTag(DataCompressUtil.decompress(tag.getByteArray("GraphData")))
                : new CompoundTag();
        runtime.setGraphData(graphTag);
        state = LevelState.byName(tag.getString("State"));
        runTicks = tag.getLong("RunTicks");
        waveCount = tag.getInt("WaveCount");
        leaks = tag.getInt("Leaks");
        spawnedEntities.clear();
        for (Tag entry : tag.getList("Spawned", Tag.TAG_INT_ARRAY)) {
            spawnedEntities.add(NbtUtils.loadUUID(entry));
        }
        entityTargets.clear();
        for (Tag entry : tag.getList("EntityTargets", Tag.TAG_COMPOUND)) {
            CompoundTag compound = (CompoundTag) entry;
            entityTargets.put(compound.getUUID("Entity"), BlockPos.of(compound.getLong("Door")));
        }
        pendingResumes.clear();
        for (Tag entry : tag.getList("PendingResumes", Tag.TAG_COMPOUND)) {
            CompoundTag compound = (CompoundTag) entry;
            PendingResume task = new PendingResume();
            task.nodeUid = compound.getString("Node");
            task.portId = compound.getString("Port");
            task.ticks = compound.getInt("Ticks");
            pendingResumes.add(task);
        }
        pendingWaves.clear();
        for (Tag entry : tag.getList("PendingWaves", Tag.TAG_COMPOUND)) {
            CompoundTag compound = (CompoundTag) entry;
            PendingWave wave = new PendingWave();
            wave.entityId = compound.getString("Entity");
            wave.remaining = compound.getInt("Remaining");
            wave.interval = compound.getInt("Interval");
            wave.ticksUntilNext = compound.getInt("Next");
            wave.redDoorId = compound.getInt("RedDoor");
            wave.blueDoorId = compound.getInt("BlueDoor");
            wave.nodeUid = compound.getString("Node");
            wave.portId = compound.getString("Port");
            pendingWaves.add(wave);
        }
        runtime.loadState(tag.getCompound("Runtime"));
    }

    // ==================== 客户端同步 ====================

    @Override
    @NotNull
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return saveWithoutMetadata(provider);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // ==================== NBT 字节序列化 ====================

    private static byte[] tagToBytes(CompoundTag tag) {
        try {
            var bos = new ByteArrayOutputStream();
            NbtIo.write(tag, new DataOutputStream(bos));
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("图 NBT 序列化失败", e);
        }
    }

    private static CompoundTag bytesToTag(byte[] bytes) {
        try {
            return NbtIo.read(new DataInputStream(new ByteArrayInputStream(bytes)),
                    NbtAccounter.unlimitedHeap());
        } catch (IOException e) {
            throw new RuntimeException("图 NBT 反序列化失败", e);
        }
    }
}
