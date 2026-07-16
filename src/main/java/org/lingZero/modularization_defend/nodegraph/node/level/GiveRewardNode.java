package org.lingZero.modularization_defend.nodegraph.node.level;

import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.NodeAttribute;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.definition.IOptionDefinitionContext;
import net.minecraft.network.chat.Component;
import org.lingZero.modularization_defend.nodegraph.TurretLogicGraph;
import org.lingZero.modularization_defend.nodegraph.exec.LevelActionNode;

/**
 * 给予奖励动作节点——向关卡控制器附近的所有玩家发放指定物品。
 *
 * <h3>选项</h3>
 * <ul>
 *   <li><b>itemId (string)</b> — 物品注册 ID，默认 minecraft:diamond</li>
 *   <li><b>count (int)</b> — 每人数量，默认 1</li>
 * </ul>
 */
@NodeAttribute(name = "给予奖励", group = "关卡/动作", graphTypes = {TurretLogicGraph.class})
public class GiveRewardNode extends LevelActionNode {

    @Override
    public Component getDisplayName() {
        return Component.translatable("node.modularization_defend.give_reward");
    }

    @Override
    public void onDefineOptions(IOptionDefinitionContext context) {
        super.onDefineOptions(context);
        context.addOption("itemId", String.class)
                .withDisplayName(Component.translatable("node.modularization_defend.item_id"))
                .withDefaultValue("minecraft:diamond");
        context.addOption("count", Integer.class)
                .withDisplayName(Component.translatable("node.modularization_defend.reward_count"))
                .withDefaultValue(1);
    }
}
