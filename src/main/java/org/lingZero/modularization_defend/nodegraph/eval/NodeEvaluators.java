package org.lingZero.modularization_defend.nodegraph.eval;

import org.lingZero.modularization_defend.nodegraph.eval.NodeGraphEvaluator.EvalCtx;
import org.lingZero.modularization_defend.nodegraph.node.CompareFunction;
import org.lingZero.modularization_defend.nodegraph.node.ConversionTarget;
import org.lingZero.modularization_defend.nodegraph.node.MathFunction;

/**
 * 为所有自定义节点注册求值逻辑。在模组初始化时调用 {@link #init()}。
 *
 * <h3>外部上下文 Key</h3>
 * <ul>
 *   <li>{@code "timer:elapsed:<nodeUid>"} — 计时器已过秒数 (Float)</li>
 *   <li>{@code "store:<nodeUid>"} — 数据存储节点的当前值 (Float)</li>
 * </ul>
 */
public final class NodeEvaluators {

    private NodeEvaluators() {}

    public static void init() {
        registerIntegerValue();
        registerThresholdGate();
        registerLogicGates();
        registerConversion();
        registerMathFunction();
        registerComparator();
        registerTimer();
        registerDataStore();
        registerTypeConverter();
    }

    private static void registerIntegerValue() {
        NodeGraphEvaluator.registerEvaluator("整数值", ctx -> {
            Object opt = ctx.evaluator().getOptionValue(ctx.node(), "value");
            return opt instanceof Integer i ? i : 0;
        });
    }

    private static void registerThresholdGate() {
        NodeGraphEvaluator.registerEvaluator("阈值门", ctx -> {
            if (!"result".equals(ctx.portId())) return null;
            Object val = ctx.evaluator().getInputPortValue(ctx.node(), "value");
            Object thr = ctx.evaluator().getOptionValue(ctx.node(), "threshold");
            int v = val instanceof Integer i ? i : (val instanceof Number n ? n.intValue() : 0);
            int t = thr instanceof Integer i ? i : (thr instanceof Number n ? n.intValue() : 3);
            return v > t;
        });
    }

    private static void registerLogicGates() {
        NodeGraphEvaluator.registerEvaluator("与门", ctx ->
            "result".equals(ctx.portId()) ? allInputs(ctx, true, (a, b) -> a && b) : null);
        NodeGraphEvaluator.registerEvaluator("或门", ctx ->
            "result".equals(ctx.portId()) ? allInputs(ctx, false, (a, b) -> a || b) : null);
        NodeGraphEvaluator.registerEvaluator("非门", ctx -> {
            if (!"out".equals(ctx.portId())) return null;
            Object in = ctx.evaluator().getInputPortValue(ctx.node(), "in");
            return !(in instanceof Boolean b && b);
        });
        NodeGraphEvaluator.registerEvaluator("异或门", ctx -> {
            if (!"out".equals(ctx.portId())) return null;
            boolean a = ctx.evaluator().getInputPortValue(ctx.node(), "a") instanceof Boolean ab && ab;
            boolean b = ctx.evaluator().getInputPortValue(ctx.node(), "b") instanceof Boolean bb && bb;
            return a != b;
        });
    }

    private static boolean allInputs(EvalCtx ctx, boolean init,
                                      java.util.function.BinaryOperator<Boolean> op) {
        boolean result = init;
        boolean hasInput = false;
        for (var port : ctx.evaluator().getDynamicInputPorts(ctx.node())) {
            Object val = ctx.evaluator().evaluatePort(port);
            if (val instanceof Boolean b) {
                result = op.apply(result, b);
                hasInput = true;
            }
        }
        return hasInput ? result : init;
    }

    private static void registerConversion() {
        NodeGraphEvaluator.registerEvaluator("十进制→二进制", ctx -> {
            String pid = ctx.portId();
            if (!pid.startsWith("bit")) return null;
            int i = Integer.parseInt(pid.substring(3));
            Object v = ctx.evaluator().getInputPortValue(ctx.node(), "value");
            int val = v instanceof Number n ? n.intValue() : 0;
            return ((val >> i) & 1) == 1;
        });
        NodeGraphEvaluator.registerEvaluator("二进制→十进制", ctx -> {
            if (!"value".equals(ctx.portId())) return null;
            int result = 0;
            for (int i = 0; i < 16; i++) {
                Object b = ctx.evaluator().getInputPortValue(ctx.node(), "bit" + i);
                if (b instanceof Boolean bb && bb) result |= (1 << i);
            }
            return result;
        });
    }

    private static void registerMathFunction() {
        NodeGraphEvaluator.registerEvaluator("函数计算", ctx -> {
            if (!"result".equals(ctx.portId())) return null;
            float a = toFloat(ctx.evaluator().getInputPortValue(ctx.node(), "a"));
            MathFunction fn = getMathFunction(ctx);
            if (fn == null) return a;
            if (fn.isDualInput()) {
                float b = toFloat(ctx.evaluator().getInputPortValue(ctx.node(), "b"));
                return applyMath(fn, a, b);
            }
            return applyMath(fn, a, 0);
        });
    }

