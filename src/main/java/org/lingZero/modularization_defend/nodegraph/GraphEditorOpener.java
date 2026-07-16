package org.lingZero.modularization_defend.nodegraph;

import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.nodegraphtookit.editor.GraphEditorView;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.core.BlockPos;
import org.joml.Vector2f;
import org.lingZero.modularization_defend.nodegraph.network.SaveCardGraphPacket;
import org.lingZero.modularization_defend.nodegraph.network.SaveControllerGraphPacket;

/**
 * 客户端辅助类——用于在独立屏幕中打开节点图编辑器。
 */
@OnlyIn(Dist.CLIENT)
public final class GraphEditorOpener {

    private GraphEditorOpener() {}

    /** 打开一个包含预置示例节点的编辑器（/nodegraph 命令使用） */
    public static void openTurretLogicEditor() {
        var graph = createDemoGraph();
        openEditor(graph, "turret_logic_demo", null);
    }

    /** 打开一个空白编辑器（/nodegraph empty 命令使用） */
    public static void openEmptyTurretLogicEditor() {
        var graph = new TurretLogicGraph();
        openEditor(graph, "turret_logic_empty", null);
    }

    /**
     * 打开程序存储卡的节点图编辑器。
     *
     * @param graphTag 卡片中存储的图 NBT（空则创建新图）
     * @param mainHand 卡片所在的手（用于保存）
     * @param player   持有卡片的玩家
     */
    public static void openCardEditor(CompoundTag graphTag, boolean mainHand, Player player) {
        var graph = new TurretLogicGraph();
        if (!graphTag.isEmpty()) {
            graph.graphModel.deserializeNBT(Minecraft.getInstance().level.registryAccess(), graphTag);
        }

        openEditor(graph, "turret_logic_card", savedTag -> {
            // 保存回调：将图数据发送回服务端写入卡片
            PacketDistributor.sendToServer(new SaveCardGraphPacket(savedTag, mainHand));
        });
    }

    /**
     * 打开关卡控制器方块的节点图编辑器。
     *
     * @param graphTag 方块中存储的图 NBT（空则创建新图）
     * @param pos      关卡控制器的方块位置（用于保存）
     */
    public static void openControllerEditor(CompoundTag graphTag, BlockPos pos) {
        var graph = new TurretLogicGraph();
        if (!graphTag.isEmpty()) {
            graph.graphModel.deserializeNBT(Minecraft.getInstance().level.registryAccess(), graphTag);
        }

        openEditor(graph, "turret_logic_controller", savedTag ->
                PacketDistributor.sendToServer(new SaveControllerGraphPacket(pos, savedTag)));
    }

    /** 内部通用方法：用指定图和标题创建一个编辑器屏幕并打开 */
    private static void openEditor(TurretLogicGraph graph, String titleKey,
                                    java.util.function.Consumer<CompoundTag> onSaved) {
        var editorView = new GraphEditorView();
        editorView.layout(layout -> {
            layout.widthPercent(100);
            layout.heightPercent(100);
        });
        editorView.loadGraph(graph, onSaved != null ? onSaved : savedTag -> { });

        var ui = new ModularUI(UI.of(editorView));
        var screen = new ModularUIScreen(ui, Component.translatable("screen." + titleKey));
        Minecraft.getInstance().setScreen(screen);
    }

    /** 创建一个包含示例节点和连线的演示图 */
    public static TurretLogicGraph createDemoGraph() {
        var graph = new TurretLogicGraph();
        var model = graph.graphModel;

        var targetNode = model.createNodeModel(
                new org.lingZero.modularization_defend.nodegraph.node.TargetCountNode(),
                new Vector2f(100, 200));
        var thresholdNode = model.createNodeModel(
                new org.lingZero.modularization_defend.nodegraph.node.ThresholdGateNode(),
                new Vector2f(400, 200));

        var countOutput = targetNode.getOutputsById().get("count");
        var countInput = thresholdNode.getInputsById().get("count");
        if (countOutput != null && countInput != null) {
            model.createWire(countInput, countOutput);
        }

        return graph;
    }
}
