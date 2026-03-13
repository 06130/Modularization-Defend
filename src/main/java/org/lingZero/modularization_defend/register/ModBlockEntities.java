package org.lingZero.modularization_defend.register;


import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.lingZero.modularization_defend.blocks.BlockEntity.ElectricityRepeaterBlockEntity;


import static org.lingZero.modularization_defend.modularization_defend.MODID;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(
                    net.minecraft.core.registries.BuiltInRegistries.BLOCK_ENTITY_TYPE, MODID);


    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ElectricityRepeaterBlockEntity>> Electricity_Repeater_BLOCK_ENTITY = BLOCK_ENTITIES.register(
            "electricity_repeater_block_entity",
            () -> BlockEntityType.Builder.of(
                            ElectricityRepeaterBlockEntity::new,
                            ModBlocks.ELECTRICITY_REPEATER_BLOCK.get()
                    )
                    .build(null)
    );
}
