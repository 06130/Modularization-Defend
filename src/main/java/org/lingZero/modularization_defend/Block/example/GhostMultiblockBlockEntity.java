package org.lingZero.modularization_defend.Block.example;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.lingZero.modularization_defend.Block.ModBlockEntities;
import org.lingZero.modularization_defend.Block.bounding.IBoundingBlock;

/**
 * 幽灵多方块结构的BlockEntity。
 * 记录被右键点击的次数，展示"无碰撞但可交互"的特性。
 */
public class GhostMultiblockBlockEntity extends BlockEntity implements IBoundingBlock {

    private int clickCount;

    public GhostMultiblockBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GHOST_MULTIBLOCK.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, GhostMultiblockBlockEntity be) {
    }

    public int getClickCount() {
        return clickCount;
    }

    public void incrementClickCount() {
        clickCount++;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("ClickCount", clickCount);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        clickCount = tag.getInt("ClickCount");
    }
}
