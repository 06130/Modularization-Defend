package org.lingZero.modularization_defend.level;

import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.AbstractNodeModel;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.ICustomNodeModel;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.NodeModel;
import com.lowdragmc.lowdraglib2.nodegraphtookit.model.node.PortModel;
import com.mojang.logging.LogUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import org.lingZero.modularization_defend.nodegraph.TurretLogicGraph;
import org.lingZero.modularization_defend.nodegraph.eval.NodeGraphEvaluator;
import org.lingZero.modularization_defend.nodegraph.exec.ILevelHost;
import org.lingZero.modularization_defend.nodegraph.exec.LevelExecEngine;
import org.lingZero.modularization_defend.nodegraph.exec.LevelNodeExecutors;
import org.lingZero.modularization_defend.nodegraph.node.DataStoreNode;
import org.lingZero.modularization_defend.nodegraph.node.TimerNode;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 关卡图运行时——节点图求值引擎的宿主（仅服务端使用）。
 *
 * <p>职责：</p>
 * <ol>
 *   <li>从 NBT 惰性构建/缓存图实例（图变更时失效重建）</li>
 *   <li>每 tick 注入外部上下文（关卡时间、波次、敌人数等）</li>
 *   <li>维护有状态节点（计时器/数据存储）的状态并持久化</li>
 *   <li>检查事件节点条件，上升沿触发执行链</li>
 * </ol>
 */
public class LevelGraphRuntime {

    private static final Logger LOGGER = LogUtils.getLogger();

    private CompoundTag graphData = new CompoundTag();
    private TurretLogicGraph graph;
    private NodeGraphEvaluator evaluator;
    private boolean graphBroken;

    /** 计时器节点已计 tick 数（nodeUid → ticks） */
    private final Map<String, Integer> timerTicks = new HashMap<>();
    /** 数据存储节点当前值（nodeUid → value） */
    private final Map<String, Float> storeValues = new HashMap<>();
    /** 上一 tick 条件为真的事件节点 uid（用于边沿检测） */
    private final Set<String> prevTrue = new HashSet<>();

    // ==================== 图管理 ====================

    /** 设置图数据并使缓存的图实例失效 */
    public void setGraphData(CompoundTag tag) {
        this.graphData = tag != null ? tag : new CompoundTag();
        this.graph = null;
        this.evaluator = null;
        this.graphBroken = false;
    }

    public boolean hasGraph() {
        return !graphData.isEmpty();
    }

    /** 清空运行时状态（关卡启动/复位时调用） */
    public void resetState() {
        timerTicks.clear();
        storeValues.clear();
        prevTrue.clear();
    }

    private boolean ensureGraph(HolderLookup.Provider registries) {
        if (graph != null) return true;
        if (graphBroken || graphData.isEmpty()) return false;
        try {
            var newGraph = new TurretLogicGraph();
            newGraph.graphModel.deserializeNBT(registries, graphData);
            graph = newGraph;
            evaluator = new NodeGraphEvaluator(graph.graphModel);
            return true;
        } catch (Exception e) {
            LOGGER.error("关卡节点图反序列化失败，图已禁用", e);
            graphBroken = true;
            return false;
        }
    }

    // ==================== tick 驱动 ====================

    /**
     * 驱动图一个 tick。
     *
     * @param host        关卡宿主（动作节点的副作用出口）
     * @param registries  注册表访问（用于图反序列化）
     * @param timeSeconds 关卡运行秒数
     * @param wave        当前波次
     * @param enemies     存活敌人数
     * @param waveCleared 波次是否清空
     * @param leaks       累计漏怪数
     */
    public void tick(ILevelHost host, HolderLookup.Provider registries,
                     float timeSeconds, int wave, int enemies, boolean waveCleared, int leaks) {
        if (!ensureGraph(registries)) return;

        evaluator.setExternalInput(LevelNodeExecutors.KEY_RUNNING, true);
        evaluator.setExternalInput(LevelNodeExecutors.KEY_TIME, timeSeconds);
        evaluator.setExternalInput(LevelNodeExecutors.KEY_WAVE, wave);
        evaluator.setExternalInput(LevelNodeExecutors.KEY_ENEMIES, enemies);
        evaluator.setExternalInput(LevelNodeExecutors.KEY_WAVE_CLEARED, waveCleared);
        evaluator.setExternalInput(LevelNodeExecutors.KEY_LEAKS, leaks);

        // 先发布上一 tick 的有状态节点值，供本 tick 求值使用
        publishStatefulValues();
        evaluator.clearCache();

        // 基于旧值计算有状态节点的新状态，再发布并清缓存，使事件条件看到最新值
        updateStatefulNodes();
        publishStatefulValues();
        evaluator.clearCache();

        checkEvents(host);
    }

