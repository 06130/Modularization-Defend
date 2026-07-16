package org.lingZero.modularization_defend.nodegraph.node;

import net.minecraft.network.chat.Component;

/** 转换目标类型枚举——仅包含可互转的数值与字符串类型 */
public enum ConversionTarget {

    INT(Component.translatable("node.modularization_defend.conv.int"), "整数", Integer.class),
    LONG(Component.translatable("node.modularization_defend.conv.long"), "长整数", Long.class),
    FLOAT(Component.translatable("node.modularization_defend.conv.float"), "浮点数", Float.class),
    DOUBLE(Component.translatable("node.modularization_defend.conv.double"), "双精度", Double.class),
    STRING(Component.translatable("node.modularization_defend.conv.string"), "字符串", String.class);

    private final Component displayName;
    private final String typeName;
    private final Class<?> javaType;

    ConversionTarget(Component displayName, String typeName, Class<?> javaType) {
        this.displayName = displayName;
        this.typeName = typeName;
        this.javaType = javaType;
    }

    public Component getDisplayName() { return displayName; }
    public String getTypeName() { return typeName; }
    public Class<?> getJavaType() { return javaType; }
}
