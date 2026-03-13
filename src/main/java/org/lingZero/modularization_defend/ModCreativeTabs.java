package org.lingZero.modularization_defend;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static org.lingZero.modularization_defend.register.blocks.ElectricityRepeater.ELECTRICITY_REPEATER_ITEM;
import static org.lingZero.modularization_defend.register.items.EXAMPLE_ITEM.EXAMPLE_ITEM;
import static org.lingZero.modularization_defend.modularization_defend.MODID;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB,MODID);

    public static final Supplier<CreativeModeTab> MODULARIZATION_DEFEND_TAB = CREATIVE_TABS.register("modularization_defend_tab", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.modularization_defend"))
                    .icon(() -> new ItemStack(EXAMPLE_ITEM.get()))
                    .displayItems(
                            (parameters, output) -> {
                                output.accept(EXAMPLE_ITEM.get());
                                output.accept(ELECTRICITY_REPEATER_ITEM.get());
                            }
                    )
                    .build());
}
