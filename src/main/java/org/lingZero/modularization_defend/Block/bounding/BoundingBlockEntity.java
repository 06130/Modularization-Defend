package org.lingZero.modularization_defend.Block.bounding;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.lingZero.modularization_defend.Block.ModBlockEntities;

/**
 * 占位方块的BlockEntity。存储指向主方块坐标的引用，
 * 所有交互（右键、破坏、红石等）均代理到主方块。
 */
public class BoundingBlockEntity extends BlockEntity {

    @Nullable
    private BlockPos mainPos;

    public BoundingBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BOUNDING_BLOCK.get(), pos, state);
    }

    /**
     * 设置该占位方块指向的主方块坐标。
     */
    public void setMainPosition(BlockPos pos) {
        this.mainPos = pos;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    /**
     * 获取主方块坐标，可能为null。
     */
    @Nullable
    public BlockPos getMainPos() {
        return mainPos;
    }

    /**
     * 获取主方块的BlockEntity（已转换为IBoundingBlock接口）。
     * 如果主方块不存在或主方块BE未加载，返回null。
     */
    @Nullable
    public IBoundingBlock getMainBlockEntity() {
        if (mainPos == null || level == null) {
            return null;
        }
        BlockEntity be = level.getBlockEntity(mainPos);
        if (be instanceof IBoundingBlock bb) {
            return bb;
        }
        return null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        if (mainPos != null) {
            tag.put("MainPos", NbtUtils.writeBlockPos(mainPos));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        NbtUtils.readBlockPos(tag, "MainPos").ifPresent(pos -> mainPos = pos);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = super.getUpdateTag(provider);
        if (mainPos != null) {
            tag.put("MainPos", NbtUtils.writeBlockPos(mainPos));
        }
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        super.handleUpdateTag(tag, provider);
        NbtUtils.readBlockPos(tag, "MainPos").ifPresent(pos -> mainPos = pos);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
