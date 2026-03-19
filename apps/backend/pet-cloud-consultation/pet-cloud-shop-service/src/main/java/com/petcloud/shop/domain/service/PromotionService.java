package com.petcloud.shop.domain.service;

import com.petcloud.shop.domain.vo.PromotionVO;

import java.util.List;

/**
 * 满减活动服务接口
 *
 * @author luohao
 */
public interface PromotionService {

    /**
     * 获取当前有效的满减活动列表
     *
     * @return 满减活动列表
     */
    List<PromotionVO> getActivePromotions();
}
