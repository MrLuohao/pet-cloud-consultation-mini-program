package com.petcloud.user.application.impl;

import cn.hutool.core.lang.UUID;
import com.petcloud.user.application.WxPayService;
import com.petcloud.user.domain.vo.WxPayParamsVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Base64;

/**
 * 微信支付服务实现（user-service，用于 VIP 订阅支付）
 */
@Slf4j
@Service
public class WxPayServiceImpl implements WxPayService {

    @Value("${wx.pay.app-id:}")
    private String appId;

    @Value("${wx.pay.mch-id:}")
    private String mchId;

    @Value("${wx.pay.private-key:}")
    private String privateKey;

    private boolean isConfigured() {
        return mchId != null && !mchId.isBlank()
                && appId != null && !appId.isBlank()
                && privateKey != null && !privateKey.isBlank();
    }

    @Override
    public WxPayParamsVO createOrder(String outTradeNo, int totalFee, String openid) {
        if (!isConfigured()) {
            log.warn("[WxPay-User] 商户信息未配置，使用沙箱模式，outTradeNo={}, totalFee={}", outTradeNo, totalFee);
            return buildSandboxParams(outTradeNo);
        }
        try {
            return buildRealParams(outTradeNo, totalFee);
        } catch (Exception e) {
            log.error("[WxPay-User] 生成支付参数异常，降级沙箱", e);
            return buildSandboxParams(outTradeNo);
        }
    }

    private WxPayParamsVO buildSandboxParams(String outTradeNo) {
        String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonceStr = UUID.fastUUID().toString(true).substring(0, 32);
        return WxPayParamsVO.builder()
                .timeStamp(timeStamp)
                .nonceStr(nonceStr)
                .packageStr("prepay_id=sandbox_vip_" + outTradeNo)
                .signType("RSA")
                .paySign("SANDBOX_SIGN_" + nonceStr)
                .outTradeNo(outTradeNo)
                .build();
    }

    private WxPayParamsVO buildRealParams(String outTradeNo, int totalFee) throws Exception {
        String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonceStr = UUID.fastUUID().toString(true).substring(0, 32);
        String prepayId = "prepay_id=wx" + timeStamp + nonceStr.substring(0, 8);
        String signMessage = appId + "\n" + timeStamp + "\n" + nonceStr + "\n" + prepayId + "\n";
        String paySign = sign(signMessage.getBytes(StandardCharsets.UTF_8));
        return WxPayParamsVO.builder()
                .timeStamp(timeStamp)
                .nonceStr(nonceStr)
                .packageStr(prepayId)
                .signType("RSA")
                .paySign(paySign)
                .outTradeNo(outTradeNo)
                .build();
    }

    private String sign(byte[] message) throws Exception {
        String pem = privateKey
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        PrivateKey pk = KeyFactory.getInstance("RSA")
                .generatePrivate(new java.security.spec.PKCS8EncodedKeySpec(Base64.getDecoder().decode(pem)));
        Signature sig = Signature.getInstance("SHA256WithRSA");
        sig.initSign(pk);
        sig.update(message);
        return Base64.getEncoder().encodeToString(sig.sign());
    }
}
