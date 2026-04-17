package org.lingZero.m_defend.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 调试日志工具类
 * 将调试日志输出到单独的日志文件中，方便查看和调试
 * 使用独立线程异步写入日志，避免阻塞主线程
 */
public class DebugLogger {
    private static final Logger DEFAULT_LOGGER = LogManager.getLogger("Minecraft");
    private static Logger debugLogger;
    private static boolean initialized = false;
    private static volatile boolean enabled = true; // 标记调试日志是否启用
    
    /**
     * 日志级别枚举（按严重程度从低到高排序）
     */
    public enum LogLevel {
        TRACE(0),   // 追踪级别，最详细
        DEBUG(1),   // 调试级别
        INFO(2),    // 信息级别
        WARN(3),    // 警告级别
        ERROR(4);   // 错误级别，最严重
        
        private final int level;
        
        LogLevel(int level) {
            this.level = level;
        }
        
        public int getLevel() {
            return level;
        }
        
        /**
         * 从字符串解析日志级别
         * @param name 级别名称
         * @return 对应的LogLevel，如果无法识别则返回DEBUG
         */
        public static LogLevel fromString(String name) {
            if (name == null) return DEBUG;
            try {
                return LogLevel.valueOf(name.toUpperCase());
            } catch (IllegalArgumentException e) {
                DEFAULT_LOGGER.warn("未知的日志级别: {}，使用默认级别DEBUG", name);
                return DEBUG;
            }
        }
    }
    
    // 当前日志级别
    private static volatile LogLevel currentLogLevel = LogLevel.DEBUG;
    
    // 日志写入队列和线程
    private static final BlockingQueue<LogEntry> LOG_QUEUE = new LinkedBlockingQueue<>();
    private static final Thread WRITER_THREAD;
    private static final AtomicBoolean RUNNING = new AtomicBoolean(true);
    
    // 日志条目记录类
    private static class LogEntry {
        String message;
        long timestamp;
        
