package org.lingZero.modularization_defend.nodegraph.node.level;

import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.NodeAttribute;
import net.minecraft.network.chat.Component;
import org.lingZero.modularization_defend.nodegraph.TurretLogicGraph;
import org.lingZero.modularization_defend.nodegraph.exec.LevelEventNode;

/**
 * 波次清空事件节点——本关已生成过敌人且全部被消灭时触发。
 * 每次新波次生成后条件回落，波次再次清空时会再次触发。
 *
 * <h3>端口</h3>
 * <ul><li><b>输出 then (exec)</b> — 波次清空时触发下游动作链</li></ul>
 */
@NodeAttribute(name = "波次清空", group = "关卡/事件", graphTypes = {TurretLogicGraph.class})
public class OnWaveClearedNode extends LevelEventNode {

    @Override
    public Component getDisplayName() {
        return Component.translatable("node.modularization_defend.on_wave_cleared");
    }
}
