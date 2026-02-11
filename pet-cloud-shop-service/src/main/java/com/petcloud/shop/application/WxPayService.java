package com.petcloud.shop.application;

import com.petcloud.shop.domain.vo.WxPayParamsVO;

/**
 * 微信支付服务接口
 */
public interface WxPayService {

    /**
     * 发起微信统一下单，返回前端调起支付所需参数
     *
     * @param orderId  订单ID
     * @param totalFee 支付金额（分）
     * @param openid   用户 openid
     * @return wx.requestPayment 所需参数
     */
    WxPayParamsVO createOrder(Long orderId, int totalFee, String openid);
}
