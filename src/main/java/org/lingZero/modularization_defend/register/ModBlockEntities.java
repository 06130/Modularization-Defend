package org.lingZero.modularization_defend.register;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;


import static org.lingZero.modularization_defend.modularization_defend.MODID;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(
                    net.minecraft.core.registries.BuiltInRegistries.BLOCK_ENTITY_TYPE, MODID);

}
