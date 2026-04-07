package org.lingZero.m_defend.util;

/**
 * 能源数据单位转换工具类
 * 将大数字转换为带单位的简洁格式（K, M, G, T, E 等）
 * 
 * 单位定义：
 * K = 千 (10³)
 * M = 百万 (10⁶)
 * G = 十亿 (10⁹)
 * T = 万亿 (10¹²)
 * P = 千万亿 (10¹⁵)
 * E = 百京 (10¹⁸)
 * Z = 泽塔 (10²¹)
 * Y = 尧塔 (10²⁴)
 */
public final class EnergydataConversion {
    
    // 预计算的常量（编译时常量）- 使用 double 支持更大范围
    private static final double THOUSAND = 1_000.0;              // 10³ K
    private static final double MILLION = 1_000_000.0;           // 10⁶ M
    private static final double BILLION = 1_000_000_000.0;       // 10⁹ G
    private static final double TRILLION = 1_000_000_000_000.0;  // 10¹² T
    private static final double QUADRILLION = 1_000_000_000_000_000.0;  // 10¹⁵ P
    private static final double QUINTILLION = 1_000_000_000_000_000_000.0; // 10¹⁸ E
    private static final double SEXTILLION = 1_000_000_000_000_000_000_000.0; // 10²¹ Z
    private static final double SEPTILLION = 1_000_000_000_000_000_000_000_000.0; // 10²⁴ Y
    
    // 单位符号数组（避免重复字符串创建）
    private static final char[] UNIT_SYMBOLS = {' ', 'K', 'M', 'G', 'T', 'P', 'E', 'Z', 'Y'};
    
    // 单位阈值数组（用于快速定位）
    private static final double[] UNIT_THRESHOLDS = {
        THOUSAND,      // K 阈值
        MILLION,       // M 阈值
        BILLION,       // G 阈值
        TRILLION,      // T 阈值
        QUADRILLION,   // P 阈值
        QUINTILLION,   // E 阈值
        SEXTILLION,    // Z 阈值
        SEPTILLION     // Y 阈值
    };
    
    /**
     * 将数字转换为带单位的简洁格式（支持 long 和 double）
     * 自动选择最合适的单位
     * 
     * @param value 要转换的数字
     * @return 格式化后的字符串（如：1.5K, 2.3M, 1G 等）
     */
    public static String convertToHumanReadable(long value) {
        return convertToHumanReadable((double) value);
    }
    
    /**
     * 将数字转换为带单位的简洁格式（double 版本，支持超大数值）
     * 
     * @param value 要转换的数字
     * @return 格式化后的字符串（如：1.5K, 2.3M, 1G, 1Z, 1Y 等）
     */
    public static String convertToHumanReadable(double value) {
        if (value < 0) {
            return "Invalid";
        }
        
        if (value == 0) {
            return "0";
        }
        
        // 快速路径：小于 1000 直接返回
        if (value < THOUSAND) {
            return formatDouble(value, 0, ' ');
        }
        
        // 找到合适的单位
        int unitIndex = getUnitIndex(value);
        
        // 计算转换后的值
        double divisor = UNIT_THRESHOLDS[unitIndex];
        double result = value / divisor;
        
        // 根据数值大小决定小数位数
        if (result >= 100) {
            return formatDouble(result, 0, UNIT_SYMBOLS[unitIndex + 1]);
        } else if (result >= 10) {
            return formatDouble(result, 1, UNIT_SYMBOLS[unitIndex + 1]);
        } else {
            return formatDouble(result, 2, UNIT_SYMBOLS[unitIndex + 1]);
        }
    }
    
    /**
     * 将数字转换为精确的带单位格式（保留指定小数位）
     * 
     * @param value 要转换的数字
     * @param decimalPlaces 小数位数
     * @return 格式化后的字符串
     */
    public static String convertWithPrecision(long value, int decimalPlaces) {
        return convertWithPrecision((double) value, decimalPlaces);
    }
    
    /**
     * 将数字转换为精确的带单位格式（double 版本）
     * 
     * @param value 要转换的数字
     * @param decimalPlaces 小数位数
     * @return 格式化后的字符串
     */
    public static String convertWithPrecision(double value, int decimalPlaces) {
        if (value < 0) {
            return "Invalid";
        }
        
        if (value == 0) {
            return "0";
        }
        
        if (value < THOUSAND) {
            return formatDouble(value, 0, ' ');
        }
        
        int unitIndex = getUnitIndex(value);
        double divisor = UNIT_THRESHOLDS[unitIndex];
        double result = value / divisor;
        
        return formatDouble(result, decimalPlaces, UNIT_SYMBOLS[unitIndex + 1]);
    }
    
