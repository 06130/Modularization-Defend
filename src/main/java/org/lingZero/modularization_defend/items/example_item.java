package org.lingZero.modularization_defend.items;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.function.Supplier;

import static org.lingZero.modularization_defend.modularization_defend.ITEMS;

public class example_item {
    public static final Supplier<Item> EXAMPLE_ITEM = ITEMS.registerSimpleItem("example_item", new Item.Properties());
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

}
