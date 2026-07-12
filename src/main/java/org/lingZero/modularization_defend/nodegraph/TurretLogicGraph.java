package org.lingZero.modularization_defend.nodegraph;

import com.lowdragmc.lowdraglib2.LDLib2;
import com.lowdragmc.lowdraglib2.nodegraphtookit.api.graph.Graph;
import com.lowdragmc.lowdraglib2.nodegraphtookit.api.graph.GraphNodeRegistry;
import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.Node;
import com.lowdragmc.lowdraglib2.nodegraphtookit.api.type.TypeHandle;
import com.lowdragmc.lowdraglib2.nodegraphtookit.api.type.TypeHandleHelpers;
import org.jetbrains.annotations.Nullable;
import org.lingZero.modularization_defend.nodegraph.node.CompareFunction;
import org.lingZero.modularization_defend.nodegraph.node.MathFunction;

import java.util.List;

/**
 * 炮塔逻辑图——用节点图编辑器定义炮塔的行为逻辑链。
 */
public class TurretLogicGraph extends Graph {

    /** 注册自定义选项类型（仅用于节点选项，不显示为变量/常量类型） */
    public static final TypeHandle MATH_FUNCTION_TYPE =
            TypeHandleHelpers.customType(MathFunction.class, "modularization_defend:math_function", "Math Function");
    public static final TypeHandle COMPARE_FUNCTION_TYPE =
            TypeHandleHelpers.customType(CompareFunction.class, "modularization_defend:compare_function", "Compare Function");

    static {
        TypeHandleHelpers.setCustomDefaultValue(MATH_FUNCTION_TYPE, () -> MathFunction.ADD);
        TypeHandleHelpers.setCustomDefaultValue(COMPARE_FUNCTION_TYPE, () -> CompareFunction.GREATER_THAN);
    }

    public static final GraphNodeRegistry NODE_REGISTRY =
            GraphNodeRegistry.create(LDLib2.id("turret_logic"), TurretLogicGraph.class);

    @Override
    public @Nullable List<TypeHandle> getSupportTypes() {
        return null; // 自动从节点端口检测
    }

    @Override
    public List<Class<? extends Node>> getSupportNodes() {
        return NODE_REGISTRY.getNodeClasses();
    }
}
