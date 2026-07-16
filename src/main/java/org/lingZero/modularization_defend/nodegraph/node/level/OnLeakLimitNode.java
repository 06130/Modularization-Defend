package org.lingZero.modularization_defend.nodegraph.node.level;

import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.NodeAttribute;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.definition.IOptionDefinitionContext;
import net.minecraft.network.chat.Component;
import org.lingZero.modularization_defend.nodegraph.TurretLogicGraph;
import org.lingZero.modularization_defend.nodegraph.exec.LevelEventNode;

/**
 * 漏怪上限事件节点——本关漏怪数（进入蓝门的敌人数）达到设定上限时触发，
 * 通常下游接"关卡结果（失败）"。
 *
 * <h3>选项</h3>
 * <ul><li><b>limit (int)</b> — 漏怪上限，默认 10</li></ul>
 *
 * <h3>端口</h3>
 * <ul><li><b>输出 then (exec)</b> — 漏怪达上限时触发下游动作链</li></ul>
 */
@NodeAttribute(name = "漏怪上限", group = "关卡/事件", graphTypes = {TurretLogicGraph.class})
public class OnLeakLimitNode extends LevelEventNode {

    @Override
    public Component getDisplayName() {
        return Component.translatable("node.modularization_defend.on_leak_limit");
    }

    @Override
    public void onDefineOptions(IOptionDefinitionContext context) {
        super.onDefineOptions(context);
        context.addOption("limit", Integer.class)
                .withDisplayName(Component.translatable("node.modularization_defend.leak_limit"))
                .withDefaultValue(10);
    }
}
