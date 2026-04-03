package org.lingZero.modularization_defend.Register;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.lingZero.modularization_defend.Items.DefendCore;
import org.lingZero.modularization_defend.ModularizationDefend;

import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(ModularizationDefend.MODID);

    public static final DeferredItem<BlockItem> ELECTRICITY_REPEATER_ITEM =
            ITEMS.registerSimpleBlockItem("electricity_repeater_item", ModBlocks.ELECTRICITY_REPEATER_BLOCK);
    
    public static final DeferredItem<BlockItem> AGREEMENT_CORE_ITEM =
            ITEMS.registerSimpleBlockItem("agreement_core_item", ModBlocks.AGREEMENT_CORE_BLOCK);

    public static final Supplier<Item> EXAMPLE_ITEM =
            ITEMS.registerSimpleItem("example_item", new Item.Properties());

    public static final Supplier<Item> TERMINAL_ITEM =
            ITEMS.registerSimpleItem("terminal_item", new Item.Properties()
    );
    public static final DeferredItem<DefendCore> DEFEND_CORE =
            ITEMS.registerItem(
                    "defend_core",
                    DefendCore::new,
                    new Item.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.EPIC)
            );
}
