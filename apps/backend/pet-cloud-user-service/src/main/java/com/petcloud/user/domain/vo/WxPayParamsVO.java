package com.petcloud.user.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 微信支付参数 VO（user-service）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WxPayParamsVO {
    private String timeStamp;
    private String nonceStr;
    private String packageStr;
    private String signType;
    private String paySign;
    /** 业务单号（VIP 场景下为 planType）*/
    private String outTradeNo;
}
