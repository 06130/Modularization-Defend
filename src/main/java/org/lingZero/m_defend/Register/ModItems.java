package org.lingZero.m_defend.Register;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.lingZero.m_defend.DataComponents.DefendCoreData;
import org.lingZero.m_defend.DataComponents.TurretCoreData;
import org.lingZero.m_defend.Items.DefendCore;
import org.lingZero.m_defend.Items.Turret.Turret1Item;
import org.lingZero.m_defend.Items.TurretCore.BasicTurretCore;
import org.lingZero.m_defend.Items.TurretCore.Shu;
import org.lingZero.m_defend.Items.TurretRemovalTool;
import org.lingZero.m_defend.ModularizationDefend;

import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(ModularizationDefend.MODID);

    public static final DeferredItem<BlockItem> UPGRADE_CRAFTING_TABLE_ITEM =
            ITEMS.registerSimpleBlockItem("upgrade_crafting_table_item", ModBlocks.UPGRADE_CRAFTING_TABLE);

    public static final Supplier<Item> EXAMPLE_ITEM =
            ITEMS.registerSimpleItem("example_item", new Item.Properties());

    public static final Supplier<Item> TERMINAL_ITEM =
            ITEMS.registerSimpleItem("terminal_item", new Item.Properties()
    );

    public static final DeferredItem<DefendCore> DEFEND_CORE =
            ITEMS.registerItem(
                    "defend_core",
                    properties -> new DefendCore(properties
                            .component(ModDataComponents.CORE_MODULE_DATA.get(), DefendCoreData.createDefault())),
                    new Item.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.EPIC)
            );
    public static final DeferredItem<Item> TURRET_1_ITEM =
            ITEMS.registerItem("turret_1_item",
                    Turret1Item::new,
                    new Item.Properties()
                            .stacksTo(64)
            );

    //TurretCore
    public static final DeferredItem<Item> TURRET_CORE =
            ITEMS.registerItem("basic_turret_core",
                     properties -> new BasicTurretCore(properties
                          .component(ModDataComponents.TURRET_CORE_DATA.get(), TurretCoreData.createDefault())),
                     new Item.Properties()
                         .stacksTo(1)
                         .rarity(Rarity.UNCOMMON)
                    );

    public static final DeferredItem<Item> TURRET_CORE_SHU =
            ITEMS.registerItem("turret_core_shu",
                    properties -> new Shu(properties
                            .component(ModDataComponents.TURRET_CORE_DATA.get(), TurretCoreData.createDefault())),
                    new Item.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.UNCOMMON)
            );
    
    // 炮塔拆除工具
    public static final DeferredItem<Item> TURRET_REMOVAL_TOOL =
            ITEMS.registerItem("turret_removal_tool",
                    TurretRemovalTool::new,
                    new Item.Properties()
                            .stacksTo(1)
            );
}