    /** 将计时器/数据存储的当前状态发布为外部输入（求值引擎的既定契约 key） */
    private void publishStatefulValues() {
        timerTicks.forEach((uid, ticks) ->
                evaluator.setExternalInput("timer:elapsed:" + uid, ticks / 20f));
        storeValues.forEach((uid, value) ->
                evaluator.setExternalInput("store:" + uid, value));
    }

    /** 更新计时器/数据存储节点的状态（由宿主驱动，引擎本身无状态） */
    private void updateStatefulNodes() {
        for (AbstractNodeModel nodeModel : graph.graphModel.getNodeModels()) {
            if (!(nodeModel instanceof ICustomNodeModel cn) || !(nodeModel instanceof NodeModel nm)) continue;
            String uid = nodeModel.getUid().toString();

            if (cn.getNode() instanceof TimerNode) {
                boolean reset = asBool(evaluator.getInputPortValue(nm, "reset"));
                boolean trigger = asBool(evaluator.getInputPortValue(nm, "trigger"));
                Object autoResetOpt = evaluator.getOptionValue(nm, "autoReset");
                boolean autoReset = !(autoResetOpt instanceof Boolean b) || b;
                int ticks = timerTicks.getOrDefault(uid, 0);
                if (reset) ticks = 0;
                else if (trigger) ticks++;
                else if (autoReset) ticks = 0;
                timerTicks.put(uid, ticks);

            } else if (cn.getNode() instanceof DataStoreNode) {
                PortModel writePort = nm.getInputsById().get("write");
                PortModel valuePort = nm.getInputsById().get("value");
                if (valuePort == null || !valuePort.isConnected()) continue;
                // write 端口未连线时，只要 value 有输入就写入
                boolean write = writePort == null || !writePort.isConnected()
                        || asBool(evaluator.evaluatePort(writePort));
                if (write && evaluator.evaluatePort(valuePort) instanceof Number n) {
                    storeValues.put(uid, n.floatValue());
                }
            }
        }
    }

    /** 检查所有事件节点，条件上升沿时触发其执行链 */
    private void checkEvents(ILevelHost host) {
        for (AbstractNodeModel nodeModel : graph.graphModel.getNodeModels()) {
            if (!LevelExecEngine.isEventNode(nodeModel)) continue;
            String uid = nodeModel.getUid().toString();
            boolean now = LevelExecEngine.testCondition(nodeModel, host, evaluator);
            boolean was = prevTrue.contains(uid);
            if (now && !was) {
                LevelExecEngine.triggerChain(nodeModel, host, evaluator);
            }
            if (now) prevTrue.add(uid);
            else prevTrue.remove(uid);
        }
    }

    /** 从指定节点的指定输出端口恢复执行链（延迟/间隔波次到期后由宿主调用） */
    public void resumeChain(ILevelHost host, HolderLookup.Provider registries, String nodeUid, String portId) {
        if (!ensureGraph(registries)) return;
        try {
            var model = graph.graphModel.getModel(java.util.UUID.fromString(nodeUid));
            if (model instanceof AbstractNodeModel node) {
                LevelExecEngine.resumeFrom(node, portId, host, evaluator);
            }
        } catch (IllegalArgumentException ignored) {
            // uid 非法（图被替换过），静默丢弃该待续任务
        }
    }

    private static boolean asBool(Object val) {
        return val instanceof Boolean b && b;
    }

    // ==================== 状态持久化 ====================

    public CompoundTag saveState() {
        CompoundTag tag = new CompoundTag();
        CompoundTag timers = new CompoundTag();
        timerTicks.forEach(timers::putInt);
        tag.put("Timers", timers);
        CompoundTag stores = new CompoundTag();
        storeValues.forEach(stores::putFloat);
        tag.put("Stores", stores);
        ListTag prev = new ListTag();
        prevTrue.forEach(uid -> prev.add(StringTag.valueOf(uid)));
        tag.put("PrevTrue", prev);
        return tag;
    }

    public void loadState(CompoundTag tag) {
        resetState();
        CompoundTag timers = tag.getCompound("Timers");
        for (String key : timers.getAllKeys()) timerTicks.put(key, timers.getInt(key));
        CompoundTag stores = tag.getCompound("Stores");
        for (String key : stores.getAllKeys()) storeValues.put(key, stores.getFloat(key));
        ListTag prev = tag.getList("PrevTrue", Tag.TAG_STRING);
        for (int i = 0; i < prev.size(); i++) prevTrue.add(prev.getString(i));
    }
}
