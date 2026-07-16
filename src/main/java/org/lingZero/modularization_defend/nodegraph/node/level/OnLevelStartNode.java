package org.lingZero.modularization_defend.nodegraph.node.level;

import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.NodeAttribute;
import net.minecraft.network.chat.Component;
import org.lingZero.modularization_defend.nodegraph.TurretLogicGraph;
import org.lingZero.modularization_defend.nodegraph.exec.LevelEventNode;

/**
 * 关卡开始事件节点——关卡进入运行状态的那一刻触发一次。
 *
 * <h3>端口</h3>
 * <ul><li><b>输出 then (exec)</b> — 关卡启动时触发下游动作链</li></ul>
 */
@NodeAttribute(name = "关卡开始", group = "关卡/事件", graphTypes = {TurretLogicGraph.class})
public class OnLevelStartNode extends LevelEventNode {

    @Override
    public Component getDisplayName() {
        return Component.translatable("node.modularization_defend.on_level_start");
    }
}
