package com.petcloud.shop.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 商品收藏VO
 *
 * @author luohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCollectionVO {

    /**
     * 收藏ID
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
     * 商品图片
     */
    private String productImage;

    /**
     * 商品价格
     */
    private BigDecimal productPrice;

    /**
     * 商品原价
     */
    private BigDecimal originalPrice;

    /**
     * 商品库存
     */
    private Integer stock;

    /**
     * 商品状态(0下架/1上架)
     */
    private Integer status;

    /**
     * 收藏时间
     */
    private Date collectTime;

    // Builder pattern support
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ProductCollectionVO vo = new ProductCollectionVO();

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

        public Builder productImage(String productImage) {
            vo.productImage = productImage;
            return this;
        }

        public Builder productPrice(BigDecimal productPrice) {
            vo.productPrice = productPrice;
            return this;
        }

        public Builder originalPrice(BigDecimal originalPrice) {
            vo.originalPrice = originalPrice;
            return this;
        }

        public Builder stock(Integer stock) {
            vo.stock = stock;
            return this;
        }

        public Builder status(Integer status) {
            vo.status = status;
            return this;
        }

        public Builder collectTime(Date collectTime) {
            vo.collectTime = collectTime;
            return this;
        }

        public ProductCollectionVO build() {
            return vo;
        }
    }
}
