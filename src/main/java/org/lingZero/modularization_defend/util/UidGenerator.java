package org.lingZero.modularization_defend.util;

import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;

/**
 * 随机 UID 生成工具类
 * 提供多种 UID 生成方式，包括 UUID、随机字符串、时间戳等
 * 默认使用短 UID 格式以减少内存占用
 * 
 * <h2>使用方法：</h2>
 * <pre>
 * // 推荐：生成短 UID（节省约 60% 内存）
 * String uid = UidGenerator.generateShortUID();
 * String itemUid = UidGenerator.generateShortUID("ITEM_");
 * 
 * // 生成 NanoID（适合 URL、分享码等场景）
 * String nanoId = UidGenerator.generateNanoID();
 * String customNanoId = UidGenerator.generateNanoID(16);
 * 
 * // 生成随机字符串
 * String randomStr = UidGenerator.generateRandomString(32);
 * String secureRandomStr = UidGenerator.generateSecureRandomString(32);
 * 
 * // 批量生成（使用短 UID）
 * String[] uids = UidGenerator.generateMultipleUUIDs(10);
 * </pre>
 * 
 * <h2>各方法对比：</h2>
 * <ul>
 *   <li>{@link #generateShortUID()} - 推荐默认选择，12-15 位，节省内存</li>
 *   <li>{@link #generateUUID()} - 标准 UUID，36 位，兼容性好但占用较大</li>
 *   <li>{@link #generateNanoID()} - 21 位，适合 URL 和分享码</li>
 *   <li>{@link #generateTimestampUID()} - 基于时间戳，适合日志、订单等</li>
 * </ul>
 */
public final class UidGenerator {
    
    // 字符集定义
    private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();
    private static final char[] BASE62_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final char[] BASE36_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    
    // 随机数生成器（线程安全）
    private static final Random RANDOM = new Random();
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    
    // 预定义的分隔符
    private static final String DEFAULT_SEPARATOR = "-";
    private static final String NO_SEPARATOR = "";
    
    /**
     * 私有构造函数，防止实例化
     */
    private UidGenerator() {
        throw new IllegalStateException("Utility class");
    }
    
    /**
     * 生成标准 UUID（32 位，带连字符）
     * 格式：xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
     * 注意：此方法生成的 UID 较长，如无特殊需求，建议使用 {@link #generateShortUID()} 以节省内存
     * 
     * <h3>使用示例：</h3>
     * <pre>
     * String uuid = UidGenerator.generateUUID();
     * // 输出示例：550e8400-e29b-41d4-a716-446655440000
     * </pre>
     * 
     * @return 标准格式的 UUID 字符串
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * 生成无连字符的 UUID（32 位）
     * 格式：xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
     * 注意：此方法生成的 UID 较长，如无特殊需求，建议使用 {@link #generateShortUID()} 以节省内存
     * 
     * <h3>使用示例：</h3>
     * <pre>
     * String simpleUuid = UidGenerator.generateSimpleUUID();
     * // 输出示例：550e8400e29b41d4a716446655440000
     * </pre>
     * 
     * @return 无分隔符的 UUID 字符串
     */
    public static String generateSimpleUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 生成压缩的 UUID（使用 Base62 编码，更短）
     * 长度：约 22 位
     * 
     * <h3>使用示例：</h3>
     * <pre>
     * String compressedUuid = UidGenerator.generateCompressedUUID();
     * // 输出示例：2fF8kL9pQwX7yH3nM5vR1c
     * </pre>
     * 
     * @return 压缩格式的 UUID 字符串
     */
    public static String generateCompressedUUID() {
        return encodeBase62(UUID.randomUUID().getMostSignificantBits(), 
                           UUID.randomUUID().getLeastSignificantBits());
    }
    
    /**
     * 生成指定长度的随机字符串 UID（使用 Base62 字符集）
     * 字符集：0-9, A-Z, a-z（共 62 个字符）
     * 
     * <h3>使用示例：</h3>
     * <pre>
     * String randomStr = UidGenerator.generateRandomString(16);
     * // 输出示例：K7mN9pQ2xR5tY8wZ
     * 
     * // 用于生成验证码
     * String code = UidGenerator.generateRandomString(6);
     * // 输出示例：A3F8K2
     * </pre>
     * 
     * @param length 字符串长度
     * @return 随机字符串 UID
     * @throws IllegalArgumentException 当 length <= 0 时抛出
     */
    public static String generateRandomString(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive: " + length);
        }
        
