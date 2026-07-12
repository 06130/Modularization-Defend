package org.lingZero.modularization_defend.nodegraph.node;

import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.Node;
import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.NodeAttribute;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.Capabilities;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.NodeModel;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.definition.IOptionDefinitionContext;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.definition.IPortDefinitionContext;
import net.minecraft.network.chat.Component;
import org.lingZero.modularization_defend.nodegraph.TurretLogicGraph;

import java.util.Optional;

/**
 * 二进制→十进制转换节点——将多个布尔位输入合成为一个整数值输出。
 *
 * <h3>选项</h3>
 * <ul><li><b>bits (int)</b> — 位宽，默认 4（1–16）</li></ul>
 *
 * <h3>端口</h3>
 * <ul>
 *   <li><b>输入 bit0..bitN (bool)</b> — 各二进制位（bit0 为最低位）</li>
 *   <li><b>输出 value (int)</b> — 合成的十进制整数值</li>
 * </ul>
 */
@NodeAttribute(name = "二进制→十进制", group = "转换", graphTypes = {TurretLogicGraph.class})
public class BinaryToDecimalNode extends Node {

    @Override
    public Component getDisplayName() {
        return Component.translatable("node.modularization_defend.bin_to_dec");
    }

    @Override
    public void setImplementation(NodeModel nodeModel) {
        super.setImplementation(nodeModel);
        nodeModel.setCapability(Capabilities.RENAMABLE, true);
    }

    @Override
    public void onDefineOptions(IOptionDefinitionContext context) {
        super.onDefineOptions(context);
        context.addOption("bits", Integer.class)
                .withDisplayName(Component.translatable("node.modularization_defend.bit_width"))
                .withDefaultValue(4);
    }

    @Override
    public void onDefinePorts(IPortDefinitionContext context) {
        super.onDefinePorts(context);
        Optional.ofNullable(getNodeOptionById("bits")).ifPresent(o ->
            o.tryGetValue(Integer.class).ifSuccess(bits -> {
                int count = bits instanceof Integer i ? Math.max(1, Math.min(i, 16)) : 4;
                for (int i = 0; i < count; i++) {
                    context.addInputPort("bit" + i, Boolean.class)
                            .withDisplayName(Component.literal("BIT " + i))
                            .build();
                }
            })
        );
        context.addOutputPort("value", Integer.class)
                .withDisplayName(Component.translatable("node.modularization_defend.decimal_value"))
                .build();
    }
}
