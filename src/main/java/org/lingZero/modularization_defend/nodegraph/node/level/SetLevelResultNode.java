package org.lingZero.modularization_defend.nodegraph.node.level;

import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.NodeAttribute;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.definition.IOptionDefinitionContext;
import net.minecraft.network.chat.Component;
import org.lingZero.modularization_defend.nodegraph.TurretLogicGraph;
import org.lingZero.modularization_defend.nodegraph.exec.LevelActionNode;

/**
 * 关卡结果动作节点——判定关卡胜利或失败并结束运行。
 * 执行后关卡进入终态，图停止 tick，通常放在动作链末端。
 *
 * <h3>选项</h3>
 * <ul><li><b>victory (bool)</b> — true 胜利 / false 失败，默认 true</li></ul>
 */
@NodeAttribute(name = "关卡结果", group = "关卡/动作", graphTypes = {TurretLogicGraph.class})
public class SetLevelResultNode extends LevelActionNode {

    @Override
    public Component getDisplayName() {
        return Component.translatable("node.modularization_defend.set_level_result");
    }

    @Override
    public void onDefineOptions(IOptionDefinitionContext context) {
        super.onDefineOptions(context);
        context.addOption("victory", Boolean.class)
                .withDisplayName(Component.translatable("node.modularization_defend.victory"))
                .withDefaultValue(true);
    }
}
