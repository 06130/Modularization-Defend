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
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 红门多方块结构的BlockEntity。
 * 实现GeoBlockEntity以提供GeckoLib渲染（无动画），
 * 维护实体类型ID集合，结构范围内的匹配实体将被定期清除。
 */
public class RedDoorBlockEntity extends BlockEntity implements IBoundingBlock, GeoBlockEntity {

    private static final AABB STRUCTURE_AABB = new AABB(-1, 0, -1, 2, 3, 2);
    private static final int SCAN_INTERVAL = 10;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private final Set<ResourceLocation> entityIds = new HashSet<>();
    private int tickCounter;

    public RedDoorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RED_DOOR.get(), pos, state);
    }

    // ==================== GeckoLib（无动画） ====================

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // 红门不使用呼吸动画
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // ==================== 服务端Tick ====================

    public static void serverTick(Level level, BlockPos pos, BlockState state, RedDoorBlockEntity be) {
        if (be.entityIds.isEmpty()) return;
        be.tickCounter++;
        if (be.tickCounter < SCAN_INTERVAL) return;
        be.tickCounter = 0;
        be.scanAndRemoveEntities(level, pos);
    }

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

    public Set<ResourceLocation> getEntityIds() {
        return Collections.unmodifiableSet(entityIds);
    }

    public void addEntityId(ResourceLocation entityId) {
        entityIds.add(entityId);
        setChanged();
    }

    public void clearEntityIds() {
        entityIds.clear();
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("TickCounter", tickCounter);
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
