package org.lingZero.modularization_defend.Register;

import net.minecraft.nbt.CompoundTag;
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
                    properties -> new DefendCore(properties
                            .component(ModDataComponents.CORE_MODULE_DATA.get(), createDefaultCoreData())),
                    new Item.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.EPIC)
            );
    
    /**
     * 创建防御核心的默认数据
     */
    private static CompoundTag createDefaultCoreData() {
        CompoundTag data = new CompoundTag();
        
        data.putInt("firing_rate_level", 1);  // 默认射速倍率
        data.putInt("harm_level", 1);  // 默认伤害倍率
        data.putInt("energy_expend_level", 1);  // 默认能量消耗倍率
        
        data.putLong("energy_max", 10000);  // 默认能量存储上限
        data.putLong("energy_current", 0); // 默认当前能量
        
        data.putDouble("level", 1); // 护盾容量 (1=1 点伤害)
        data.putBoolean("shieldActive", false);  // 护盾系统默认关闭
        data.putString("turretCount", "null");  // 炮台核心类型
        
        return data;
    }
}
