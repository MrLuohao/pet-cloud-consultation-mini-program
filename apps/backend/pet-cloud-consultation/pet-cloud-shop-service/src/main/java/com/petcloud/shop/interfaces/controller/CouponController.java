package com.petcloud.shop.interfaces.controller;

import com.petcloud.common.core.response.Response;
import com.petcloud.common.web.utils.UserContextHolderWeb;
import com.petcloud.shop.domain.dto.CouponReceiveDTO;
import com.petcloud.shop.domain.service.CouponService;
import com.petcloud.shop.domain.vo.CouponVO;
import com.petcloud.shop.domain.vo.UserCouponVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 优惠券控制器
 *
 * @author luohao
 */
@RestController
@RequestMapping("/api/coupon")
@RequiredArgsConstructor
public class CouponController {

    private static final Logger log = LoggerFactory.getLogger(CouponController.class);

    private final CouponService couponService;
    private final UserContextHolderWeb userContextHolder;

    /**
     * 获取可领取优惠券列表
     */
    @GetMapping("/list")
    public Response<List<CouponVO>> getAvailableCoupons(HttpServletRequest request) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("获取可领取优惠券列表，userId: {}", userId);
        List<CouponVO> coupons = couponService.getAvailableCoupons(userId);
        return Response.succeed(coupons);
    }

    /**
     * 获取我的优惠券
     */
    @GetMapping("/my")
    public Response<List<UserCouponVO>> getUserCoupons(HttpServletRequest request,
                                                        @RequestParam(required = false) Integer status) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("获取我的优惠券，userId: {}, status: {}", userId, status);
        List<UserCouponVO> userCoupons = couponService.getUserCoupons(userId, status);
        return Response.succeed(userCoupons);
    }

    /**
     * 领取优惠券
     */
    @PostMapping("/receive")
    public Response<Boolean> receiveCoupon(HttpServletRequest request,
                                            @RequestBody CouponReceiveDTO req) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("领取优惠券，userId: {}, couponId: {}", userId, req.getCouponId());
        Boolean result = couponService.receiveCoupon(userId, req.getCouponId());
        return Response.succeed(result);
    }

    /**
     * 获取订单可用优惠券
     */
    @GetMapping("/available")
    public Response<List<UserCouponVO>> getAvailableCouponsForOrder(HttpServletRequest request,
                                                                       @RequestParam BigDecimal totalAmount) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("获取订单可用优惠券，userId: {}, totalAmount: {}", userId, totalAmount);
        List<UserCouponVO> coupons = couponService.getAvailableCouponsForOrder(userId, totalAmount);
        return Response.succeed(coupons);
    }
}
