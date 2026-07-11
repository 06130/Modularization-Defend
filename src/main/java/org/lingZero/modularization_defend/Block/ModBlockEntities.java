package org.lingZero.modularization_defend.Block;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.lingZero.modularization_defend.Block.bounding.BoundingBlockEntity;
import org.lingZero.modularization_defend.Block.example.CombatConsoleBlockEntity;
import org.lingZero.modularization_defend.Block.example.GhostMultiblockBlockEntity;

/**
 * 模组 BlockEntity 类型注册中心。
 */
public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "modularization_defend");

    /**
     * 占位方块的 BlockEntity 类型。
     */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BoundingBlockEntity>> BOUNDING_BLOCK =
            BLOCK_ENTITIES.register("bounding_block",
                    () -> BlockEntityType.Builder.of(BoundingBlockEntity::new, ModBlocks.BOUNDING_BLOCK.get()).build(null));

    /**
     * 作战控制台的 BlockEntity 类型。
     */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CombatConsoleBlockEntity>> COMBAT_CONSOLE =
            BLOCK_ENTITIES.register("combat_console",
                    () -> BlockEntityType.Builder.of(CombatConsoleBlockEntity::new, ModBlocks.COMBAT_CONSOLE.get()).build(null));

    /**
     * 幽灵多方块结构的 BlockEntity 类型。
     */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GhostMultiblockBlockEntity>> GHOST_MULTIBLOCK =
            BLOCK_ENTITIES.register("ghost_multiblock",
                    () -> BlockEntityType.Builder.of(GhostMultiblockBlockEntity::new, ModBlocks.GHOST_MULTIBLOCK.get()).build(null));
}
