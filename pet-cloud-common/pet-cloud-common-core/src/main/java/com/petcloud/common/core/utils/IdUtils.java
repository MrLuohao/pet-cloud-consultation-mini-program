package com.petcloud.common.core.utils;

import java.lang.management.ManagementFactory;
import java.net.NetworkInterface;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Enumeration;

/**
 * 分布式唯一ID生成器（雪花算法变种）
 *
 * @author luohao
 */
public class IdUtils {

    // 起始时间戳（2023-01-01）
    private static final long EPOCH = 1672531200000L;

    // 各部分的位移量
    private static final int TIMESTAMP_SHIFT = 22;
    private static final int WORKER_ID_SHIFT = 10;

    // 各部分的掩码
    private static final int SEQUENCE_MASK = (1 << 10) - 1;

    // 上一次生成ID的时间戳
    private long lastTimestamp = -1L;

    // 序列号计数器
    private int sequence = 0;

    // 工作机器ID（分布式环境中需唯一）
    private final long workerId;

    // 单例实例
    private static final IdUtils INSTANCE = new IdUtils();

    private IdUtils() {
        this.workerId = generateWorkerId();
    }

    public static IdUtils getInstance() {
        return INSTANCE;
    }

    /**
     * 生成下一个唯一ID
     */
    public synchronized long nextId() {
        long currentTimestamp = timeGen();

        // 时钟回拨检测
        if (currentTimestamp < lastTimestamp) {
            throw new IllegalStateException("系统时钟回退，拒绝生成ID");
        }

        // 同一毫秒内处理序列号
        if (lastTimestamp == currentTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;

            // 当前毫秒内序列号已用完，等待下一毫秒
            if (sequence == 0) {
                currentTimestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // 不同毫秒则序列号重置
            sequence = 0;
        }

        lastTimestamp = currentTimestamp;

        // 构造ID：时间戳(41位) + 机器ID(12位) + 序列号(10位)
        return ((currentTimestamp - EPOCH) << TIMESTAMP_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    private long generateWorkerId() {
        try {
            // 1. 尝试从系统属性获取配置
            String workerIdConfig = System.getProperty("distributed.id.worker");
            if (workerIdConfig != null) {
                return Long.parseLong(workerIdConfig) & 0xFFF;
            }

            // 2. 尝试从环境变量获取配置
            workerIdConfig = System.getenv("DISTRIBUTED_ID_WORKER");
            if (workerIdConfig != null) {
                return Long.parseLong(workerIdConfig) & 0xFFF;
            }

            // 3. 生成基于机器特征的workerId（默认策略）
            StringBuilder sb = new StringBuilder();

            // 使用网络接口MAC地址
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                byte[] mac = networkInterface.getHardwareAddress();
                if (mac != null) {
                    for (byte b : mac) {
                        sb.append(String.format("%02X", b));
                    }
                }
            }

            // 使用进程ID作为后备
            String pid = ManagementFactory.getRuntimeMXBean().getName();
            sb.append(pid.split("@")[0]);

            // 使用SecureRandom生成随机数
            return (sb.toString().hashCode() & 0x7FFFF) & 0xFFF;
        } catch (Exception ex) {
            // 生成完全随机的workerId
            return new SecureRandom().nextInt(0x1000);
        }
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    private long timeGen() {
        return Instant.now().toEpochMilli();
    }

    // ================= 静态工具方法 =================

    /**
     * 直接生成下一个ID（便捷方法）
     */
    public static long getId() {
        return getInstance().nextId();
    }

    /**
     * 获取ID生成时间（毫秒）
     */
    public static long getIdTimeMillis(long id) {
        return EPOCH + (id >> TIMESTAMP_SHIFT);
    }

    /**
     * 获取ID中的机器ID
     */
    public static long getWorkerId(long id) {
        return (id >>> WORKER_ID_SHIFT) & 0xFFF;
    }

    /**
     * 获取ID中的序列号
     */
    public static int getSequence(long id) {
        return (int) (id & SEQUENCE_MASK);
    }

    /**
     * 获取ID的数字长度
     */
    public static int getIdLength(long id) {
        return String.valueOf(id).length();
    }
}
