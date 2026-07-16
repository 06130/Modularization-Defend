package org.lingZero.modularization_defend.nodegraph.node.level;

import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.NodeAttribute;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.definition.IPortDefinitionContext;
import net.minecraft.network.chat.Component;
import org.lingZero.modularization_defend.nodegraph.TurretLogicGraph;
import org.lingZero.modularization_defend.nodegraph.exec.LevelEventNode;

/**
 * 条件触发事件节点——通用事件源：条件输入由假变真（上升沿）时触发一次。
 * 可与计时器、比较器等数据流节点组合出任意触发逻辑（如"关卡时间超过 60 秒"）。
 *
 * <h3>端口</h3>
 * <ul>
 *   <li><b>输入 condition (bool)</b> — 触发条件</li>
 *   <li><b>输出 then (exec)</b> — 条件上升沿触发下游动作链</li>
 * </ul>
 */
@NodeAttribute(name = "条件触发", group = "关卡/事件", graphTypes = {TurretLogicGraph.class})
public class OnConditionNode extends LevelEventNode {

    @Override
    public Component getDisplayName() {
        return Component.translatable("node.modularization_defend.on_condition");
    }

    @Override
    protected void defineExtraPorts(IPortDefinitionContext context) {
        context.addInputPort("condition", Boolean.class)
                .withDisplayName(Component.translatable("node.modularization_defend.condition"))
                .build();
    }
}
