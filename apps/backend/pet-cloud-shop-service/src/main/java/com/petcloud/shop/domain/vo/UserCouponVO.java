package com.petcloud.shop.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 用户优惠券VO
 *
 * @author luohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCouponVO {

    /**
     * 用户优惠券ID
     */
    private Long id;

    /**
     * 优惠券ID
     */
    private Long couponId;

    /**
     * 优惠券名称
     */
    private String couponName;

    /**
     * 类型：1满减券 2折扣券
     */
    private Integer couponType;

    /**
     * 类型描述
     */
    private String typeDesc;

    /**
     * 减免金额
     */
    private BigDecimal discountAmount;

    /**
     * 折扣率
     */
    private BigDecimal discountRate;

    /**
     * 最低使用金额
     */
    private BigDecimal minAmount;

    /**
     * 最大优惠金额
     */
    private BigDecimal maxDiscount;

    /**
     * 状态：0未使用 1已使用 2已过期
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 过期时间
     */
    private String expireTime;

    /**
     * 是否可用
     */
    private Boolean available;

    // Builder pattern support
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UserCouponVO vo = new UserCouponVO();

        public Builder id(Long id) {
            vo.id = id;
            return this;
        }

        public Builder couponId(Long couponId) {
            vo.couponId = couponId;
            return this;
        }

        public Builder couponName(String couponName) {
            vo.couponName = couponName;
            return this;
        }

        public Builder couponType(Integer couponType) {
            vo.couponType = couponType;
            return this;
        }

        public Builder typeDesc(String typeDesc) {
            vo.typeDesc = typeDesc;
            return this;
        }

        public Builder discountAmount(java.math.BigDecimal discountAmount) {
            vo.discountAmount = discountAmount;
            return this;
        }

        public Builder discountRate(java.math.BigDecimal discountRate) {
            vo.discountRate = discountRate;
            return this;
        }

        public Builder minAmount(java.math.BigDecimal minAmount) {
            vo.minAmount = minAmount;
            return this;
        }

        public Builder maxDiscount(java.math.BigDecimal maxDiscount) {
            vo.maxDiscount = maxDiscount;
            return this;
        }

        public Builder status(Integer status) {
            vo.status = status;
            return this;
        }

        public Builder statusDesc(String statusDesc) {
            vo.statusDesc = statusDesc;
            return this;
        }

        public Builder expireTime(String expireTime) {
            vo.expireTime = expireTime;
            return this;
        }

        public Builder available(Boolean available) {
            vo.available = available;
            return this;
        }

        public UserCouponVO build() {
            return vo;
        }
    }
}
