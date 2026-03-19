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
     * 商品金额
     */
    private BigDecimal goodsAmount;

    /**
     * 运费
     */
    private BigDecimal freight;

    /**
     * 优惠金额
     */
    private BigDecimal couponDiscount;

    /**
     * 应付金额
     */
    private BigDecimal payAmount;

    /**
     * 可用优惠券列表
     */
    private List<UserCouponVO> availableCoupons;

    /**
     * 配送文案
     */
    private String deliveryText;

    /**
     * 订单提示
     */
    private String orderHint;

    /**
     * 当前选中的支付方式
     */
    private String selectedPaymentMethod;

    /**
     * 可选支付方式
     */
    private List<PaymentMethodVO> paymentMethods;
}
