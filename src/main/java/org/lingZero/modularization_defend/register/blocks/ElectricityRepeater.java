package org.lingZero.modularization_defend.register.blocks;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

import static org.lingZero.modularization_defend.register.items.ModItems.ITEMS;

public class ElectricityRepeater {
    public static final DeferredBlock<Block> ELECTRICITY_REPEATER = BlocksRegister.BLOCKS.register("electricity_repeater",
            registryName -> new Block(BlockBehaviour.Properties.of()
                    .strength(3.0f, 6.0f)      // 硬度等级与抗爆等级
                    .sound(SoundType.STONE)                                // 挖掘时的声音类型
                    .lightLevel(state -> 10)                     // 发光等级（可选）
                    .requiresCorrectToolForDrops()                         // 需要合适工具才能掉落对应的物品（可选）
            )
    );

    public static final DeferredItem<BlockItem> ELECTRICITY_REPEATER_ITEM = ITEMS.registerSimpleBlockItem("electricity_repeater", ELECTRICITY_REPEATER);
}
