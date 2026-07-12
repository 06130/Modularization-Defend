package org.lingZero.modularization_defend.nodegraph.node;

import net.minecraft.network.chat.Component;

/** 比较函数类型枚举——LDLib2 会为此类型自动渲染下拉选择器 */
public enum CompareFunction {

    GREATER_THAN(Component.translatable("node.modularization_defend.cmp.gt")),
    LESS_THAN(Component.translatable("node.modularization_defend.cmp.lt")),
    GREATER_EQUAL(Component.translatable("node.modularization_defend.cmp.ge")),
    LESS_EQUAL(Component.translatable("node.modularization_defend.cmp.le")),
    EQUAL(Component.translatable("node.modularization_defend.cmp.eq")),
    NOT_EQUAL(Component.translatable("node.modularization_defend.cmp.ne"));

    private final Component displayName;

    CompareFunction(Component displayName) {
        this.displayName = displayName;
    }

    public Component getDisplayName() { return displayName; }
}
