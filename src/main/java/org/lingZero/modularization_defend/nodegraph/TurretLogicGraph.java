package org.lingZero.modularization_defend.nodegraph;

import com.lowdragmc.lowdraglib2.LDLib2;
import com.lowdragmc.lowdraglib2.nodegraphtookit.api.graph.Graph;
import com.lowdragmc.lowdraglib2.nodegraphtookit.api.graph.GraphNodeRegistry;
import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.Node;
import com.lowdragmc.lowdraglib2.nodegraphtookit.api.type.TypeHandle;
import com.lowdragmc.lowdraglib2.nodegraphtookit.api.type.TypeHandleHelpers;
import com.lowdragmc.lowdraglib2.nodegraphtookit.api.type.TypeHandles;
import org.jetbrains.annotations.Nullable;
import org.lingZero.modularization_defend.nodegraph.node.CompareFunction;
import org.lingZero.modularization_defend.nodegraph.node.ConversionTarget;
import org.lingZero.modularization_defend.nodegraph.node.MathFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用逻辑图——用节点图编辑器定义可求值的逻辑流程。
 */
public class TurretLogicGraph extends Graph {

    /** 自定义选项类型（仅用于节点选项下拉，不显示为变量/常量） */
    public static final TypeHandle MATH_FUNCTION_TYPE =
            TypeHandleHelpers.customType(MathFunction.class, "modularization_defend:math_function", "Math Function");
    public static final TypeHandle COMPARE_FUNCTION_TYPE =
            TypeHandleHelpers.customType(CompareFunction.class, "modularization_defend:compare_function", "Compare Function");
    public static final TypeHandle CONVERSION_TARGET_TYPE =
            TypeHandleHelpers.customType(ConversionTarget.class, "modularization_defend:conversion_target", "Conversion Target");

    static {
        TypeHandleHelpers.setCustomDefaultValue(MATH_FUNCTION_TYPE, () -> MathFunction.ADD);
        TypeHandleHelpers.setCustomDefaultValue(COMPARE_FUNCTION_TYPE, () -> CompareFunction.GREATER_THAN);
        TypeHandleHelpers.setCustomDefaultValue(CONVERSION_TARGET_TYPE, () -> ConversionTarget.INT);
    }

    public static final GraphNodeRegistry NODE_REGISTRY =
            GraphNodeRegistry.create(LDLib2.id("turret_logic"), TurretLogicGraph.class);

    /** 常用变量类型 */
    private static final List<TypeHandle> STANDARD_TYPES = List.of(
            TypeHandles.BOOL, TypeHandles.INT, TypeHandles.LONG,
            TypeHandles.FLOAT, TypeHandles.DOUBLE, TypeHandles.STRING);

    @Override
    public @Nullable List<TypeHandle> getSupportTypes() {
        return STANDARD_TYPES;
    }

    @Override
    public List<Class<? extends Node>> getSupportNodes() {
        return NODE_REGISTRY.getNodeClasses();
    }
}
