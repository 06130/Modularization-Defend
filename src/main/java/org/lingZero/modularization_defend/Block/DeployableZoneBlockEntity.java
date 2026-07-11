package org.lingZero.modularization_defend.Block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.lingZero.modularization_defend.util.TickCounter;

/**
 * 可部署区的BlockEntity。
 */
public class DeployableZoneBlockEntity extends BlockEntity {

    private final TickCounter tickCounter = new TickCounter();

    public DeployableZoneBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DEPLOYABLE_ZONE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DeployableZoneBlockEntity be) {
        be.tickCounter.increment();
    }

    public long getTickCount() {
        return tickCounter.getCount();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tickCounter.saveToTag(tag, "TickCount");
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        tickCounter.loadFromTag(tag, "TickCount");
    }
}
