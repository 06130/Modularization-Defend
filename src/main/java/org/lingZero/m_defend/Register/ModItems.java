package org.lingZero.m_defend.Register;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.lingZero.m_defend.DataComponents.DefendCoreData;
import org.lingZero.m_defend.DataComponents.TargetFilterData;
import org.lingZero.m_defend.DataComponents.TurretCoreData;
import org.lingZero.m_defend.Items.DefendCore;
import org.lingZero.m_defend.Items.GuidanceControl.GuidanceControlComponent;
import org.lingZero.m_defend.Items.TargetFilter.EntityIdFilter;
import org.lingZero.m_defend.Items.TargetFilter.FriendlyFilter;
import org.lingZero.m_defend.Items.TargetFilter.HostileFilter;
import org.lingZero.m_defend.Items.TargetFilter.NeutralFilter;
import org.lingZero.m_defend.Items.TargetFilter.PlayerFilter;
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
    
    // 制导控制组件
    public static final DeferredItem<Item> GUIDANCE_CONTROL_COMPONENT =
            ITEMS.registerItem("guidance_control_component",
                    GuidanceControlComponent::new,
                    new Item.Properties()
                            .stacksTo(64)
            );
    
    // 目标过滤器物品
    public static final DeferredItem<Item> HOSTILE_FILTER =
            ITEMS.registerItem("hostile_filter",
                    properties -> new HostileFilter(properties
                            .component(ModDataComponents.TARGET_FILTER_DATA.get(), TargetFilterData.hostile())),
                    new Item.Properties()
                            .stacksTo(64)
            );
    
    public static final DeferredItem<Item> NEUTRAL_FILTER =
            ITEMS.registerItem("neutral_filter",
                    properties -> new NeutralFilter(properties
                            .component(ModDataComponents.TARGET_FILTER_DATA.get(), TargetFilterData.neutral())),
                    new Item.Properties()
                            .stacksTo(64)
            );
    
    public static final DeferredItem<Item> FRIENDLY_FILTER =
            ITEMS.registerItem("friendly_filter",
                    properties -> new FriendlyFilter(properties
                            .component(ModDataComponents.TARGET_FILTER_DATA.get(), TargetFilterData.friendly())),
                    new Item.Properties()
                            .stacksTo(64)
            );
    
    public static final DeferredItem<Item> PLAYER_FILTER =
            ITEMS.registerItem("player_filter",
                    properties -> new PlayerFilter(properties
                            .component(ModDataComponents.TARGET_FILTER_DATA.get(), TargetFilterData.player())),
                    new Item.Properties()
                            .stacksTo(64)
            );
    
    public static final DeferredItem<Item> ENTITY_ID_FILTER =
            ITEMS.registerItem("entity_id_filter",
                    properties -> new EntityIdFilter(properties
                            .component(ModDataComponents.TARGET_FILTER_DATA.get(), TargetFilterData.entityId("minecraft:zombie"))),
                    new Item.Properties()
                            .stacksTo(64)
            );
}
