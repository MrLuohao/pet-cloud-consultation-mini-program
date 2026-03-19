package com.petcloud.shop.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 可评价订单项VO
 *
 * @author luohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewableOrderItemVO {

    /**
     * 订单项ID
     */
    private Long orderItemId;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品封面
     */
    private String coverUrl;

    /**
     * 商品单价
     */
    private BigDecimal price;

    /**
     * 购买数量
     */
    private Integer quantity;

    /**
     * 是否已购买过此商品
     */
    private Boolean hasPurchased;

    /**
     * 是否已全部评价
     */
    private Boolean allReviewed;
}
