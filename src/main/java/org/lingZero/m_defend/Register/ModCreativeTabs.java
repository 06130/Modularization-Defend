package org.lingZero.m_defend.Register;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.lingZero.m_defend.ModularizationDefend;

import java.util.function.Supplier;

import static org.lingZero.m_defend.Register.ModItems.*;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ModularizationDefend.MODID);
    public static final Supplier<CreativeModeTab> MODULARIZATION_DEFEND_TAB = CREATIVE_TABS.register("modularization_defend_tab", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.modularization_defend"))
                    .icon(() -> new ItemStack(EXAMPLE_ITEM.get()))
                    .displayItems(
                            (parameters, output) -> {
                                // 基础物品
                                output.accept(EXAMPLE_ITEM.get());
                                output.accept(TERMINAL_ITEM.get());
                                // 核心物品
                                output.accept(DEFEND_CORE.get());
                                // 方块物品
                                output.accept(UPGRADE_CRAFTING_TABLE_ITEM.get());
                                // 炮塔相关
                                output.accept(TURRET_1_ITEM.get());
                                output.accept(TURRET_REMOVAL_TOOL.get());
                                output.accept(GUIDANCE_CONTROL_COMPONENT.get());
                                // 炮塔核心
                                output.accept(TURRET_CORE.get());
                                output.accept(TURRET_CORE_SHU.get());
                                // 目标过滤器
                                output.accept(HOSTILE_FILTER.get());
                                output.accept(NEUTRAL_FILTER.get());
                                output.accept(FRIENDLY_FILTER.get());
                                output.accept(PLAYER_FILTER.get());
                                output.accept(ENTITY_ID_FILTER.get());
                            }
                    )
                    .build());
}
