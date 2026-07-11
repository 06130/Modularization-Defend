package org.lingZero.modularization_defend.Block.example;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.lingZero.modularization_defend.Block.ModBlockEntities;
import org.lingZero.modularization_defend.Block.bounding.IBoundingBlock;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 幽灵多方块结构的BlockEntity。
 * 维护一个实体类型 ID 集合，可由实体选取器写入/清除。
 */
public class GhostMultiblockBlockEntity extends BlockEntity implements IBoundingBlock {

    private final Set<ResourceLocation> entityIds = new HashSet<>();

    public GhostMultiblockBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GHOST_MULTIBLOCK.get(), pos, state);
    }

    /**
     * 获取已存储的实体 ID 集合（只读）。
     */
    public Set<ResourceLocation> getEntityIds() {
        return Collections.unmodifiableSet(entityIds);
    }

    /**
     * 向实体列表中添加一个实体 ID。
     */
    public void addEntityId(ResourceLocation entityId) {
        entityIds.add(entityId);
        setChanged();
    }

    /**
     * 清空实体列表。
     */
    public void clearEntityIds() {
        entityIds.clear();
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        ListTag list = new ListTag();
        for (ResourceLocation id : entityIds) {
            list.add(StringTag.valueOf(id.toString()));
        }
        tag.put("EntityIds", list);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
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
