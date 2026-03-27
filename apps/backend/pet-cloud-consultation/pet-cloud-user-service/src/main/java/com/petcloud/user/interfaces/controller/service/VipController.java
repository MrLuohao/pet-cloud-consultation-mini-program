package com.petcloud.user.interfaces.controller.service;

import com.petcloud.common.core.response.Response;
import com.petcloud.common.web.utils.UserContextHolderWeb;
import com.petcloud.user.application.WxPayService;
import com.petcloud.user.domain.dto.VipSubscribeDTO;
import com.petcloud.user.domain.enums.UserRespType;
import com.petcloud.user.domain.enums.VipPlanType;
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
        String planType = body.getOrDefault("planType", VipPlanType.DEFAULT_CODE);
        VipPlanType plan = VipPlanType.fromCode(planType);
        if (plan == null) {
            return Response.error(UserRespType.VIP_PLAN_UNSUPPORTED, planType);
        }

        String outTradeNo = "VIP-" + userId + "-" + planType + "-" + System.currentTimeMillis();
        log.info("发起VIP支付, userId: {}, planType: {}, fee: {}分", userId, planType, plan.getFeeInCent());

        WxPayParamsVO payParams = wxPayService.createOrder(outTradeNo, plan.getFeeInCent(), null);
        return Response.succeed(payParams);
    }
}
