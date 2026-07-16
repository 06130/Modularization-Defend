package org.lingZero.modularization_defend.nodegraph.node.level;

import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.NodeAttribute;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.definition.IOptionDefinitionContext;
import net.minecraft.network.chat.Component;
import org.lingZero.modularization_defend.nodegraph.TurretLogicGraph;
import org.lingZero.modularization_defend.nodegraph.exec.LevelActionNode;

/**
 * 发送消息动作节点——向关卡控制器附近的玩家广播一条聊天消息。
 *
 * <h3>选项</h3>
 * <ul><li><b>message (string)</b> — 消息内容</li></ul>
 */
@NodeAttribute(name = "发送消息", group = "关卡/动作", graphTypes = {TurretLogicGraph.class})
public class SendMessageNode extends LevelActionNode {

    @Override
    public Component getDisplayName() {
        return Component.translatable("node.modularization_defend.send_message");
    }

    @Override
    public void onDefineOptions(IOptionDefinitionContext context) {
        super.onDefineOptions(context);
        context.addOption("message", String.class)
                .withDisplayName(Component.translatable("node.modularization_defend.message"))
                .withDefaultValue("");
    }
}
