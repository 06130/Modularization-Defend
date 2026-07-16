package org.lingZero.modularization_defend.Block;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.lingZero.modularization_defend.Block.bounding.BoundingBlockEntity;
import org.lingZero.modularization_defend.Block.example.BlueDoorBlockEntity;
import org.lingZero.modularization_defend.Block.example.CombatConsoleBlockEntity;
import org.lingZero.modularization_defend.Block.DeployableZoneBlockEntity;
import org.lingZero.modularization_defend.Block.example.DeployableZoneHighBlockEntity;
import org.lingZero.modularization_defend.Block.example.LevelEditorBlockEntity;
import org.lingZero.modularization_defend.Block.example.RedDoorBlockEntity;
import org.lingZero.modularization_defend.level.LevelControllerBlockEntity;

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
     * 蓝门多方块结构的 BlockEntity 类型。
     */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlueDoorBlockEntity>> BLUE_DOOR =
            BLOCK_ENTITIES.register("bluedoor",
                    () -> BlockEntityType.Builder.of(BlueDoorBlockEntity::new, ModBlocks.BLUE_DOOR.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DeployableZoneBlockEntity>> DEPLOYABLE_ZONE =
            BLOCK_ENTITIES.register("deployable_zone",
                    () -> BlockEntityType.Builder.of(DeployableZoneBlockEntity::new, ModBlocks.DEPLOYABLE_ZONE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DeployableZoneHighBlockEntity>> DEPLOYABLE_ZONE_HIGH =
            BLOCK_ENTITIES.register("deployable_zone_high",
                    () -> BlockEntityType.Builder.of(DeployableZoneHighBlockEntity::new, ModBlocks.DEPLOYABLE_ZONE_HIGH.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RedDoorBlockEntity>> RED_DOOR =
            BLOCK_ENTITIES.register("reddoor",
                    () -> BlockEntityType.Builder.of(RedDoorBlockEntity::new, ModBlocks.RED_DOOR.get()).build(null));

    /**
     * 关卡编辑器的 BlockEntity 类型。
     */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LevelEditorBlockEntity>> LEVEL_EDITOR =
            BLOCK_ENTITIES.register("level_editor",
                    () -> BlockEntityType.Builder.of(LevelEditorBlockEntity::new, ModBlocks.LEVEL_EDITOR.get()).build(null));

    /**
     * 关卡控制器的 BlockEntity 类型。
     */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LevelControllerBlockEntity>> LEVEL_CONTROLLER =
            BLOCK_ENTITIES.register("level_controller",
                    () -> BlockEntityType.Builder.of(LevelControllerBlockEntity::new, ModBlocks.LEVEL_CONTROLLER.get()).build(null));
}
