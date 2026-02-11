package com.petcloud.shop.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 购物车VO
 *
 * @author luohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartVO {

    /**
     * 购物车ID
     */
    private Long id;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品分类名称
     */
    private String categoryName;

    /**
     * 商品封面
     */
    private String coverUrl;

    /**
     * 商品价格
     */
    private BigDecimal price;

    /**
     * 商品原价
     */
    private BigDecimal originalPrice;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 小计
     */
    private BigDecimal subtotal;

    /**
     * 选中状态
     */
    private Boolean selected;

    // Builder pattern support
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private CartVO vo = new CartVO();

        public Builder id(Long id) {
            vo.id = id;
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

        public Builder categoryName(String categoryName) {
            vo.categoryName = categoryName;
            return this;
        }

        public Builder coverUrl(String coverUrl) {
            vo.coverUrl = coverUrl;
            return this;
        }

        public Builder price(java.math.BigDecimal price) {
            vo.price = price;
            return this;
        }

        public Builder originalPrice(java.math.BigDecimal originalPrice) {
            vo.originalPrice = originalPrice;
            return this;
        }

        public Builder quantity(Integer quantity) {
            vo.quantity = quantity;
            return this;
        }

        public Builder stock(Integer stock) {
            vo.stock = stock;
            return this;
        }

        public Builder subtotal(java.math.BigDecimal subtotal) {
            vo.subtotal = subtotal;
            return this;
        }

        public Builder selected(Boolean selected) {
            vo.selected = selected;
            return this;
        }

        public CartVO build() {
            return vo;
        }
    }
}
