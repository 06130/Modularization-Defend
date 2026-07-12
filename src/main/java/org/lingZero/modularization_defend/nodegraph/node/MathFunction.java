package org.lingZero.modularization_defend.nodegraph.node;

import net.minecraft.network.chat.Component;

/** 数学函数类型枚举——LDLib2 会为此类型自动渲染下拉选择器 */
public enum MathFunction {

    ADD(0, true, Component.translatable("node.modularization_defend.math.add")),
    SUBTRACT(1, true, Component.translatable("node.modularization_defend.math.subtract")),
    MULTIPLY(2, true, Component.translatable("node.modularization_defend.math.multiply")),
    DIVIDE(3, true, Component.translatable("node.modularization_defend.math.divide")),
    MODULO(4, true, Component.translatable("node.modularization_defend.math.modulo")),
    POWER(5, true, Component.translatable("node.modularization_defend.math.power")),
    SQRT(6, false, Component.translatable("node.modularization_defend.math.sqrt")),
    ABS(7, false, Component.translatable("node.modularization_defend.math.abs")),
    NEGATE(8, false, Component.translatable("node.modularization_defend.math.negate")),
    MIN(9, true, Component.translatable("node.modularization_defend.math.min")),
    MAX(10, true, Component.translatable("node.modularization_defend.math.max"));

    private final int id;
    private final boolean dualInput;
    private final Component displayName;

    MathFunction(int id, boolean dualInput, Component displayName) {
        this.id = id;
        this.dualInput = dualInput;
        this.displayName = displayName;
    }

    public int getId() { return id; }
    public boolean isDualInput() { return dualInput; }
    public Component getDisplayName() { return displayName; }
}
