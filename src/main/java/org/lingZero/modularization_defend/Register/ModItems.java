package org.lingZero.modularization_defend.Register;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.lingZero.modularization_defend.DataComponents.DefendCoreData;
import org.lingZero.modularization_defend.DataComponents.TurretCoreData;
import org.lingZero.modularization_defend.Items.DefendCore;
import org.lingZero.modularization_defend.Items.TurretCore.TurretCore;
import org.lingZero.modularization_defend.ModularizationDefend;

import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(ModularizationDefend.MODID);

    public static final DeferredItem<BlockItem> ELECTRICITY_REPEATER_ITEM =
            ITEMS.registerSimpleBlockItem("electricity_repeater_item", ModBlocks.ELECTRICITY_REPEATER_BLOCK);
    
    public static final DeferredItem<BlockItem> AGREEMENT_CORE_ITEM =
            ITEMS.registerSimpleBlockItem("agreement_core_item", ModBlocks.AGREEMENT_CORE_BLOCK);

    public static final DeferredItem<BlockItem> UPGRADE_CRAFTING_TABLE_ITEM =
            ITEMS.registerSimpleBlockItem("upgrade_crafting_table_item", ModBlocks.UPGRADE_CRAFTING_TABLE);

    public static final Supplier<Item> EXAMPLE_ITEM =
            ITEMS.registerSimpleItem("example_item", new Item.Properties());

    public static final Supplier<Item> TERMINAL_ITEM =
            ITEMS.registerSimpleItem("terminal_item", new Item.Properties()
    );
    
    // 防御核心物品 - 注册时直接设置默认数据组件
    public static final DeferredItem<DefendCore> DEFEND_CORE =
            ITEMS.registerItem(
                    "defend_core",
                    properties -> new DefendCore(properties
                            .component(ModDataComponents.CORE_MODULE_DATA.get(), DefendCoreData.createDefault())),
                    new Item.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.EPIC)
            );

    //TurretCore
    public static final DeferredItem<Item> TURRET_CORE =
            ITEMS.registerItem("turret_core",
                     properties -> new TurretCore(properties
                          .component(ModDataComponents.TURRET_CORE_DATA.get(), TurretCoreData.createDefault())),
                     new Item.Properties()
                         .stacksTo(1)
                         .rarity(Rarity.UNCOMMON)
                    );
}
