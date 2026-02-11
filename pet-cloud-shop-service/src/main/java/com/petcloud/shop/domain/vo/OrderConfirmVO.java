package com.petcloud.shop.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单确认VO
 *
 * @author luohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderConfirmVO {

    /**
     * 商品列表
     */
    private List<OrderItemVO> items;

    /**
     * 收货地址
     */
    private AddressVO address;

    /**
     * 商品总数
     */
    private Integer totalCount;

    /**
     * 商品总金额
     */
    private BigDecimal totalAmount;

    /**
     * 运费
     */
    private BigDecimal freight;

    /**
     * 可用优惠券列表
     */
    private List<UserCouponVO> availableCoupons;

    // Builder pattern support
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private OrderConfirmVO vo = new OrderConfirmVO();

        public Builder items(List<OrderItemVO> items) {
            vo.items = items;
            return this;
        }

        public Builder address(AddressVO address) {
            vo.address = address;
            return this;
        }

        public Builder totalCount(Integer totalCount) {
            vo.totalCount = totalCount;
            return this;
        }

        public Builder totalAmount(java.math.BigDecimal totalAmount) {
            vo.totalAmount = totalAmount;
            return this;
        }

        public Builder freight(java.math.BigDecimal freight) {
            vo.freight = freight;
            return this;
        }

        public Builder availableCoupons(List<UserCouponVO> availableCoupons) {
            vo.availableCoupons = availableCoupons;
            return this;
        }

        public OrderConfirmVO build() {
            return vo;
        }
    }
}
