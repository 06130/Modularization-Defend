package org.lingZero.modularization_defend.Block;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.lingZero.modularization_defend.Block.bounding.BoundingBlockEntity;
import org.lingZero.modularization_defend.Block.example.ExampleMultiblockBlockEntity;
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
     * 示例多方块结构的 BlockEntity 类型。
     */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExampleMultiblockBlockEntity>> EXAMPLE_MULTIBLOCK =
            BLOCK_ENTITIES.register("example_multiblock",
                    () -> BlockEntityType.Builder.of(ExampleMultiblockBlockEntity::new, ModBlocks.EXAMPLE_MULTIBLOCK.get()).build(null));

    /**
     * 幽灵多方块结构的 BlockEntity 类型。
     */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GhostMultiblockBlockEntity>> GHOST_MULTIBLOCK =
            BLOCK_ENTITIES.register("ghost_multiblock",
                    () -> BlockEntityType.Builder.of(GhostMultiblockBlockEntity::new, ModBlocks.GHOST_MULTIBLOCK.get()).build(null));
}
