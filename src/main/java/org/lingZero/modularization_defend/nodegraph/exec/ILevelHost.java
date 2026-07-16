package org.lingZero.modularization_defend.nodegraph.exec;

/**
 * 关卡宿主接口——动作节点通过该接口对世界产生副作用。
 * 由 LevelControllerBlockEntity 实现，使执行流框架不直接依赖具体方块实体。
 */
public interface ILevelHost {

    /** 在指定 ID 的红门处生成一波敌人（目标为指定 ID 的蓝门），并纳入关卡追踪 */
    void spawnWave(String entityId, int count, int redDoorId, int blueDoorId);

    /** 调度延迟恢复：delayTicks 后从指定节点的指定输出端口继续执行链 */
    void scheduleResume(String nodeUid, String portId, int delayTicks);

    /**
     * 调度间隔波次：每 intervalTicks 在红门生成 1 只敌人，共 count 只，
     * 全部生成完毕后从指定节点的指定输出端口继续执行链。
     */
    void scheduleIntervalWave(String entityId, int count, int intervalTicks,
                              int redDoorId, int blueDoorId, String resumeNodeUid, String resumePortId);

    /** 向宿主附近的玩家广播消息 */
    void broadcastMessage(String message);

    /** 给予宿主附近的玩家奖励物品 */
    void giveReward(String itemId, int count);

    /** 判定关卡结果并结束运行（true 胜利 / false 失败） */
    void setLevelResult(boolean victory);

    /** 复位关卡（清理敌人与运行时状态回到待机）；restart 为 true 时复位后立即重新开始 */
    void resetLevelFromGraph(boolean restart);
}
