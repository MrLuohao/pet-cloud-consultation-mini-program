package com.petcloud.shop.domain.service;

import com.petcloud.shop.domain.dto.SubscriptionConfigDTO;
import com.petcloud.shop.domain.dto.SubscriptionCreateDTO;
import com.petcloud.shop.domain.vo.ProductSubscriptionVO;

import java.util.List;

/**
 * 商品订阅服务接口
 *
 * @author luohao
 */
public interface ProductSubscriptionService {

    /**
     * 创建商品订阅
     */
    Long createSubscription(Long userId, SubscriptionCreateDTO dto);

    /**
     * 查询用户订阅列表
     */
    List<ProductSubscriptionVO> getSubscriptionList(Long userId);

    /**
     * 暂停订阅
     */
    void pauseSubscription(Long subscriptionId, Long userId);

    /**
     * 恢复订阅
     */
    void resumeSubscription(Long subscriptionId, Long userId);

    /**
     * 取消订阅
     */
    void cancelSubscription(Long subscriptionId, Long userId);

    /**
     * 修改订阅配置
     */
    void updateConfig(Long subscriptionId, Long userId, SubscriptionConfigDTO dto);
}
