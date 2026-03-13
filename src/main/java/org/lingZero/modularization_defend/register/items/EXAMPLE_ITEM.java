package org.lingZero.modularization_defend.register.items;

import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public class EXAMPLE_ITEM {
    public static final Supplier<Item> EXAMPLE_ITEM = ModItems.ITEMS.registerSimpleItem("example_item", new Item.Properties());
}
