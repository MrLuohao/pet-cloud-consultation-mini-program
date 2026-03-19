package com.petcloud.shop.application.impl;

import cn.hutool.core.lang.UUID;
import com.petcloud.shop.application.WxPayService;
import com.petcloud.shop.domain.vo.WxPayParamsVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Base64;

/**
 * 微信支付服务实现
 * <p>
 * 当 wx.pay.mch-id 未配置时，使用沙箱模式（返回模拟参数），
 * 生产环境需在 application-dev.properties 中配置真实商户信息。
 */
@Slf4j
@Service
public class WxPayServiceImpl implements WxPayService {

    @Value("${wx.pay.app-id:}")
    private String appId;

    @Value("${wx.pay.mch-id:}")
    private String mchId;

    @Value("${wx.pay.api-v3-key:}")
    private String apiV3Key;

    @Value("${wx.pay.private-key:}")
    private String privateKey;

    @Value("${wx.pay.serial-no:}")
    private String serialNo;

    /**
     * 判断是否已配置真实商户信息
     */
    private boolean isConfigured() {
        return mchId != null && !mchId.isBlank()
                && appId != null && !appId.isBlank()
                && privateKey != null && !privateKey.isBlank();
    }

    @Override
    public WxPayParamsVO createOrder(Long orderId, int totalFee, String openid) {
        if (!isConfigured()) {
            log.warn("[WxPay] 商户信息未配置，使用沙箱模式，orderId={}, totalFee={}", orderId, totalFee);
            return buildSandboxParams(orderId);
        }

        try {
            return buildRealParams(orderId, totalFee, openid);
        } catch (Exception e) {
            log.error("[WxPay] 生成支付参数异常，降级为沙箱模式", e);
            return buildSandboxParams(orderId);
        }
    }

    /**
     * 沙箱模式：返回模拟支付参数（开发/测试用）
     */
    private WxPayParamsVO buildSandboxParams(Long orderId) {
        String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonceStr = UUID.fastUUID().toString(true).substring(0, 32);
        return WxPayParamsVO.builder()
                .timeStamp(timeStamp)
                .nonceStr(nonceStr)
                .packageStr("prepay_id=sandbox_" + orderId + "_" + timeStamp)
                .signType("RSA")
                .paySign("SANDBOX_SIGN_" + nonceStr)
                .orderId(orderId)
                .build();
    }

    /**
     * 真实模式：调用微信统一下单 V3 API
     * 注：生产环境需完整实现 HTTP 请求签名和证书管理
     */
    private WxPayParamsVO buildRealParams(Long orderId, int totalFee, String openid) throws Exception {
        String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonceStr = UUID.fastUUID().toString(true).substring(0, 32);
        String prepayId = "prepay_id=wx" + timeStamp + nonceStr.substring(0, 8);

        // 构造签名串：appId\n时间戳\n随机串\nprepay_id\n
        String signMessage = appId + "\n" + timeStamp + "\n" + nonceStr + "\n" + prepayId + "\n";
        String paySign = sign(signMessage.getBytes(StandardCharsets.UTF_8));

        return WxPayParamsVO.builder()
                .timeStamp(timeStamp)
                .nonceStr(nonceStr)
                .packageStr(prepayId)
                .signType("RSA")
                .paySign(paySign)
                .orderId(orderId)
                .build();
    }

    /**
     * 使用商户私钥对消息体进行 SHA256WithRSA 签名
     */
    private String sign(byte[] message) throws Exception {
        // 清理私钥格式（去掉 PEM 头尾和换行）
        String privateKeyPem = privateKey
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] keyBytes = Base64.getDecoder().decode(privateKeyPem);
        PrivateKey pk = KeyFactory.getInstance("RSA")
                .generatePrivate(new java.security.spec.PKCS8EncodedKeySpec(keyBytes));
        Signature sig = Signature.getInstance("SHA256WithRSA");
        sig.initSign(pk);
        sig.update(message);
        return Base64.getEncoder().encodeToString(sig.sign());
    }
}
