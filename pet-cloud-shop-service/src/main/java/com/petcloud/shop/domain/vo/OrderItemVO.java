package com.petcloud.shop.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 订单项VO
 *
 * @author luohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemVO {

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
     * 小计
     */
    private BigDecimal subtotal;

    /**
     * 是否已评价
     */
    private Boolean reviewed;

    // Builder pattern support
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private OrderItemVO vo = new OrderItemVO();

        public Builder orderItemId(Long orderItemId) {
            vo.orderItemId = orderItemId;
            return this;
        }

        public Builder productId(Long productId) {
            vo.productId = productId;
            return this;
        }

        public Builder productName(String productName) {
            vo.productName = productName;
            return this;
        }

        public Builder coverUrl(String coverUrl) {
            vo.coverUrl = coverUrl;
            return this;
        }

        public Builder price(BigDecimal price) {
            vo.price = price;
            return this;
        }

        public Builder quantity(Integer quantity) {
            vo.quantity = quantity;
            return this;
        }

        public Builder subtotal(BigDecimal subtotal) {
            vo.subtotal = subtotal;
            return this;
        }

        public Builder reviewed(Boolean reviewed) {
            vo.reviewed = reviewed;
            return this;
        }

        public OrderItemVO build() {
            return vo;
        }
    }
}