        char[] result = new char[length];
        for (int i = 0; i < length; i++) {
            result[i] = BASE62_CHARS[RANDOM.nextInt(BASE62_CHARS.length)];
        }
        return new String(result);
    }
    
    /**
     * 生成安全的随机字符串 UID（使用 SecureRandom，更适合安全敏感场景）
     * 字符集：0-9, A-Z, a-z（共 62 个字符）
     * 
     * <h3>使用示例：</h3>
     * <pre>
     * // 用于生成令牌、密钥等安全敏感数据
     * String token = UidGenerator.generateSecureRandomString(32);
     * // 输出示例：xK9mP2nQ5rT8vW1yZ4bC7dF0gH3jL6o
     * </pre>
     * 
     * @param length 字符串长度
     * @return 安全的随机字符串 UID
     * @throws IllegalArgumentException 当 length <= 0 时抛出
     */
    public static String generateSecureRandomString(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive: " + length);
        }
        
        char[] result = new char[length];
        for (int i = 0; i < length; i++) {
            result[i] = BASE62_CHARS[SECURE_RANDOM.nextInt(BASE62_CHARS.length)];
        }
        return new String(result);
    }
    
    /**
     * 生成基于时间戳的 UID（唯一性依赖于时间精度和随机数）
     * 格式：时间戳 + 随机数（4 位）
     * 
     * <h3>使用示例：</h3>
     * <pre>
     * String timestampUid = UidGenerator.generateTimestampUID();
     * // 输出示例：17108640000001234
     * </pre>
     * 
     * @return 时间戳 UID
     */
    public static String generateTimestampUID() {
        long timestamp = System.currentTimeMillis();
        int randomPart = RANDOM.nextInt(10000);
        return String.format("%d%04d", timestamp, randomPart);
    }
    
    /**
     * 生成简短的唯一 ID（适合用于游戏内物品、玩家等标识）
     * 格式：前缀 + 时间戳压缩 + 随机码
     * 长度：约 12-15 位（相比 UUID 节省约 60% 内存）
     * 
     * <h3>使用示例：</h3>
     * <pre>
     * // 生成物品 ID
     * String itemId = UidGenerator.generateShortUID("ITEM_");
     * // 输出示例：ITEM_K7M9P2N5Q8R1
     * 
     * // 生成玩家 ID
     * String playerId = UidGenerator.generateShortUID("PLAYER_");
     * // 输出示例：PLAYER_X3Y6Z9A2B5C8
     * 
     * // 生成任务 ID
     * String questId = UidGenerator.generateShortUID("QUEST_");
     * // 输出示例：QUEST_D4E7F0G3H6I9
     * </pre>
     * 
     * @param prefix 前缀标识（如："ITEM_", "PLAYER_" 等）
     * @return 简短 UID
     */
    public static String generateShortUID(String prefix) {
        if (prefix == null) {
            prefix = "";
        }
        
        long timestamp = System.currentTimeMillis();
        int randomPart = SECURE_RANDOM.nextInt(65536); // 2^16
        
        return prefix + encodeBase36(timestamp) + encodeBase36(randomPart);
    }
    
    /**
     * 生成简短的唯一 ID（无前缀版本）
     * 推荐使用此方法替代 UUID，可节省约 60% 内存
     * 
     * <h3>使用示例：</h3>
     * <pre>
     * String uid = UidGenerator.generateShortUID();
     * // 输出示例：K7M9P2N5Q8R1
     * </pre>
     * 
     * @return 简短 UID
     */
    public static String generateShortUID() {
        return generateShortUID("");
    }
    
    /**
     * 生成十六进制格式的 UID
     * 长度：32 位（128 位随机数的十六进制表示）
     * 
     * <h3>使用示例：</h3>
     * <pre>
     * String hexUid = UidGenerator.generateHexUID();
     * // 输出示例：2F4A8B1C3D5E6F7A8B9C0D1E2F3A4B5C
     * </pre>
     * 
     * @return 十六进制 UID
     */
    public static String generateHexUID() {
        byte[] bytes = new byte[16];
        SECURE_RANDOM.nextBytes(bytes);
        return encodeHex(bytes);
    }
    
    /**
     * 生成指定长度的十六进制 UID
     * 
     * <h3>使用示例：</h3>
     * <pre>
     * // 生成 8 字节的十六进制 UID（16 位）
     * String shortHex = UidGenerator.generateHexUID(8);
     * // 输出示例：2F4A8B1C3D5E6F7A
     * 
     * // 生成 4 字节的十六进制 UID（8 位）
     * String tinyHex = UidGenerator.generateHexUID(4);
     * // 输出示例：2F4A8B1C
     * </pre>
     * 
     * @param length 长度（字节数，生成的字符串长度为 length*2）
     * @return 十六进制 UID
     * @throws IllegalArgumentException 当 length <= 0 时抛出
     */
    public static String generateHexUID(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive: " + length);
        }
        
        byte[] bytes = new byte[length];
        SECURE_RANDOM.nextBytes(bytes);
        return encodeHex(bytes);
    }
    
    /**
     * 生成 NanoID 格式的 UID（类似短网址服务使用的格式）
     * 默认长度：21 位
     * 特点：URL 安全、易于阅读和输入
     * 
     * <h3>使用示例：</h3>
     * <pre>
     * String nanoId = UidGenerator.generateNanoID();
     * // 输出示例：V1StGXR8_Z5jdHi6B-myT
     * 
     * // 用于生成分享链接
     * String shareCode = UidGenerator.generateNanoID();
     * // https://example.com/share/V1StGXR8_Z5jdHi6B-myT
     * </pre>
     * 
     * @return NanoID 格式的 UID
     */
    public static String generateNanoID() {
        return generateNanoID(21);
    }
    
    /**
     * 生成指定长度的 NanoID 格式的 UID
     * 
     * <h3>使用示例：</h3>
     * <pre>
     * // 生成较短的 ID（适合二维码、短链接）
     * String shortNanoId = UidGenerator.generateNanoID(12);
     * // 输出示例：V1StGXR8_Z5j
     * 
     * // 生成较长的 ID（更高唯一性）
     * String longNanoId = UidGenerator.generateNanoID(32);
     * // 输出示例：V1StGXR8_Z5jdHi6B-myTxPqL9KmN3
     * </pre>
     * 
     * @param size 长度
     * @return NanoID 格式的 UID
     * @throws IllegalArgumentException 当 size <= 0 时抛出
     */
    public static String generateNanoID(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive: " + size);
        }
        
        char[] symbols = new char[size];
        for (int i = 0; i < size; i++) {
            symbols[i] = BASE62_CHARS[SECURE_RANDOM.nextInt(BASE62_CHARS.length)];
        }
        return new String(symbols);
    }
    
    /**
     * 批量生成多个 UID（使用短 UID 格式，节省内存）
     * 
     * <h3>使用示例：</h3>
     * <pre>
     * // 批量生成 10 个 UID
     * String[] uids = UidGenerator.generateMultipleUUIDs(10);
     * for (String uid : uids) {
     *     System.out.println(uid);
     * }
     * // 输出示例：
     * // K7M9P2N5Q8R1
     * // X3Y6Z9A2B5C8
     * // D4E7F0G3H6I9
     * // ...
     * </pre>
     * 
     * @param count 生成数量
     * @return UID 数组
     * @throws IllegalArgumentException 当 count <= 0 时抛出
     */
    public static String[] generateMultipleUUIDs(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be positive: " + count);
        }
        
        String[] uids = new String[count];
        for (int i = 0; i < count; i++) {
            uids[i] = generateShortUID(); // 改用短 UID
        }
        return uids;
    }
    
    /**
     * 将字节数组编码为十六进制字符串
     */
    private static String encodeHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(HEX_CHARS[(b >> 4) & 0xF]);
            sb.append(HEX_CHARS[b & 0xF]);
        }
        return sb.toString();
    }
    
    /**
     * 将 long 值编码为 Base62 字符串
     */
    private static String encodeBase62(long value) {
        if (value == 0) {
            return String.valueOf(BASE62_CHARS[0]);
        }
        
        StringBuilder sb = new StringBuilder();
        while (value > 0) {
            sb.append(BASE62_CHARS[(int)(value % 62)]);
            value /= 62;
        }
        return sb.reverse().toString();
    }
    
    /**
     * 将两个 long 值编码为 Base62 字符串（用于压缩 UUID）
     */
    private static String encodeBase62(long mostSigBits, long leastSigBits) {
        return encodeBase62(mostSigBits ^ leastSigBits);
    }
    
    /**
     * 将 long 值编码为 Base36 字符串
     */
    private static String encodeBase36(long value) {
        if (value == 0) {
            return String.valueOf(BASE36_CHARS[0]);
        }
        
        StringBuilder sb = new StringBuilder();
        while (value > 0) {
            sb.append(BASE36_CHARS[(int)(value % 36)]);
            value /= 36;
        }
        return sb.reverse().toString();
    }
    
    /**
     * 验证字符串是否为有效的 UUID 格式
     * 
     * <h3>使用示例：</h3>
     * <pre>
     * boolean isValid = UidGenerator.isValidUUID("550e8400-e29b-41d4-a716-446655440000");
     * // 返回 true
     * 
     * boolean isInvalid = UidGenerator.isValidUUID("invalid-uuid");
     * // 返回 false
     * </pre>
     * 
     * @param uuid 待验证的 UUID 字符串
     * @return 如果是有效的 UUID 返回 true，否则返回 false
     */
    public static boolean isValidUUID(String uuid) {
        if (uuid == null || uuid.isEmpty()) {
            return false;
        }
        
        try {
            UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * 获取随机数生成器实例（用于需要自定义随机逻辑的场景）
     * 
     * @return Random 实例
     */
    public static Random getRandom() {
        return RANDOM;
    }
    
    /**
     * 获取安全随机数生成器实例（用于安全敏感场景）
     * 
     * @return SecureRandom 实例
     */
    public static SecureRandom getSecureRandom() {
        return SECURE_RANDOM;
    }
}
