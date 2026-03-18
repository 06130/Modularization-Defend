package org.lingZero.modularization_defend.Register;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.lingZero.modularization_defend.ModularizationDefend;

import java.util.function.Supplier;

import static org.lingZero.modularization_defend.Register.ModItems.*;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ModularizationDefend.MODID);
    public static final Supplier<CreativeModeTab> MODULARIZATION_DEFEND_TAB = CREATIVE_TABS.register("modularization_defend_tab", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.modularization_defend"))
                    .icon(() -> new ItemStack(EXAMPLE_ITEM.get()))
                    .displayItems(
                            (parameters, output) -> {
                                output.accept(EXAMPLE_ITEM.get());
                                output.accept(ELECTRICITY_REPEATER_ITEM.get());
                                output.accept(TERMINAL_ITEM.get());
                                output.accept(ModBlocks.AGREEMENT_CORE_BLOCK.get().asItem());
                                output.accept(ModItems.AGREEMENT_CORE_ITEM.get());
                            }
                    )
                    .build());
}
