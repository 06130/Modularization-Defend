package org.lingZero.modularization_defend.Block.example;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.lingZero.modularization_defend.Block.ModBlockEntities;
import org.lingZero.modularization_defend.Block.bounding.IBoundingBlock;
import org.lingZero.modularization_defend.util.TickCounter;

/**
 * 作战控制台的BlockEntity。
 * 使用独立的TickCounter类管理tick计数，实现IBoundingBlock以接收来自占位方块的代理调用。
 */
public class CombatConsoleBlockEntity extends BlockEntity implements IBoundingBlock {

    private final TickCounter tickCounter = new TickCounter();

    public CombatConsoleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COMBAT_CONSOLE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CombatConsoleBlockEntity be) {
        be.tickCounter.increment();
    }

    public TickCounter getTickCounter() { return tickCounter; }
    public long getTickCount() { return tickCounter.getCount(); }

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
