package org.lingZero.m_defend.Register;


import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.lingZero.m_defend.Blocks.Multiblock.AffiliateBlockEntity;
import org.lingZero.m_defend.Blocks.Multiblock.Turret1BlockEntity;
import org.lingZero.m_defend.Blocks.UpgradeCraftingTable;


import static org.lingZero.m_defend.ModularizationDefend.MODID;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(
                    net.minecraft.core.registries.BuiltInRegistries.BLOCK_ENTITY_TYPE, MODID);
    
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
    
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AffiliateBlockEntity>> AFFILIATE_BLOCK_ENTITY = BLOCK_ENTITIES.register(
            "affiliate_block_entity",
            () -> {
                var block = ModBlocks.AFFILIATE_BLOCK.get();
                return BlockEntityType.Builder.of(
                                AffiliateBlockEntity::new,
                                block
                        )
                        .build(null);
            }
    );
    
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<Turret1BlockEntity>> TURRET1_BLOCK_ENTITY = BLOCK_ENTITIES.register(
            "turret1_block_entity",
            () -> {
                var block = ModBlocks.TURRET1_BLOCK.get();
                return BlockEntityType.Builder.of(
                                Turret1BlockEntity::new,
                                block
                        )
                        .build(null);
            }
    );
}
