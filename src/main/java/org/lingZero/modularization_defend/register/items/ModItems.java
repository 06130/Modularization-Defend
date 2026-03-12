package org.lingZero.modularization_defend.register.items;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.lingZero.modularization_defend.modularization_defend;

import java.util.function.Supplier;


public class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(modularization_defend.MODID);

    public static final Supplier<Item> EXAMPLE_ITEM = ITEMS.registerSimpleItem("example_item", new Item.Properties());

}