    private static MathFunction getMathFunction(EvalCtx ctx) {
        Object opt = ctx.evaluator().getOptionValue(ctx.node(), "function");
        if (opt instanceof MathFunction mf) return mf;
        if (opt instanceof String s) { try { return MathFunction.valueOf(s); } catch (Exception ignored) {} }
        return MathFunction.ADD;
    }

    private static Float applyMath(MathFunction fn, float a, float b) {
        return switch (fn) {
            case ADD -> a + b;
            case SUBTRACT -> a - b;
            case MULTIPLY -> a * b;
            case DIVIDE -> b != 0 ? a / b : 0f;
            case MODULO -> b != 0 ? a % b : 0f;
            case POWER -> (float) Math.pow(a, b);
            case SQRT -> (float) Math.sqrt(Math.max(0, a));
            case ABS -> Math.abs(a);
            case NEGATE -> -a;
            case MIN -> Math.min(a, b);
            case MAX -> Math.max(a, b);
        };
    }

    private static void registerComparator() {
        NodeGraphEvaluator.registerEvaluator("比较器", ctx -> {
            if (!"result".equals(ctx.portId())) return null;
            float a = toFloat(ctx.evaluator().getInputPortValue(ctx.node(), "a"));
            float b = toFloat(ctx.evaluator().getInputPortValue(ctx.node(), "b"));
            CompareFunction fn = getCompareFunction(ctx);
            return fn != null ? applyCmp(fn, a, b) : a > b;
        });
    }

    private static CompareFunction getCompareFunction(EvalCtx ctx) {
        Object opt = ctx.evaluator().getOptionValue(ctx.node(), "function");
        if (opt instanceof CompareFunction cf) return cf;
        if (opt instanceof String s) { try { return CompareFunction.valueOf(s); } catch (Exception ignored) {} }
        return CompareFunction.GREATER_THAN;
    }

    private static boolean applyCmp(CompareFunction fn, float a, float b) {
        return switch (fn) {
            case GREATER_THAN -> a > b;
            case LESS_THAN -> a < b;
            case GREATER_EQUAL -> a >= b;
            case LESS_EQUAL -> a <= b;
            case EQUAL -> a == b;
            case NOT_EQUAL -> a != b;
        };
    }

    private static void registerTimer() {
        NodeGraphEvaluator.registerEvaluator("计时器", ctx -> {
            if (!"time".equals(ctx.portId())) return null;
            Object e = ctx.evaluator().getExternalInput("timer:elapsed:" + ctx.node().getUid());
            return e instanceof Number n ? n.floatValue() : 0f;
        });
    }

    private static void registerDataStore() {
        NodeGraphEvaluator.registerEvaluator("数据存储", ctx -> {
            if (!"stored".equals(ctx.portId())) return null;
            Object s = ctx.evaluator().getExternalInput("store:" + ctx.node().getUid());
            if (s instanceof Number n) return n.floatValue();
            Object init = ctx.evaluator().getOptionValue(ctx.node(), "initialValue");
            return init instanceof Number n ? n.floatValue() : 0f;
        });
    }

    private static float toFloat(Object val) {
        return val instanceof Number n ? n.floatValue() : 0f;
    }

    // ==================== 类型转换 ====================
    private static void registerTypeConverter() {
        registerConverter("Float→目标");
        registerConverter("Int→目标");
        registerConverter("Bool→目标");
        registerConverter("String→目标");
    }

    private static void registerConverter(String nodeName) {
        NodeGraphEvaluator.registerEvaluator(nodeName, ctx -> {
            if (!"result".equals(ctx.portId())) return null;
            double val = toDouble(ctx.evaluator().getInputPortValue(ctx.node(), "value"));
            ConversionTarget target = getConversionTarget(ctx, "target", ConversionTarget.INT);
            return convertValue(val, target);
        });
    }

    private static ConversionTarget getConversionTarget(EvalCtx ctx, String optionId, ConversionTarget fallback) {
        Object opt = ctx.evaluator().getOptionValue(ctx.node(), optionId);
        if (opt instanceof ConversionTarget ct) return ct;
        if (opt instanceof String s) { try { return ConversionTarget.valueOf(s); } catch (Exception ignored) {} }
        return fallback;
    }

    private static Object convertValue(double num, ConversionTarget target) {
        return switch (target) {
            case INT -> (int) num;
            case LONG -> (long) num;
            case FLOAT -> (float) num;
            case DOUBLE -> num;
            case STRING -> String.valueOf(num);
        };
    }

    private static double toDouble(Object val) {
        if (val instanceof Number n) return n.doubleValue();
        if (val instanceof Boolean b) return b ? 1.0 : 0.0;
        if (val instanceof String s) {
            try { return Double.parseDouble(s); } catch (NumberFormatException ignored) {}
        }
        return 0.0;
    }
}
