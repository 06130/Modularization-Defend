package org.lingZero.modularization_defend.blocks.BlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import static org.lingZero.modularization_defend.register.ModBlockEntities.Electricity_Repeater_BLOCK_ENTITY;


public class ElectricityRepeaterBlockEntity extends BlockEntity  {
    private int value;
    public ElectricityRepeaterBlockEntity(BlockPos pos, BlockState state) {
        super(Electricity_Repeater_BLOCK_ENTITY.get(), pos, state);
    }



    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        // Will default to 0 if absent. See the NBT article for more information.
        this.value = tag.getInt("value");
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("value", this.value);
    }
}
