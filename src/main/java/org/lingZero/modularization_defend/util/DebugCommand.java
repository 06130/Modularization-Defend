package org.lingZero.modularization_defend.util;

import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.lingZero.modularization_defend.ldlibUI.DefendCoreGUI;

/**
 * 调试命令处理器
 * 提供 /md_debug 命令用于快速测试LDLib UI
 */
public class DebugCommand {
    
    /**
     * 注册调试命令
     */
    public static void register(RegisterCommandsEvent event) {
        var dispatcher = event.getDispatcher();
        
        var rootCommand = Commands.literal("md_debug")
                .requires(source -> source.hasPermission(2)) // 需要OP权限
                .then(Commands.literal("UIdebug")
                        .executes(DebugCommand::openScreenUI));
                rootCommand.then(Commands.literal("printDataComponent")
                .executes(DebugCommand::printDataComponent));
        
        dispatcher.register(rootCommand);
    }

    /**
     * 打印玩家手持物品的所有数据组件原始信息到聊天栏
     */
    public static int printDataComponent(CommandContext<CommandSourceStack> context) {
        var player = context.getSource().getPlayer();
        if (player != null) {
            // 获取玩家主手物品
            var mainHandItem = player.getMainHandItem();
                
            if (mainHandItem.isEmpty()) {
                // 主手没有物品
                player.sendSystemMessage(Component.literal("§c主手没有持有任何物品！"));
                return Command.SINGLE_SUCCESS;
            }
                
            // 输出物品基本信息
            player.sendSystemMessage(Component.literal("§6=== 物品信息 ==="));
            player.sendSystemMessage(Component.literal("§b物品名称: §f" + mainHandItem.getHoverName().getString()));
            player.sendSystemMessage(Component.literal("§b物品 ID: §f" + mainHandItem.getItem().getDescriptionId()));
            player.sendSystemMessage(Component.literal("§b数量: §f" + mainHandItem.getCount()));
                
            // 获取所有数据组件
            var components = mainHandItem.getComponents();
                
            if (components.isEmpty()) {
                player.sendSystemMessage(Component.literal("§e该物品没有任何数据组件"));
                return Command.SINGLE_SUCCESS;
            }
                
            // 输出所有数据组件的原始信息
            player.sendSystemMessage(Component.literal("§6=== 数据组件列表 (共 " + components.size() + " 个) ==="));
                
            int index = 1;
            // 遍历所有注册的数据组件类型
            for (var componentType : net.minecraft.core.registries.BuiltInRegistries.DATA_COMPONENT_TYPE) {
                var value = components.get(componentType);
                if (value != null) {
                    // 输出组件类型和值的字符串表示
                    String componentInfo = String.format("§7[%d] §b%s§f: §r%s", 
                        index++, 
                        componentType.toString(),
                        value.toString()
                    );
                        
                    player.sendSystemMessage(Component.literal(componentInfo));
                }
            }
                
            player.sendSystemMessage(Component.literal("§6========================="));
        }
        return Command.SINGLE_SUCCESS;
    }
    /**
     * 打开测试UI界面
     */
    public static int openScreenUI(CommandContext<CommandSourceStack> context) {
        var player = context.getSource().getPlayer();
        if (player != null) {
            var minecraft = Minecraft.getInstance();
            var modularUI = DefendCoreGUI.createModularUI(player);
            minecraft.setScreen(new ModularUIScreen(modularUI, Component.empty()));
        }

        return Command.SINGLE_SUCCESS;
    }
}
