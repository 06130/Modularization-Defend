package org.lingZero.modularization_defend.nodegraph.node.level;

import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.NodeAttribute;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.definition.IOptionDefinitionContext;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.definition.IPortDefinitionContext;
import net.minecraft.network.chat.Component;
import org.lingZero.modularization_defend.nodegraph.TurretLogicGraph;
import org.lingZero.modularization_defend.nodegraph.exec.LevelActionNode;

/**
 * 复位关卡动作节点——清理生成的敌人与全部运行时状态，关卡回到待机。
 * 终点节点（无 then 出口）。配合"延迟"可实现结算后自动复位，
 * 开启 restart 选项则复位后立即重新开始（自动循环关卡）。
 *
 * <h3>选项</h3>
 * <ul><li><b>restart (bool)</b> — 复位后自动重新开始，默认关闭</li></ul>
 *
 * <h3>端口</h3>
 * <ul><li><b>输入 exec (exec)</b> — 执行流入口（无输出，执行链到此结束）</li></ul>
 */
@NodeAttribute(name = "复位关卡", group = "关卡/动作", graphTypes = {TurretLogicGraph.class})
public class ResetLevelNode extends LevelActionNode {

    @Override
    public Component getDisplayName() {
        return Component.translatable("node.modularization_defend.reset_level");
    }

    /** 终点节点：复位会清空全部运行时状态，继续执行下游没有意义 */
    @Override
    protected void defineExecOutputs(IPortDefinitionContext context) {
    }

    @Override
    public void onDefineOptions(IOptionDefinitionContext context) {
        super.onDefineOptions(context);
        context.addOption("restart", Boolean.class)
                .withDisplayName(Component.translatable("node.modularization_defend.reset_restart"))
                .withDefaultValue(false);
    }
}
