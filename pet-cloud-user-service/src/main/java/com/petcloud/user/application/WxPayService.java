package com.petcloud.user.application;

import com.petcloud.user.domain.vo.WxPayParamsVO;

/**
 * 微信支付服务接口（user-service）
 */
public interface WxPayService {

    /**
     * 生成前端调起支付所需参数
     *
     * @param outTradeNo 自定义交易号（如 VIP-userId-时间戳）
     * @param totalFee   支付金额（分）
     * @param openid     用户 openid
     * @return wx.requestPayment 所需参数
     */
    WxPayParamsVO createOrder(String outTradeNo, int totalFee, String openid);
}
