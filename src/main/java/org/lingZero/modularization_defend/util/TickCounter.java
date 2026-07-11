package org.lingZero.modularization_defend.util;

import net.minecraft.nbt.CompoundTag;

/**
 * 独立的Tick计数器工具类。
 * 封装tick累加、格式化输出和NBT持久化逻辑，
 * 可在任何BlockEntity或组件中复用。
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * TickCounter counter = new TickCounter();
 * counter.increment();           // 每tick调用
 * long ticks = counter.getCount();
 * String time = counter.getElapsedTimeFormatted(); // "01:23:45"
 * }</pre>
 */
public class TickCounter {

    /** 每秒tick数（Minecraft固定20 TPS） */
    public static final int TICKS_PER_SECOND = 20;
    public static final int TICKS_PER_MINUTE = TICKS_PER_SECOND * 60;
    public static final int TICKS_PER_HOUR = TICKS_PER_MINUTE * 60;

    private long tickCount;

    public TickCounter() {
        this.tickCount = 0;
    }

    /** tick自增1 */
    public void increment() {
        tickCount++;
    }

    /** 增加指定tick数量 */
    public void add(long delta) {
        tickCount += delta;
    }

    /** 获取当前累计tick数 */
    public long getCount() {
        return tickCount;
    }

    /** 重置计数器 */
    public void reset() {
        tickCount = 0;
    }

    // ==================== 时间格式化 ====================

    /** 获取运行总秒数 */
    public long getElapsedSeconds() {
        return tickCount / TICKS_PER_SECOND;
    }

    /** 获取运行总分钟数 */
    public long getElapsedMinutes() {
        return tickCount / TICKS_PER_MINUTE;
    }

    /** 获取运行总小时数 */
    public long getElapsedHours() {
        return tickCount / TICKS_PER_HOUR;
    }

    /** 格式化为 "HH:MM:SS" */
    public String getElapsedTimeFormatted() {
        long totalSeconds = getElapsedSeconds();
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    /** 获取当前游戏日内的时间（0-23999 ticks = 一个MC日） */
    public long getDayTime() {
        return tickCount % 24000;
    }

    /** 获取经过的游戏日数 */
    public long getElapsedDays() {
        return tickCount / 24000;
    }

    // ==================== NBT持久化 ====================

    /** 将计数器状态写入NBT */
    public void saveToTag(CompoundTag tag, String key) {
        tag.putLong(key, tickCount);
    }

    /** 从NBT读取计数器状态 */
    public void loadFromTag(CompoundTag tag, String key) {
        tickCount = tag.getLong(key);
    }
}
