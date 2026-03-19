package com.petcloud.shop.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 微信支付所需参数 VO
 * 供前端调用 wx.requestPayment() 使用
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WxPayParamsVO {

    /** 时间戳（Unix秒级，字符串形式） */
    private String timeStamp;

    /** 随机字符串（32位以内） */
    private String nonceStr;

    /** 统一下单返回的 prepay_id，格式：prepay_id=xxx */
    private String packageStr;

    /** 签名方式，固定 RSA */
    private String signType;

    /** 使用微信支付私钥对签名串的 Base64 编码签名 */
    private String paySign;

    /** 订单ID（方便前端跳转） */
    private Long orderId;
}