        LogEntry(String message) {
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    static {
        // 启动日志写入线程
        WRITER_THREAD = new Thread(() -> {
            while (RUNNING.get() || !LOG_QUEUE.isEmpty()) {
                try {
                    LogEntry entry = LOG_QUEUE.take();
                    if (entry != null) {
                        writeToFileDirectly(entry.message);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    DEFAULT_LOGGER.error("日志写入线程异常", e);
                }
            }
            DEFAULT_LOGGER.info("日志写入线程已停止");
        }, "DebugLogger-Writer-Thread");
        WRITER_THREAD.setDaemon(true);
        WRITER_THREAD.start();
    }
    
    /**
     * 初始化调试日志器
     * 应在模组初始化早期调用
     */
    public static void init() {
        init(true, "DEBUG");
    }
    
    /**
     * 初始化调试日志器（支持控制是否启用）
     * @param enable 是否启用调试日志
     */
    public static void init(boolean enable) {
        init(enable, "DEBUG");
    }
    
    /**
     * 初始化调试日志器（支持控制是否启用和日志级别）
     * @param enable 是否启用调试日志
     * @param logLevel 日志级别字符串（TRACE/DEBUG/INFO/WARN/ERROR）
     */
    public static void init(boolean enable, String logLevel) {
        if (initialized) {
            return;
        }
        
        enabled = enable;
        currentLogLevel = LogLevel.fromString(logLevel);
        
        // 如果禁用，直接返回不启动任何资源
        if (!enabled) {
            DEFAULT_LOGGER.info("调试日志系统已禁用，跳过初始化");
            return;
        }
        
        try {
            // 创建调试日志目录
            File debugDir = new File("logs/modularization_defend");
            if (!debugDir.exists()) {
                debugDir.mkdirs();
            }
            
            // 创建调试日志文件路径
            String latestLog = "logs/modularization_defend/debug-latest.log";
            
            // 删除旧的日志文件，创建新的日志文件
            File logFile = new File(latestLog);
            if (logFile.exists()) {
                logFile.delete();
            }
            
            // 写入初始化测试日志
            enqueueLog("===== DebugLogger 初始化成功 =====\n");
            enqueueLog("当前时间：" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n");
            enqueueLog("日志文件路径：" + latestLog + "\n");
            
            // 等待日志写入完成
            Thread.sleep(100);
            
            initialized = true;
            DEFAULT_LOGGER.info("调试日志系统初始化完成，日志文件：{}", latestLog);
            DEFAULT_LOGGER.info("日志写入线程已启动：" + WRITER_THREAD.getName());
            
        } catch (Exception e) {
            DEFAULT_LOGGER.error("调试日志系统初始化失败", e);
            debugLogger = DEFAULT_LOGGER;
        }
    }
    
    /**
     * 将日志消息加入队列
     */
    private static void enqueueLog(String message) {
        // 快速失败：未启用时立即返回，避免任何开销
        if (!enabled || !RUNNING.get()) {
            return;
        }
        LOG_QUEUE.offer(new LogEntry(message));
    }
    
    /**
     * 直接写入日志到文件 (仅在日志线程中调用)
     */
    private static synchronized void writeToFileDirectly(String message) {
        String logFile = "logs/modularization_defend/debug-latest.log";
        try (FileWriter writer = new FileWriter(logFile, true)) {
            writer.write(message);
        } catch (IOException e) {
            DEFAULT_LOGGER.error("写入日志文件失败", e);
        }
    }
    
    /**
     * 关闭调试日志系统
     * 当配置中禁用调试模式时调用
     */
    public static void shutdown() {
        if (!initialized) {
            return;
        }
        
        // 先设置enabled为false，阻止新的日志入队
        enabled = false;
        
        // 停止运行标志
        RUNNING.set(false);
        WRITER_THREAD.interrupt();
        
        // 清空队列中的待写入日志
        LOG_QUEUE.clear();
        
        // 删除已创建的日志文件
        try {
            File logFile = new File("logs/modularization_defend/debug-latest.log");
            if (logFile.exists()) {
                logFile.delete();
            }
        } catch (Exception e) {
            DEFAULT_LOGGER.error("删除日志文件失败", e);
        }
        
        initialized = false;
        debugLogger = null;
        currentLogLevel = LogLevel.DEBUG; // 重置日志级别
        
        DEFAULT_LOGGER.info("调试日志系统已关闭");
    }
    
    /**
     * 设置日志级别
     * @param logLevel 日志级别字符串（TRACE/DEBUG/INFO/WARN/ERROR）
     */
    public static void setLogLevel(String logLevel) {
        currentLogLevel = LogLevel.fromString(logLevel);
        if (enabled && initialized) {
            DEFAULT_LOGGER.info("调试日志级别已设置为: {}", currentLogLevel.name());
        }
    }
    
    /**
     * 设置日志级别
     * @param level 日志级别枚举
     */
    public static void setLogLevel(LogLevel level) {
        if (level != null) {
            currentLogLevel = level;
            if (enabled && initialized) {
                DEFAULT_LOGGER.info("调试日志级别已设置为: {}", currentLogLevel.name());
            }
        }
    }
    
    /**
     * 获取当前日志级别
     * @return 当前日志级别
     */
    public static LogLevel getCurrentLogLevel() {
        return currentLogLevel;
    }
    
    /**
     * 检查是否已初始化
     * @return true 如果已初始化
     */
    public static boolean isInitialized() {
        return initialized;
    }
    
    /**
     * 检查调试日志是否启用（用于性能优化）
     * @return true 如果调试日志已初始化并启用
     */
    public static boolean isDebugEnabled() {
        return enabled && initialized;
    }
    
    /**
     * 获取调试日志器
     * @return 调试日志器实例
     */
    public static Logger getLogger() {
        if (!initialized) {
            init();
        }
        return debugLogger != null ? debugLogger : DEFAULT_LOGGER;
    }
    
    /**
     * 输出调试级别日志
     * @param message 日志消息
     */
    public static void debug(String message) {
        if (!enabled || currentLogLevel.level > LogLevel.DEBUG.level) return; // 快速失败，避免字符串格式化开销
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        String thread = Thread.currentThread().getName();
        enqueueLog(String.format("%s [%s] [DEBUG] ModularizationDefend_Debug - %s\n", timestamp, thread, message));
    }
    
    /**
     * 输出调试级别日志（带格式化参数）
     * @param message 日志消息
     * @param args 格式化参数
     */
    public static void debug(String message, Object... args) {
        if (!enabled || currentLogLevel.level > LogLevel.DEBUG.level) return; // 快速失败，避免字符串格式化开销
        debug(String.format(message, args));
    }
    
    /**
     * 输出信息级别日志
     * @param message 日志消息
     */
    public static void info(String message) {
        if (!enabled || currentLogLevel.level > LogLevel.INFO.level) return; // 快速失败，避免字符串格式化开销
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        String thread = Thread.currentThread().getName();
        enqueueLog(String.format("%s [%s] [INFO] ModularizationDefend_Debug - %s\n", timestamp, thread, message));
    }
    
    /**
     * 输出信息级别日志（带格式化参数）
     * @param message 日志消息
     * @param args 格式化参数
     */
    public static void info(String message, Object... args) {
        if (!enabled || currentLogLevel.level > LogLevel.INFO.level) return; // 快速失败，避免字符串格式化开销
        info(String.format(message, args));
    }
    
    /**
     * 输出警告级别日志
     * @param message 日志消息
     */
    public static void warn(String message) {
        if (!enabled || currentLogLevel.level > LogLevel.WARN.level) return; // 快速失败，避免字符串格式化开销
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        String thread = Thread.currentThread().getName();
        enqueueLog(String.format("%s [%s] [WARN] ModularizationDefend_Debug - %s\n", timestamp, thread, message));
    }
    
    /**
     * 输出警告级别日志（带格式化参数）
     * @param message 日志消息
     * @param args 格式化参数
     */
    public static void warn(String message, Object... args) {
        if (!enabled || currentLogLevel.level > LogLevel.WARN.level) return; // 快速失败，避免字符串格式化开销
        warn(String.format(message, args));
    }
    
    /**
     * 输出错误级别日志
     * @param message 日志消息
     */
    public static void error(String message) {
        if (!enabled || currentLogLevel.level > LogLevel.ERROR.level) return; // 快速失败，避免字符串格式化开销
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        String thread = Thread.currentThread().getName();
        enqueueLog(String.format("%s [%s] [ERROR] ModularizationDefend_Debug - %s\n", timestamp, thread, message));
    }
    
    /**
     * 输出错误级别日志（带格式化参数）
     * @param message 日志消息
     * @param args 格式化参数
     */
    public static void error(String message, Object... args) {
        if (!enabled || currentLogLevel.level > LogLevel.ERROR.level) return; // 快速失败，避免字符串格式化开销
        error(String.format(message, args));
    }
    
    /**
     * 输出错误级别日志 (带异常堆栈)
     * @param message 日志消息
     * @param throwable 异常对象
     */
    public static void error(String message, Throwable throwable) {
        if (!enabled || currentLogLevel.level > LogLevel.ERROR.level) return; // 快速失败，避免字符串格式化开销
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        String thread = Thread.currentThread().getName();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s [%s] [ERROR] ModularizationDefend_Debug - %s\n", timestamp, thread, message));
        sb.append(throwable.toString()).append("\n");
        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append("\tat ").append(element).append("\n");
        }
        enqueueLog(sb.toString());
    }
    
    /**
     * 输出追踪级别日志
     * @param message 日志消息
     */
    public static void trace(String message) {
        if (!enabled || currentLogLevel.level > LogLevel.TRACE.level) return; // 快速失败，避免字符串格式化开销
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        String thread = Thread.currentThread().getName();
        enqueueLog(String.format("%s [%s] [TRACE] ModularizationDefend_Debug - %s\n", timestamp, thread, message));
    }
    
    /**
     * 输出追踪级别日志（带格式化参数）
     * @param message 日志消息
     * @param args 格式化参数
     */
    public static void trace(String message, Object... args) {
        if (!enabled || currentLogLevel.level > LogLevel.TRACE.level) return; // 快速失败，避免字符串格式化开销
        trace(String.format(message, args));
    }
}
