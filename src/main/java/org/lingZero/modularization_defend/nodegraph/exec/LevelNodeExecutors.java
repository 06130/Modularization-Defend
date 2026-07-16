package org.lingZero.modularization_defend.nodegraph.exec;

import org.lingZero.modularization_defend.nodegraph.eval.NodeGraphEvaluator;
import org.lingZero.modularization_defend.nodegraph.node.level.BranchNode;

/**
 * 关卡域节点注册中心——为事件节点注册触发条件、为动作节点注册执行器、
 * 为查询节点注册数据流求值器。在模组初始化时调用 {@link #init()}。
 *
 * <h3>外部上下文 Key（由 LevelControllerBlockEntity 每 tick 注入）</h3>
 * <ul>
 *   <li>{@code "level:running"} — 关卡是否运行中 (Boolean)</li>
 *   <li>{@code "level:time"} — 关卡运行秒数 (Float)</li>
 *   <li>{@code "level:wave"} — 当前波次 (Integer)</li>
 *   <li>{@code "level:enemies"} — 存活敌人数 (Integer)</li>
 *   <li>{@code "level:waveCleared"} — 波次是否清空 (Boolean)</li>
 *   <li>{@code "level:leaks"} — 累计漏怪数 (Integer)</li>
 * </ul>
 */
public final class LevelNodeExecutors {

    public static final String KEY_RUNNING = "level:running";
    public static final String KEY_TIME = "level:time";
    public static final String KEY_WAVE = "level:wave";
    public static final String KEY_ENEMIES = "level:enemies";
    public static final String KEY_WAVE_CLEARED = "level:waveCleared";
    public static final String KEY_LEAKS = "level:leaks";

    private LevelNodeExecutors() {}

    public static void init() {
        registerEventConditions();
        registerActionExecutors();
        registerQueryEvaluators();
    }

    // ==================== 事件条件 ====================

    private static void registerEventConditions() {
        LevelExecEngine.registerCondition("关卡开始", ctx ->
                ctx.evaluator().getExternalInput(KEY_RUNNING) instanceof Boolean b && b);

        LevelExecEngine.registerCondition("波次清空", ctx ->
                ctx.evaluator().getExternalInput(KEY_WAVE_CLEARED) instanceof Boolean b && b);

        LevelExecEngine.registerCondition("条件触发", ctx -> ctx.inputAsBool("condition"));

        LevelExecEngine.registerCondition("漏怪上限", ctx -> {
            Object leaks = ctx.evaluator().getExternalInput(KEY_LEAKS);
            int limit = ctx.asInt(ctx.option("limit"), 10);
            return leaks instanceof Number n && n.intValue() >= limit;
        });
    }

    // ==================== 动作执行器 ====================

    private static void registerActionExecutors() {
        LevelExecEngine.registerExecutor("生成波次", ctx -> {
            String entityId = ctx.option("entityId");
            // count 端口：连线取动态值，未连线取内嵌常量（默认 3）
            int count = ctx.asInt(ctx.input("count"), 3);
            int redDoorId = ctx.asInt(ctx.option("redDoorId"), 1);
            int blueDoorId = ctx.asInt(ctx.option("blueDoorId"), 1);
            if (entityId != null && !entityId.isBlank() && count > 0) {
                ctx.host().spawnWave(entityId, count, redDoorId, blueDoorId);
            }
            return LevelActionNode.PORT_THEN;
        });

        LevelExecEngine.registerExecutor("间隔生成波次", ctx -> {
            String entityId = ctx.option("entityId");
            int count = ctx.asInt(ctx.input("count"), 3);
            float intervalSeconds = ctx.asFloat(ctx.input("interval"), 1.0f);
            int redDoorId = ctx.asInt(ctx.option("redDoorId"), 1);
            int blueDoorId = ctx.asInt(ctx.option("blueDoorId"), 1);
            if (entityId == null || entityId.isBlank() || count <= 0) {
                return LevelActionNode.PORT_THEN;
            }
            int intervalTicks = Math.max(1, Math.round(intervalSeconds * 20));
            ctx.host().scheduleIntervalWave(entityId, count, intervalTicks,
                    redDoorId, blueDoorId, ctx.nodeUid(), LevelActionNode.PORT_THEN);
            // 中断链，全部生成完毕后由宿主从 then 端口恢复
            return null;
        });

        LevelExecEngine.registerExecutor("延迟", ctx -> {
            float seconds = ctx.asFloat(ctx.input("seconds"), 1.0f);
            int delayTicks = Math.max(1, Math.round(seconds * 20));
            ctx.host().scheduleResume(ctx.nodeUid(), LevelActionNode.PORT_THEN, delayTicks);
            // 中断链，到期后由宿主从 then 端口恢复
            return null;
        });

        LevelExecEngine.registerExecutor("条件分支", ctx ->
                ctx.inputAsBool("condition") ? BranchNode.PORT_TRUE : BranchNode.PORT_FALSE);

        LevelExecEngine.registerExecutor("发送消息", ctx -> {
            String message = ctx.option("message");
            if (message != null && !message.isBlank()) {
                ctx.host().broadcastMessage(message);
            }
            return LevelActionNode.PORT_THEN;
        });

        LevelExecEngine.registerExecutor("给予奖励", ctx -> {
            String itemId = ctx.option("itemId");
            int count = ctx.asInt(ctx.option("count"), 1);
            if (itemId != null && !itemId.isBlank() && count > 0) {
                ctx.host().giveReward(itemId, count);
            }
            return LevelActionNode.PORT_THEN;
        });

        LevelExecEngine.registerExecutor("关卡结果", ctx -> {
            Object victory = ctx.option("victory");
            ctx.host().setLevelResult(!(victory instanceof Boolean b) || b);
            return LevelActionNode.PORT_THEN;
        });

        LevelExecEngine.registerExecutor("复位关卡", ctx -> {
            Object restart = ctx.option("restart");
            ctx.host().resetLevelFromGraph(restart instanceof Boolean b && b);
            // 终点节点：复位已清空运行时状态，中断链
            return null;
        });
    }

    // ==================== 查询节点求值器（数据流） ====================

    private static void registerQueryEvaluators() {
        NodeGraphEvaluator.registerEvaluator("关卡时间", ctx -> {
            if (!"time".equals(ctx.portId())) return null;
            Object t = ctx.evaluator().getExternalInput(KEY_TIME);
            return t instanceof Number n ? n.floatValue() : 0f;
        });

        NodeGraphEvaluator.registerEvaluator("存活敌人数", ctx -> {
            if (!"count".equals(ctx.portId())) return null;
            Object c = ctx.evaluator().getExternalInput(KEY_ENEMIES);
            return c instanceof Number n ? n.intValue() : 0;
        });

        NodeGraphEvaluator.registerEvaluator("当前波次", ctx -> {
            if (!"wave".equals(ctx.portId())) return null;
            Object w = ctx.evaluator().getExternalInput(KEY_WAVE);
            return w instanceof Number n ? n.intValue() : 0;
        });

        NodeGraphEvaluator.registerEvaluator("漏怪数", ctx -> {
            if (!"count".equals(ctx.portId())) return null;
            Object l = ctx.evaluator().getExternalInput(KEY_LEAKS);
            return l instanceof Number n ? n.intValue() : 0;
        });
    }
}
