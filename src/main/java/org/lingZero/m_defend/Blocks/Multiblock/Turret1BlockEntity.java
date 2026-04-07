package org.lingZero.m_defend.Blocks.Multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.lingZero.m_defend.Register.ModBlockEntities;

public class Turret1BlockEntity extends BlockEntity {
    public Turret1BlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TURRET1_BLOCK_ENTITY.get(), pos, state);
    }
}
