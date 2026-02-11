package com.petcloud.user.application.impl;

import com.petcloud.user.domain.service.GuestLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 访客限制服务实现类
 * 使用Redis存储访客使用次数
 *
 * 设计思路：
 * - 未登录用户通过设备ID标识
 * - 每个设备对每种功能有固定的终身免费使用次数（不会重置）
 * - 使用Redis存储，支持分布式部署
 * - 数据永不过期，除非手动重置
 *
 * @author luohao
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GuestLimitServiceImpl implements GuestLimitService {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * AI诊断免费次数（终身限制）
     */
    private static final int AI_DIAGNOSIS_FREE_COUNT = 3;

    /**
     * Redis key前缀
     */
    private static final String KEY_PREFIX = "guest:limit:";

    @Override
    public int getRemainingCount(String deviceId, String limitType) {
        String key = buildKey(deviceId, limitType);
        String countStr = stringRedisTemplate.opsForValue().get(key);

        if (countStr == null) {
            // 未使用过，返回最大次数
            return getMaxCount(limitType);
        }

        int usedCount = Integer.parseInt(countStr);
        int maxCount = getMaxCount(limitType);

        return Math.max(0, maxCount - usedCount);
    }

    @Override
    public boolean canUse(String deviceId, String limitType) {
        return getRemainingCount(deviceId, limitType) > 0;
    }

    @Override
    public int recordUsage(String deviceId, String limitType) {
        String key = buildKey(deviceId, limitType);

        // 增加使用次数（永不过期）
        Long newCount = stringRedisTemplate.opsForValue().increment(key);

        int maxCount = getMaxCount(limitType);
        int remaining = Math.max(0, maxCount - newCount.intValue());

        log.info("访客使用记录，设备: {}, 类型: {}, 已用: {}, 剩余: {}",
                deviceId, limitType, newCount, remaining);

        return remaining;
    }

    @Override
    public void resetUsage(String deviceId, String limitType) {
        String key = buildKey(deviceId, limitType);
        stringRedisTemplate.delete(key);
        log.info("重置访客使用次数，设备: {}, 类型: {}", deviceId, limitType);
    }

    /**
     * 构建Redis key
     */
    private String buildKey(String deviceId, String limitType) {
        return KEY_PREFIX + limitType + ":" + deviceId;
    }

    /**
     * 获取最大免费次数
     */
    private int getMaxCount(String limitType) {
        // 可根据不同类型配置不同次数
        return switch (limitType) {
            case "ai_diagnosis" -> AI_DIAGNOSIS_FREE_COUNT;
            default -> 0;
        };
    }
}
