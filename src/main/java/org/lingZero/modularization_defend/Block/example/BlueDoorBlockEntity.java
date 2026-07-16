package org.lingZero.modularization_defend.Block.example;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.lingZero.modularization_defend.Block.ModBlockEntities;
import org.lingZero.modularization_defend.Block.bounding.IBoundingBlock;
import org.lingZero.modularization_defend.level.DoorRegistry;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 蓝门多方块结构的BlockEntity。
 * 实现GeoBlockEntity以提供GeckoLib动画视觉效果，灵感来源于明日方舟"蓝门"。
 * 维护一个实体类型ID集合，结构范围内的匹配实体将被定期清除。
 */
public class BlueDoorBlockEntity extends BlockEntity implements IBoundingBlock, GeoBlockEntity {

    /** 结构范围（3x3x3，主方块在底面中心） */
    public static final AABB STRUCTURE_AABB = new AABB(-1, 0, -1, 2, 3, 2);

    /** 实体扫描间隔（tick） */
    private static final int SCAN_INTERVAL = 10;

    /** GeckoLib动画实例缓存（客户端侧自动管理） */
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private final Set<ResourceLocation> entityIds = new HashSet<>();
    private int tickCounter;
    /** 关卡门 ID（1~9），关卡节点图通过它引用此门 */
    private int doorId = 1;

    public BlueDoorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BLUE_DOOR.get(), pos, state);
    }

    // ==================== GeckoLib（无动画） ====================

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // 蓝门不使用呼吸动画
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // ==================== 服务端Tick ====================

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlueDoorBlockEntity be) {
        if (be.entityIds.isEmpty()) {
            return;
        }
        be.tickCounter++;
        if (be.tickCounter < SCAN_INTERVAL) {
            return;
        }
        be.tickCounter = 0;
        be.scanAndRemoveEntities(level, pos);
    }

    /** 扫描结构范围内的实体，匹配到的直接移除 */
    private void scanAndRemoveEntities(Level level, BlockPos pos) {
        AABB worldBounds = STRUCTURE_AABB.move(pos);
        List<Entity> entities = level.getEntities((Entity) null, worldBounds, e -> !(e instanceof Player));
        for (Entity entity : entities) {
            ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
            if (entityIds.contains(entityId)) {
                entity.discard();
            }
        }
    }

    // ==================== 关卡门 ID ====================

    public int getDoorId() {
        return doorId;
    }

    /** 循环切换门 ID（1→9→1），返回新 ID */
    public int cycleDoorId() {
        doorId = doorId % 9 + 1;
        setChanged();
        return doorId;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            DoorRegistry.register(level, DoorRegistry.DoorType.BLUE, worldPosition);
        }
    }

    @Override
    public void setRemoved() {
        if (level != null && !level.isClientSide) {
            DoorRegistry.unregister(level, DoorRegistry.DoorType.BLUE, worldPosition);
        }
        super.setRemoved();
    }

    // ==================== 实体ID列表管理 ====================

    /** 获取已存储的实体 ID 集合（只读） */
    public Set<ResourceLocation> getEntityIds() {
        return Collections.unmodifiableSet(entityIds);
    }

    /** 向实体列表中添加一个实体 ID */
    public void addEntityId(ResourceLocation entityId) {
        entityIds.add(entityId);
        setChanged();
    }

    /** 清空实体列表 */
    public void clearEntityIds() {
        entityIds.clear();
        setChanged();
    }

    // ==================== NBT持久化 ====================

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("TickCounter", tickCounter);
        tag.putInt("DoorId", doorId);
        ListTag list = new ListTag();
        for (ResourceLocation id : entityIds) {
            list.add(StringTag.valueOf(id.toString()));
        }
        tag.put("EntityIds", list);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        tickCounter = tag.getInt("TickCounter");
        doorId = tag.getInt("DoorId");
        if (doorId <= 0) doorId = 1;
        entityIds.clear();
        ListTag list = tag.getList("EntityIds", Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            ResourceLocation id = ResourceLocation.tryParse(list.getString(i));
            if (id != null) {
                entityIds.add(id);
            }
        }
    }
}
