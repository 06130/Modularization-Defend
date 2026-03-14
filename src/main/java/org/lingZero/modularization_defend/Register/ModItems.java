package org.lingZero.modularization_defend.Register;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.lingZero.modularization_defend.Items.EnergyNetworkConfigurationToolItem;
import org.lingZero.modularization_defend.Items.TerminalItem;
import org.lingZero.modularization_defend.ModularizationDefend;

import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(ModularizationDefend.MODID);

    public static final DeferredItem<BlockItem> ELECTRICITY_REPEATER_ITEM =
            ITEMS.registerSimpleBlockItem("electricity_repeater_item", ModBlocks.ELECTRICITY_REPEATER_BLOCK);

    public static final Supplier<Item> EXAMPLE_ITEM =
            ITEMS.registerSimpleItem("example_item", new Item.Properties());

    public static final Supplier<Item> TERMINAL_ITEM =
            ITEMS.registerItem("terminal_item", TerminalItem::new, new Item.Properties()
    );
    public static final Supplier<Item> ENERGY_NETWORK_CONFIGURATION_TOOL_ITEM =
            ITEMS.registerItem("energy_network_configuration_tool_item", EnergyNetworkConfigurationToolItem::new, new Item.Properties());
}
