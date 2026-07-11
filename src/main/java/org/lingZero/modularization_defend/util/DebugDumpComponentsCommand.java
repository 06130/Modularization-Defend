package org.lingZero.modularization_defend.util;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 调试命令：打印玩家手持物品的所有数据组件。
 * <p>
 * 用法：/md_dump_components
 */
public class DebugDumpComponentsCommand {

    private DebugDumpComponentsCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("md_dump_components")
                .requires(source -> source.hasPermission(2))
                .executes(ctx -> {
                    CommandSourceStack source = ctx.getSource();
                    Player player = source.getPlayerOrException();
                    ItemStack stack = player.getMainHandItem();

                    if (stack.isEmpty()) {
                        source.sendFailure(Component.literal("手上没有物品"));
                        return Command.SINGLE_SUCCESS;
                    }

                    source.sendSuccess(() -> Component.literal(
                            "=== 物品: " + stack.getDisplayName().getString() + " ==="), false);

                    DataComponentMap components = stack.getComponents();
                    if (components.isEmpty()) {
                        source.sendSuccess(() -> Component.literal("  (无数据组件)"), false);
                    } else {
                        for (TypedDataComponent<?> entry : components) {
                            DataComponentType<?> type = entry.type();
                            Object value = entry.value();
                            String typeName = type.toString();
                            source.sendSuccess(() -> Component.literal(
                                    "  §e" + typeName + " §7= §f" + value), false);
                        }
                    }

                    source.sendSuccess(() -> Component.literal(
                            "--- 共 " + components.size() + " 个数据组件 ---"), false);

                    return Command.SINGLE_SUCCESS;
                }));
    }
}
