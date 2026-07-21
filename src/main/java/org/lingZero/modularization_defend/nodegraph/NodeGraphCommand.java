package org.lingZero.modularization_defend.nodegraph;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.lowdragmc.lowdraglib2.gui.factory.PlayerUIMenuType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lingZero.modularization_defend.nodegraph.network.OpenGraphEditorPacket;

/**
 * 节点图编辑器命令——供调试和编辑炮塔逻辑图使用。
 *
 * <pre>
 * /nodegraph          — 打开包含预置示例的炮塔逻辑图编辑器（权限2）
 * /nodegraph empty    — 打开空白炮塔逻辑图编辑器（权限2）
 * /nodegraph editor   — 打开带资源面板的图资源编辑器，可加载/保存外部图文件（权限2）
 * </pre>
 */
public class NodeGraphCommand {

    private NodeGraphCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var cmd = Commands.literal("nodegraph")
                .requires(src -> src.hasPermission(2))
                // /nodegraph — 打开演示图
                .executes(ctx -> openEditor(ctx.getSource(), false));

        // /nodegraph empty — 打开空白图
        cmd.then(Commands.literal("empty")
                .executes(ctx -> openEditor(ctx.getSource(), true)));

        // /nodegraph editor — 打开图资源编辑器（外部资源加载与保存）
        cmd.then(Commands.literal("editor")
                .executes(ctx -> openResourceEditor(ctx.getSource())));

        dispatcher.register(cmd);
    }

    private static int openEditor(CommandSourceStack source, boolean empty) {
        ServerPlayer player = source.getPlayer();
        if (player != null) {
            PacketDistributor.sendToPlayer(player, new OpenGraphEditorPacket(empty));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int openResourceEditor(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player != null) {
            PlayerUIMenuType.openUI(player, TurretLogicEditor.WINDOW_ID);
        }
        return Command.SINGLE_SUCCESS;
    }
}
