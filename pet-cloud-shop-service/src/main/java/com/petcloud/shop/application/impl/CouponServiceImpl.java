package com.petcloud.shop.application.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcloud.common.core.exception.BusinessException;
import com.petcloud.common.core.exception.RespType;
import com.petcloud.shop.domain.entity.*;
import com.petcloud.shop.domain.service.CouponService;
import com.petcloud.shop.domain.vo.CouponVO;
import com.petcloud.shop.domain.vo.UserCouponVO;
import com.petcloud.shop.infrastructure.persistence.mapper.CouponMapper;
import com.petcloud.shop.infrastructure.persistence.mapper.UserCouponMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 优惠券服务实现类
 *
 * @author luohao
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;

    @Override
    public List<CouponVO> getAvailableCoupons(Long userId) {
        LambdaQueryWrapper<Coupon> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Coupon::getStatus, 1)
                .gt(Coupon::getEndTime, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .lt(Coupon::getStartTime, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        List<Coupon> coupons = couponMapper.selectList(queryWrapper);

        return coupons.stream()
                .map(coupon -> {
                    // 检查用户是否已领取
                    LambdaQueryWrapper<UserCoupon> userCouponWrapper = new LambdaQueryWrapper<>();
                    userCouponWrapper.eq(UserCoupon::getUserId, userId)
                            .eq(UserCoupon::getCouponId, coupon.getId());
                    Long count = userCouponMapper.selectCount(userCouponWrapper);
                    // 防止 NPE：如果 selectCount 返回 null，视为 0（未领取）
                    long countValue = count != null ? count : 0L;

                    return CouponVO.builder()
                            .id(coupon.getId())
                            .name(coupon.getName())
                            .type(coupon.getType())
                            .typeDesc(Integer.valueOf(1).equals(coupon.getType()) ? "满减券" : "折扣券")
                            .discountAmount(coupon.getDiscountAmount())
                            .discountRate(coupon.getDiscountRate())
                            .minAmount(coupon.getMinAmount())
                            .maxDiscount(coupon.getMaxDiscount())
                            .validDays(coupon.getValidDays())
                            .startTime(coupon.getStartTime())
                            .endTime(coupon.getEndTime())
                            .description(coupon.getDescription())
                            .totalCount(coupon.getTotalCount())
                            .receivedCount(coupon.getReceivedCount() != null ? coupon.getReceivedCount() : 0)
                            .canReceive(countValue == 0 && coupon.getReceivedCount() < coupon.getTotalCount())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<UserCouponVO> getUserCoupons(Long userId, Integer status) {
        LambdaQueryWrapper<UserCoupon> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserCoupon::getUserId, userId);
        if (status != null) {
            queryWrapper.eq(UserCoupon::getStatus, status);
        }
        queryWrapper.orderByDesc(UserCoupon::getCreateTime);
        List<UserCoupon> userCoupons = userCouponMapper.selectList(queryWrapper);

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return userCoupons.stream()
                .map(uc -> {
                    // 检查是否过期
                    boolean expired = uc.getExpireTime() != null && uc.getExpireTime().compareTo(now) < 0 && Integer.valueOf(0).equals(uc.getStatus());
                    return UserCouponVO.builder()
                            .id(uc.getId())
                            .couponId(uc.getCouponId())
                            .couponName(uc.getCouponName())
                            .couponType(uc.getCouponType())
                            .typeDesc(Integer.valueOf(1).equals(uc.getCouponType()) ? "满减券" : "折扣券")
                            .discountAmount(uc.getDiscountAmount())
                            .discountRate(uc.getDiscountRate())
                            .minAmount(uc.getMinAmount())
                            .maxDiscount(uc.getMaxDiscount())
                            .status(expired ? 2 : uc.getStatus())
                            .statusDesc(getStatusDesc(expired ? 2 : uc.getStatus()))
                            .expireTime(uc.getExpireTime())
                            .available(Integer.valueOf(0).equals(uc.getStatus()) && !expired)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public Boolean receiveCoupon(Long userId, Long couponId) {
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null || !Integer.valueOf(1).equals(coupon.getStatus())) {
            throw new BusinessException(RespType.COUPON_NOT_FOUND);
        }

        // 检查是否已领取
        LambdaQueryWrapper<UserCoupon> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getCouponId, couponId);
        UserCoupon existing = userCouponMapper.selectOne(queryWrapper);
        if (existing != null) {
            throw new BusinessException(RespType.COUPON_ALREADY_RECEIVED);
        }

        // 检查是否领完
        if (coupon.getReceivedCount() >= coupon.getTotalCount()) {
            throw new BusinessException(RespType.COUPON_EXHAUSTED);
        }

        // 创建用户优惠券
        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUserId(userId);
        userCoupon.setCouponId(couponId);
        userCoupon.setCouponName(coupon.getName());
        userCoupon.setCouponType(coupon.getType());
        userCoupon.setDiscountAmount(coupon.getDiscountAmount());
        userCoupon.setDiscountRate(coupon.getDiscountRate());
        userCoupon.setMinAmount(coupon.getMinAmount());
        userCoupon.setMaxDiscount(coupon.getMaxDiscount());
        userCoupon.setStatus(0);

        // 计算过期时间
        LocalDateTime expireTime = LocalDateTime.now().plusDays(coupon.getValidDays());
        userCoupon.setExpireTime(expireTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        userCouponMapper.insert(userCoupon);

        // 更新已领取数量
        coupon.setReceivedCount(coupon.getReceivedCount() + 1);
        couponMapper.updateById(coupon);

        return true;
    }

    @Override
    public List<UserCouponVO> getAvailableCouponsForOrder(Long userId, BigDecimal totalAmount) {
        LambdaQueryWrapper<UserCoupon> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getStatus, 0);
        List<UserCoupon> userCoupons = userCouponMapper.selectList(queryWrapper);

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return userCoupons.stream()
                .filter(uc -> {
                    // 检查是否过期
                    if (uc.getExpireTime().compareTo(now) < 0) {
                        return false;
                    }
                    // 检查是否满足最低金额
                    if (uc.getMinAmount() != null && totalAmount.compareTo(uc.getMinAmount()) < 0) {
                        return false;
                    }
                    return true;
                })
                .map(uc -> UserCouponVO.builder()
                        .id(uc.getId())
                        .couponId(uc.getCouponId())
                        .couponName(uc.getCouponName())
                        .couponType(uc.getCouponType())
                        .typeDesc(uc.getCouponType() == 1 ? "满减券" : "折扣券")
                        .discountAmount(uc.getDiscountAmount())
                        .discountRate(uc.getDiscountRate())
                        .minAmount(uc.getMinAmount())
                        .maxDiscount(uc.getMaxDiscount())
                        .status(uc.getStatus())
                        .statusDesc(getStatusDesc(uc.getStatus()))
                        .expireTime(uc.getExpireTime())
                        .available(true)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal calculateDiscount(Long userCouponId, BigDecimal orderAmount) {
        UserCoupon userCoupon = userCouponMapper.selectById(userCouponId);
        if (userCoupon == null || !Integer.valueOf(0).equals(userCoupon.getStatus())) {
            return BigDecimal.ZERO;
        }

        // 检查是否过期
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        if (userCoupon.getExpireTime().compareTo(now) < 0) {
            return BigDecimal.ZERO;
        }

        // 检查是否满足最低金额
        if (userCoupon.getMinAmount() != null && orderAmount.compareTo(userCoupon.getMinAmount()) < 0) {
            return BigDecimal.ZERO;
        }

        // 计算优惠金额
        if (Integer.valueOf(1).equals(userCoupon.getCouponType())) {
            // 满减券
            return userCoupon.getDiscountAmount() != null ? userCoupon.getDiscountAmount() : BigDecimal.ZERO;
        } else {
            // 折扣券
            if (userCoupon.getDiscountRate() != null) {
                BigDecimal discount = orderAmount.multiply(BigDecimal.ONE.subtract(userCoupon.getDiscountRate().divide(BigDecimal.valueOf(10), 2, RoundingMode.HALF_UP)));
                if (userCoupon.getMaxDiscount() != null && discount.compareTo(userCoupon.getMaxDiscount()) > 0) {
                    return userCoupon.getMaxDiscount();
                }
                return discount;
            }
        }

        return BigDecimal.ZERO;
    }

    private String getStatusDesc(Integer status) {
        if (status == null) {
            return "";
        }
        return Arrays.stream(com.petcloud.shop.domain.entity.UserCoupon.Status.values())
                .filter(s -> s.getCode().equals(status))
                .findFirst()
                .map(com.petcloud.shop.domain.entity.UserCoupon.Status::getDesc)
                .orElse("");
    }
}
