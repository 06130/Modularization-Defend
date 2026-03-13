package org.lingZero.modularization_defend.register;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.lingZero.modularization_defend.modularization_defend;

import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(modularization_defend.MODID);

    public static final DeferredItem<BlockItem> ELECTRICITY_REPEATER_ITEM =
            ITEMS.registerSimpleBlockItem("electricity_repeater_item", ModBlocks.ELECTRICITY_REPEATER_BLOCK);

    public static final Supplier<Item> EXAMPLE_ITEM =
            ITEMS.registerSimpleItem("example_item", new Item.Properties());

    public static final Supplier<Item> TERMINAL_ITEM =
            ITEMS.registerItem("terminal_item", Item::new, new Item.Properties()
    );
}
