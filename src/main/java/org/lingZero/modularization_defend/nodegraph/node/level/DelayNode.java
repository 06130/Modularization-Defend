package org.lingZero.modularization_defend.nodegraph.node.level;

import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.NodeAttribute;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.definition.IPortDefinitionContext;
import net.minecraft.network.chat.Component;
import org.lingZero.modularization_defend.nodegraph.TurretLogicGraph;
import org.lingZero.modularization_defend.nodegraph.exec.LevelActionNode;

/**
 * 延迟动作节点——被触发后暂停执行链，等待设定秒数再触发 then 下游。
 * 延迟由关卡控制器调度并持久化，存档重载后继续计时。
 *
 * <h3>端口</h3>
 * <ul>
 *   <li><b>输入 exec (exec)</b> / <b>输出 then (exec)</b> — 执行流（then 延迟后触发）</li>
 *   <li><b>输入 seconds (float)</b> — 延迟秒数，默认 1.0；可连线动态计算</li>
 * </ul>
 */
@NodeAttribute(name = "延迟", group = "关卡/动作", graphTypes = {TurretLogicGraph.class})
public class DelayNode extends LevelActionNode {

    @Override
    public Component getDisplayName() {
        return Component.translatable("node.modularization_defend.delay");
    }

    @Override
    protected void defineExtraPorts(IPortDefinitionContext context) {
        context.addInputPort("seconds", Float.class)
                .withDisplayName(Component.translatable("node.modularization_defend.delay_seconds"))
                .withDefaultValue(1.0f)
                .build();
    }
}
