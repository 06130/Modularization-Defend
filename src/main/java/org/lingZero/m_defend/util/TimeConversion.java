package org.lingZero.m_defend.util;

/**
 * Minecraft Tick 时间转换工具类
 * 将 Tick 数转换为人类可读的时间格式
 * 1 tick = 0.05 秒 (20 ticks = 1 秒)
 */
public final class TimeConversion {
    
    // 预计算的常量（编译时常量，无运行时开销）
    private static final long TICKS_PER_SECOND = 20L;
    private static final long TICKS_PER_MINUTE = 1200L;           // 20 * 60
    private static final long TICKS_PER_HOUR = 72000L;            // 1200 * 60
    private static final long TICKS_PER_DAY = 1728000L;           // 72000 * 24
    private static final long TICKS_PER_MONTH = 51840000L;        // 1728000 * 30
    private static final long TICKS_PER_YEAR = 630720000L;        // 1728000 * 365
    
    // 预计算的对数表，用于快速定位最大单位（可选优化）
    private static final int[] THRESHOLDS = {
        0,                          // 占位
        (int)TICKS_PER_SECOND,      // 秒阈值
        (int)TICKS_PER_MINUTE,      // 分钟阈值
        (int)TICKS_PER_HOUR,        // 小时阈值
        (int)TICKS_PER_DAY,         // 天阈值
        (int)TICKS_PER_MONTH,       // 月阈值
        (int)TICKS_PER_YEAR         // 年阈值
    };
    
    // 单位后缀字符数组（避免重复字符串创建）
    private static final char[] UNIT_SYMBOLS = {'S', 'M', 'H', 'D', 'M', 'Y'};
    
    /**
     * 将 Tick 数转换为人类可读的时间字符串
     * 自动选择最合适的时间单位
     * 
     * @param ticks Tick 数
     * @return 格式化后的时间字符串（如：1H, 30M, 15S 等）
     */
    public static String convertToHumanReadable(long ticks) {
        if (ticks <= 0) {
            return ticks == 0 ? "0S" : "Invalid";
        }
        
        // 快速路径：小数值直接计算
        if (ticks < TICKS_PER_MINUTE) {
            return String.valueOf(Math.max(1, ticks / TICKS_PER_SECOND)) + 'S';
        }
        
        // 使用除法快速定位主单位
        int unitIndex = getUnitIndex(ticks);
        
        switch (unitIndex) {
            case 5: // 年
                return formatTwoUnits(ticks, TICKS_PER_YEAR, TICKS_PER_MONTH, 'Y', 'M');
            case 4: // 月
                return formatTwoUnits(ticks, TICKS_PER_MONTH, TICKS_PER_DAY, 'M', 'D');
            case 3: // 天
                return formatTwoUnits(ticks, TICKS_PER_DAY, TICKS_PER_HOUR, 'D', 'H');
            case 2: // 小时
                return formatTwoUnits(ticks, TICKS_PER_HOUR, TICKS_PER_MINUTE, 'H', 'M');
            case 1: // 分钟
                return formatTwoUnits(ticks, TICKS_PER_MINUTE, TICKS_PER_SECOND, 'M', 'S');
            default: // 秒
                return String.valueOf(Math.max(1, ticks / TICKS_PER_SECOND)) + 'S';
        }
    }
    
    /**
     * 快速获取单位索引（从大到小）
     */
    private static int getUnitIndex(long ticks) {
        if (ticks >= TICKS_PER_YEAR) return 5;
        if (ticks >= TICKS_PER_MONTH) return 4;
        if (ticks >= TICKS_PER_DAY) return 3;
        if (ticks >= TICKS_PER_HOUR) return 2;
        if (ticks >= TICKS_PER_MINUTE) return 1;
        return 0;
    }
    
    /**
     * 格式化两个单位（主单位 + 次单位）
     */
    private static String formatTwoUnits(long ticks, long mainUnit, long subUnit, 
                                         char mainSymbol, char subSymbol) {
        long mainValue = ticks / mainUnit;
        long remaining = ticks % mainUnit;
        
        if (remaining == 0) {
            return mainValue + String.valueOf(mainSymbol);
        }
        
        long subValue = remaining / subUnit;
        if (subValue == 0) {
            return mainValue + String.valueOf(mainSymbol);
        }
        
        // 使用 StringBuilder 避免多次字符串拼接
        return new StringBuilder(12)
            .append(mainValue).append(mainSymbol)
            .append(subValue).append(subSymbol)
            .toString();
    }
    
    /**
     * 将 Tick 数转换为详细的时间字符串（包含所有单位）
     * 
     * @param ticks Tick 数
     * @return 详细格式化后的时间字符串（如：1Y 2M 3D 4H 5M 6S）
     */
    public static String convertToDetailed(long ticks) {
        if (ticks <= 0) {
            return ticks == 0 ? "0S" : "Invalid";
        }
        
        // 预分配 StringBuilder 容量，减少扩容
        StringBuilder result = new StringBuilder(20);
        long remaining = ticks;
        
        // 年
        long years = remaining / TICKS_PER_YEAR;
        remaining %= TICKS_PER_YEAR;
        if (years > 0) {
            result.append(years).append('Y');
        }
        
        // 月
        long months = remaining / TICKS_PER_MONTH;
        remaining %= TICKS_PER_MONTH;
        if (months > 0) {
            result.append(months).append('M');
        }
        
        // 天
        long days = remaining / TICKS_PER_DAY;
        remaining %= TICKS_PER_DAY;
        if (days > 0) {
            result.append(days).append('D');
        }
        
        // 小时
        long hours = remaining / TICKS_PER_HOUR;
        remaining %= TICKS_PER_HOUR;
        if (hours > 0) {
            result.append(hours).append('H');
        }
        
        // 分钟
        long minutes = remaining / TICKS_PER_MINUTE;
        remaining %= TICKS_PER_MINUTE;
        if (minutes > 0) {
            result.append(minutes).append('M');
        }
        
        // 秒
        long seconds = remaining / TICKS_PER_SECOND;
        if (seconds > 0 || result.length() == 0) {
            result.append(seconds).append('S');
        }
        
        return result.toString();
    }
    
    /**
     * 获取指定单位的 Tick 值（编译时常量，可直接内联）
     */
    public static long getTicksPerYear() {
        return TICKS_PER_YEAR;
    }
    
    public static long getTicksPerMonth() {
        return TICKS_PER_MONTH;
    }
    
    public static long getTicksPerDay() {
        return TICKS_PER_DAY;
    }
    
    public static long getTicksPerHour() {
        return TICKS_PER_HOUR;
    }
    
    public static long getTicksPerMinute() {
        return TICKS_PER_MINUTE;
    }
    
    public static long getTicksPerSecond() {
        return TICKS_PER_SECOND;
    }
}

