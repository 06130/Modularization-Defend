package org.lingZero.modularization_defend.blocks.BlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import static org.lingZero.modularization_defend.register.ModBlockEntities.Electricity_Repeater_BLOCK_ENTITY;


public class ElectricityRepeaterBlockEntity extends BlockEntity  {

    public ElectricityRepeaterBlockEntity(BlockPos pos, BlockState state) {
        super(Electricity_Repeater_BLOCK_ENTITY.get(), pos, state);
    }
}
