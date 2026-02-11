package com.petcloud.user.interfaces.controller;

import com.petcloud.common.core.response.Response;
import com.petcloud.common.web.utils.UserContextHolderWeb;
import com.petcloud.user.application.WxPayService;
import com.petcloud.user.domain.dto.VipSubscribeDTO;
import com.petcloud.user.domain.service.VipService;
import com.petcloud.user.domain.vo.UserInfoVO;
import com.petcloud.user.domain.vo.WxPayParamsVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 会员相关接口
 */
@Slf4j
@RestController
@RequestMapping("/api/vip")
@RequiredArgsConstructor
public class VipController {

    private final VipService vipService;
    private final UserContextHolderWeb userContextHolderWeb;
    private final WxPayService wxPayService;

    // VIP 套餐金额（分）
    private static final Map<String, Integer> PLAN_FEE = Map.of(
            "monthly",   2990,
            "quarterly", 7990,
            "yearly",    29900
    );

    /**
     * 开通/续费会员
     */
    @PostMapping("/subscribe")
    public Response<UserInfoVO> subscribe(HttpServletRequest request,
                                          @Valid @RequestBody VipSubscribeDTO dto) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        log.info("会员开通/续费, userId: {}, planId: {}", userId, dto.getPlanId());
        UserInfoVO userInfoVO = vipService.subscribe(userId, dto);
        return Response.succeed(userInfoVO);
    }

    /**
     * 发起 VIP 支付 - 返回 wx.requestPayment() 所需参数（BE-4.2）
     */
    @PostMapping("/pay")
    public Response<WxPayParamsVO> payVip(HttpServletRequest request,
                                          @RequestBody Map<String, String> body) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        String planType = body.getOrDefault("planType", "monthly");

        Integer fee = PLAN_FEE.get(planType);
        if (fee == null) {
            return Response.error(com.petcloud.common.core.exception.RespType.PARAMETER_ERROR, "不支持的会员套餐类型: " + planType);
        }

        String outTradeNo = "VIP-" + userId + "-" + planType + "-" + System.currentTimeMillis();
        log.info("发起VIP支付, userId: {}, planType: {}, fee: {}分", userId, planType, fee);

        WxPayParamsVO payParams = wxPayService.createOrder(outTradeNo, fee, null);
        return Response.succeed(payParams);
    }
}
