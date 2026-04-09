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
        if (initialized) {
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
            DEFAULT_LOGGER.error("调试日志系统初始化失败，将使用主日志文件", e);
            debugLogger = DEFAULT_LOGGER;
        }
    }
    
    /**
     * 将日志消息加入队列
     */
    private static void enqueueLog(String message) {
        if (!RUNNING.get()) {
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
        
        DEFAULT_LOGGER.info("调试日志系统已关闭");
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
        return initialized;
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
        debug(String.format(message, args));
    }
    
    /**
     * 输出信息级别日志
     * @param message 日志消息
     */
    public static void info(String message) {
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
        info(String.format(message, args));
    }
    
    /**
     * 输出警告级别日志
     * @param message 日志消息
     */
    public static void warn(String message) {
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
        warn(String.format(message, args));
    }
    
    /**
     * 输出错误级别日志
     * @param message 日志消息
     */
    public static void error(String message) {
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
        error(String.format(message, args));
    }
    
    /**
     * 输出错误级别日志 (带异常堆栈)
     * @param message 日志消息
     * @param throwable 异常对象
     */
    public static void error(String message, Throwable throwable) {
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
        trace(String.format(message, args));
    }
}
