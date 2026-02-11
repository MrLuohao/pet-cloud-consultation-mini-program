package com.petcloud.user.domain.service;

/**
 * 访客限制服务接口
 * 用于限制未登录用户对某些功能的使用次数
 *
 * @author luohao
 */
public interface GuestLimitService {

    /**
     * 获取访客剩余使用次数
     *
     * @param deviceId 设备唯一标识
     * @param limitType 限制类型（如 "ai_diagnosis"）
     * @return 剩余次数
     */
    int getRemainingCount(String deviceId, String limitType);

    /**
     * 检查是否可以使用
     *
     * @param deviceId 设备唯一标识
     * @param limitType 限制类型
     * @return true-可以使用, false-已达上限
     */
    boolean canUse(String deviceId, String limitType);

    /**
     * 记录一次使用
     *
     * @param deviceId 设备唯一标识
     * @param limitType 限制类型
     * @return 使用后的剩余次数
     */
    int recordUsage(String deviceId, String limitType);

    /**
     * 重置访客使用次数（测试用）
     *
     * @param deviceId 设备唯一标识
     * @param limitType 限制类型
     */
    void resetUsage(String deviceId, String limitType);
}
