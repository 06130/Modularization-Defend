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
        
        dispatcher.register(rootCommand);
    }
    
    /**
     * 打开测试UI界面
     */
    public static int openScreenUI(CommandContext<CommandSourceStack> context) {
        var player = context.getSource().getPlayer();
            var minecraft = Minecraft.getInstance();
            var modularUI = DefendCoreGUI.createModularUI(player);
            minecraft.setScreen(new ModularUIScreen(modularUI, Component.empty()));

        return Command.SINGLE_SUCCESS;
    }
}