    /**
     * 获取合适的单位索引（double 版本）
     */
    private static int getUnitIndex(double value) {
        // 从大到小检查，找到最大的适配单位
        if (value >= SEPTILLION) return 7;   // Y
        if (value >= SEXTILLION) return 6;   // Z
        if (value >= QUINTILLION) return 5;  // E
        if (value >= QUADRILLION) return 4;  // P
        if (value >= TRILLION) return 3;     // T
        if (value >= BILLION) return 2;      // G
        if (value >= MILLION) return 1;      // M
        if (value >= THOUSAND) return 0;     // K
        return -1; // 不需要单位
    }
    
    /**
     * 格式化 double 数值和单位
     */
    private static String formatDouble(double value, int decimals, char unit) {
        StringBuilder sb = new StringBuilder(16);
        
        if (decimals == 0) {
            sb.append((long) value);
        } else {
            // 手动格式化小数，避免 DecimalFormat 的开销
            long multiplier = 1;
            for (int i = 0; i < decimals; i++) {
                multiplier *= 10;
            }
            
            long scaled = Math.round(value * multiplier);
            long integerPart = scaled / multiplier;
            long fractionalPart = scaled % multiplier;
            
            sb.append(integerPart);
            sb.append('.');
            
            // 补齐前导零
            for (int i = 0; i < decimals - 1; i++) {
                if (fractionalPart < multiplier / Math.pow(10, i + 1)) {
                    sb.append('0');
                }
            }
            sb.append(Math.abs(fractionalPart));
        }
        
        sb.append(unit);
        return sb.toString();
    }
    
    /**
     * 将数字转换为详细格式（显示具体数值）
     * 
     * @param value 要转换的数字
     * @return 格式化后的字符串（如：1,234,567）
     */
    public static String convertToDetailed(long value) {
        if (value < 0) {
            return "Invalid";
        }
        
        return Long.toString(value);
    }
    
    /**
     * 将带单位的字符串转换回原始数字（double 版本）
     * 
     * @param formatted 格式化的字符串（如："1.5K", "2M"）
     * @return 原始数字
     */
    public static double parseToDouble(String formatted) {
        if (formatted == null || formatted.isEmpty()) {
            return 0.0;
        }
        
        formatted = formatted.trim().toUpperCase();
        
        char lastChar = formatted.charAt(formatted.length() - 1);
        double multiplier = 1.0;
        
        switch (lastChar) {
            case 'K':
                multiplier = THOUSAND;
                formatted = formatted.substring(0, formatted.length() - 1);
                break;
            case 'M':
                multiplier = MILLION;
                formatted = formatted.substring(0, formatted.length() - 1);
                break;
            case 'G':
                multiplier = BILLION;
                formatted = formatted.substring(0, formatted.length() - 1);
                break;
            case 'T':
                multiplier = TRILLION;
                formatted = formatted.substring(0, formatted.length() - 1);
                break;
            case 'P':
                multiplier = QUADRILLION;
                formatted = formatted.substring(0, formatted.length() - 1);
                break;
            case 'E':
                multiplier = QUINTILLION;
                formatted = formatted.substring(0, formatted.length() - 1);
                break;
            case 'Z':
                multiplier = SEXTILLION;
                formatted = formatted.substring(0, formatted.length() - 1);
                break;
            case 'Y':
                multiplier = SEPTILLION;
                formatted = formatted.substring(0, formatted.length() - 1);
                break;
            default:
                // 没有单位
                break;
        }
        
        try {
            return Double.parseDouble(formatted.trim()) * multiplier;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    
    /**
     * 将带单位的字符串转换回原始数字（long 版本，有限制）
     * 
     * @param formatted 格式化的字符串（如："1.5K", "2M"）
     * @return 原始数字
     */
    public static long parseToLong(String formatted) {
        return (long) parseToDouble(formatted);
    }
    
    /**
     * 获取各单位的乘数
     */
    public static double getThousand() {
        return THOUSAND;
    }
    
    public static double getMillion() {
        return MILLION;
    }
    
    public static double getBillion() {
        return BILLION;
    }
    
    public static double getTrillion() {
        return TRILLION;
    }
    
    public static double getQuadrillion() {
        return QUADRILLION;
    }
    
    public static double getQuintillion() {
        return QUINTILLION;
    }
    
    public static double getSextillion() {
        return SEXTILLION;
    }
    
    public static double getSeptillion() {
        return SEPTILLION;
    }
}
