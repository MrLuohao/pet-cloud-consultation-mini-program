package com.petcloud.shop.domain.service;

import com.petcloud.shop.domain.vo.CouponVO;
import com.petcloud.shop.domain.vo.UserCouponVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 优惠券服务接口
 *
 * @author luohao
 */
public interface CouponService {

    /**
     * 获取可领取优惠券列表
     *
     * @param userId 用户ID
     * @return 优惠券列表
     */
    List<CouponVO> getAvailableCoupons(Long userId);

    /**
     * 获取我的优惠券
     *
     * @param userId 用户ID
     * @param status 状态（可选）：0未使用 1已使用 2已过期
     * @return 用户优惠券列表
     */
    List<UserCouponVO> getUserCoupons(Long userId, Integer status);

    /**
     * 领取优惠券
     *
     * @param userId   用户ID
     * @param couponId 优惠券ID
     * @return 是否成功
     */
    Boolean receiveCoupon(Long userId, Long couponId);

    /**
     * 获取订单可用优惠券
     *
     * @param userId     用户ID
     * @param totalAmount 订单总金额
     * @return 可用优惠券列表
     */
    List<UserCouponVO> getAvailableCouponsForOrder(Long userId, BigDecimal totalAmount);

    /**
     * 计算优惠券优惠金额
     *
     * @param userCouponId 用户优惠券ID
     * @param orderAmount  订单金额
     * @return 优惠金额
     */
    BigDecimal calculateDiscount(Long userCouponId, BigDecimal orderAmount);
}
