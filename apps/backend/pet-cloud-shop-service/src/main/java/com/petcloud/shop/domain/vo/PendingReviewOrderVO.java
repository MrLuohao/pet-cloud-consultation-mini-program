package com.petcloud.shop.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 待评价订单VO
 *
 * @author luohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingReviewOrderVO {

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 订单完成时间
     */
    private Date completeTime;

    /**
     * 待评价商品列表
     */
    private List<PendingReviewItemVO> items;

    /**
     * 待评价商品项VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PendingReviewItemVO {

        /**
         * 订单项ID
         */
        private Long orderItemId;

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
         * 商品价格
         */
        private BigDecimal price;

        /**
         * 数量
         */
        private Integer quantity;

        /**
         * 是否已评价
         */
        private Boolean reviewed;
    }
}
