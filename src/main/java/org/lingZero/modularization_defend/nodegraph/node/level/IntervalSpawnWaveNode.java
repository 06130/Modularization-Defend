package org.lingZero.modularization_defend.nodegraph.node.level;

import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.NodeAttribute;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.definition.IOptionDefinitionContext;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.definition.IPortDefinitionContext;
import net.minecraft.network.chat.Component;
import org.lingZero.modularization_defend.nodegraph.TurretLogicGraph;
import org.lingZero.modularization_defend.nodegraph.exec.LevelActionNode;

/**
 * 间隔生成波次动作节点——生成波次的变种：每隔设定秒数在红门生成 1 只敌人，
 * 而非瞬间全部生成。被触发时暂停执行链，本波全部生成完毕后才触发 then 下游。
 *
 * <h3>选项</h3>
 * <ul>
 *   <li><b>entityId (string)</b> — 实体注册 ID，默认 minecraft:zombie</li>
 *   <li><b>redDoorId (int)</b> — 生成点红门 ID，默认 1</li>
 *   <li><b>blueDoorId (int)</b> — 目标蓝门 ID，默认 1</li>
 * </ul>
 *
 * <h3>端口</h3>
 * <ul>
 *   <li><b>输入 exec (exec)</b> / <b>输出 then (exec)</b> — 执行流（then 在全部生成后触发）</li>
 *   <li><b>输入 count (int)</b> — 生成数量，默认 3；可连线动态计算</li>
 *   <li><b>输入 interval (float)</b> — 生成间隔秒数，默认 1.0；可连线动态计算</li>
 * </ul>
 */
@NodeAttribute(name = "间隔生成波次", group = "关卡/动作", graphTypes = {TurretLogicGraph.class})
public class IntervalSpawnWaveNode extends LevelActionNode {

    @Override
    public Component getDisplayName() {
        return Component.translatable("node.modularization_defend.interval_spawn_wave");
    }

    @Override
    public void onDefineOptions(IOptionDefinitionContext context) {
        super.onDefineOptions(context);
        context.addOption("entityId", String.class)
                .withDisplayName(Component.translatable("node.modularization_defend.entity_id"))
                .withDefaultValue("minecraft:zombie");
        context.addOption("redDoorId", Integer.class)
                .withDisplayName(Component.translatable("node.modularization_defend.red_door_id"))
                .withDefaultValue(1);
        context.addOption("blueDoorId", Integer.class)
                .withDisplayName(Component.translatable("node.modularization_defend.blue_door_id"))
                .withDefaultValue(1);
    }

    @Override
    protected void defineExtraPorts(IPortDefinitionContext context) {
        context.addInputPort("count", Integer.class)
                .withDisplayName(Component.translatable("node.modularization_defend.spawn_count"))
                .withDefaultValue(3)
                .build();
        context.addInputPort("interval", Float.class)
                .withDisplayName(Component.translatable("node.modularization_defend.spawn_interval"))
                .withDefaultValue(1.0f)
                .build();
    }
}
