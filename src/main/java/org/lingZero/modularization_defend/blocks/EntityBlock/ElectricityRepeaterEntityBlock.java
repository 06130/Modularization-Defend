package org.lingZero.modularization_defend.blocks.EntityBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.lingZero.modularization_defend.blocks.BlockEntity.ElectricityRepeaterBlockEntity;

public class ElectricityRepeaterEntityBlock extends Block implements EntityBlock {
    // Constructor deferring to super.
    public ElectricityRepeaterEntityBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ElectricityRepeaterBlockEntity(pos, state);
    }
}
