package org.lingZero.modularization_defend.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * 数据压缩/解压工具类。
 * 提供 GZIP 和 Deflate 两种压缩方式，适用于 NBT 持久化、网络传输等场景。
 *
 * <h3>压缩方式对比</h3>
 * <ul>
 *   <li><b>GZIP</b> — 含校验头，压缩率更高，适合持久化存储（NBT、文件）</li>
 *   <li><b>Deflate</b> — 纯数据流，体积更小，适合网络传输</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * byte[] compressed = DataCompressUtil.compressGzip(originalData);
 * byte[] restored = DataCompressUtil.decompressGzip(compressed);
 * }</pre>
 */
public class DataCompressUtil {

    private DataCompressUtil() {
    }

    // ==================== 公共常量 ====================

    /** 默认缓冲区大小：8KB */
    public static final int DEFAULT_BUFFER_SIZE = 8192;

    /** 不压缩时返回的最小数据大小（字节），低于此阈值的数据压缩不划算 */
    public static final int MIN_COMPRESS_SIZE = 64;

    // ==================== GZIP 压缩 ====================

    /**
     * 使用 GZIP 压缩数据（默认压缩级别）。
     *
     * @param data 原始数据，不能为 null
     * @return 压缩后的数据。若数据小于 {@link #MIN_COMPRESS_SIZE} 则直接返回原数据副本
     * @throws IllegalArgumentException 若 data 为 null
     */
    public static byte[] compressGzip(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("压缩数据不能为 null");
        }
        if (data.length < MIN_COMPRESS_SIZE) {
            return data.clone();
        }
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(Math.max(data.length / 4, 256));
            try (GZIPOutputStream gzos = new GZIPOutputStream(bos) {{
                def.setLevel(Deflater.DEFAULT_COMPRESSION);
            }}) {
                gzos.write(data);
                gzos.finish();
            }
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("GZIP 压缩失败", e);
        }
    }

    /**
     * 解压 GZIP 数据。
     *
     * @param data GZIP 压缩数据，不能为 null
     * @return 解压后的原始数据
     * @throws IllegalArgumentException 若 data 为 null
     */
    public static byte[] decompressGzip(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("解压数据不能为 null");
        }
        if (data.length == 0) {
            return data;
        }
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length * 2);
            try (GZIPInputStream gzis = new GZIPInputStream(new ByteArrayInputStream(data))) {
                byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                int len;
                while ((len = gzis.read(buffer)) > 0) {
                    bos.write(buffer, 0, len);
                }
            }
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("GZIP 解压失败", e);
        }
    }

    // ==================== Deflate 压缩 ====================

    /**
     * 使用 Deflate 压缩数据（默认压缩级别）。
     * 比 GZIP 体积稍小，适合网络传输。
     *
     * @param data 原始数据
     * @return 压缩后的数据
     */
    public static byte[] compressDeflate(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("压缩数据不能为 null");
        }
        if (data.length < MIN_COMPRESS_SIZE) {
            return data.clone();
        }
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(Math.max(data.length / 4, 256));
            try (DeflaterOutputStream dos = new DeflaterOutputStream(bos,
                    new Deflater(Deflater.DEFAULT_COMPRESSION))) {
                dos.write(data);
                dos.finish();
            }
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Deflate 压缩失败", e);
        }
    }

    /**
     * 解压 Deflate 数据。
     *
     * @param data Deflate 压缩数据
     * @return 解压后的原始数据
     */
    public static byte[] decompressDeflate(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("解压数据不能为 null");
        }
        if (data.length == 0) {
            return data;
        }
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length * 2);
            try (InflaterInputStream iis = new InflaterInputStream(
                    new ByteArrayInputStream(data), new Inflater())) {
                byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                int len;
                while ((len = iis.read(buffer)) > 0) {
                    bos.write(buffer, 0, len);
                }
            }
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Deflate 解压失败", e);
        }
    }

    // ==================== 便捷方法 ====================

    /**
     * 自动选择最优方式压缩（当前默认使用 GZIP）。
     *
     * @param data 原始数据
     * @return 压缩后的数据
     */
    public static byte[] compress(byte[] data) {
        return compressGzip(data);
    }

    /**
     * 自动检测并解压（先尝试 GZIP，失败则尝试 Deflate）。
     *
     * @param data 压缩数据
     * @return 解压后的原始数据
     */
    public static byte[] decompress(byte[] data) {
        try {
            return decompressGzip(data);
        } catch (RuntimeException e) {
            return decompressDeflate(data);
        }
    }

    /**
     * 判断数据是否为 GZIP 格式（通过魔数 0x1F8B 判断）。
     */
    public static boolean isGzipFormat(byte[] data) {
        return data != null && data.length >= 2
                && data[0] == (byte) 0x1F && data[1] == (byte) 0x8B;
    }
}
