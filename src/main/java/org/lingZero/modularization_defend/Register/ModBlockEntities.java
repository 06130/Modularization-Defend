package org.lingZero.modularization_defend.Register;


import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.lingZero.modularization_defend.Blocks.Multiblock.ElectricityRepeater.ElectricityRepeaterBlockEntity;
import org.lingZero.modularization_defend.Blocks.Multiblock.AgreementCore.AgreementCoreBlockEntity;
import org.lingZero.modularization_defend.Blocks.UpgradeCraftingTable;


import static org.lingZero.modularization_defend.ModularizationDefend.MODID;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(
                    net.minecraft.core.registries.BuiltInRegistries.BLOCK_ENTITY_TYPE, MODID);


    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ElectricityRepeaterBlockEntity>> Electricity_Repeater_BLOCK_ENTITY = BLOCK_ENTITIES.register(
            "electricity_repeater_block_entity",
            () -> {
                // 延迟获取方块，避免类加载时的循环依赖
                var block = ModBlocks.ELECTRICITY_REPEATER_BLOCK.get();
                return BlockEntityType.Builder.of(
                                ElectricityRepeaterBlockEntity::new,
                                block
                        )
                        .build(null);
            }
    );
    
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AgreementCoreBlockEntity>> AGREEMENT_CORE_BLOCK_ENTITY = BLOCK_ENTITIES.register(
            "agreement_core_block_entity",
            () -> {
                var block = ModBlocks.AGREEMENT_CORE_BLOCK.get();
                return BlockEntityType.Builder.of(
                                AgreementCoreBlockEntity::new,
                                block
                        )
                        .build(null);
            }
    );
    
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<UpgradeCraftingTable>> UPGRADE_CRAFTING_TABLE_ENTITY = BLOCK_ENTITIES.register(
            "upgrade_crafting_table_entity",
            () -> {
                var block = ModBlocks.UPGRADE_CRAFTING_TABLE.get();
                return BlockEntityType.Builder.of(
                                UpgradeCraftingTable::new,
                                block
                        )
                        .build(null);
            }
    );
}
