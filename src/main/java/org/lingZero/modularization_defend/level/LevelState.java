package org.lingZero.modularization_defend.level;

/**
 * 关卡生命周期状态。
 */
public enum LevelState {
    /** 待机——未运行，可编辑图或启动 */
    IDLE,
    /** 运行中——每 tick 驱动节点图 */
    RUNNING,
    /** 胜利终态 */
    VICTORY,
    /** 失败终态 */
    DEFEAT;

    public static LevelState byName(String name) {
        for (LevelState s : values()) {
            if (s.name().equals(name)) return s;
        }
        return IDLE;
    }
}
