package com.petcloud.shop.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品详情VO
 *
 * @author luohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailVO {

    /**
     * 商品ID
     */
    private Long id;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 封面图
     */
    private String coverUrl;

    /**
     * 商品图片列表
     */
    private List<String> imageUrls;

    /**
     * 商品简介
     */
    private String summary;

    /**
     * 商品价格
     */
    private BigDecimal price;

    /**
     * 原价
     */
    private BigDecimal originalPrice;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 销量
     */
    private Integer sales;

    /**
     * 评分
     */
    private BigDecimal rating;

    /**
     * 评价数
     */
    private Integer reviewCount;

    /**
     * 标签
     */
    private String tag;

    /**
     * 商品评价列表
     */
    private List<ProductReviewVO> reviews;

    // Builder pattern support
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ProductDetailVO vo = new ProductDetailVO();

        public Builder id(Long id) {
            vo.id = id;
            return this;
        }

        public Builder categoryId(Long categoryId) {
            vo.categoryId = categoryId;
            return this;
        }

        public Builder categoryName(String categoryName) {
            vo.categoryName = categoryName;
            return this;
        }

        public Builder name(String name) {
            vo.name = name;
            return this;
        }

        public Builder coverUrl(String coverUrl) {
            vo.coverUrl = coverUrl;
            return this;
        }

        public Builder imageUrls(List<String> imageUrls) {
            vo.imageUrls = imageUrls;
            return this;
        }

        public Builder summary(String summary) {
            vo.summary = summary;
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

        public Builder stock(Integer stock) {
            vo.stock = stock;
            return this;
        }

        public Builder sales(Integer sales) {
            vo.sales = sales;
            return this;
        }

        public Builder rating(java.math.BigDecimal rating) {
            vo.rating = rating;
            return this;
        }

        public Builder reviewCount(Integer reviewCount) {
            vo.reviewCount = reviewCount;
            return this;
        }

        public Builder tag(String tag) {
            vo.tag = tag;
            return this;
        }

        public Builder reviews(List<ProductReviewVO> reviews) {
            vo.reviews = reviews;
            return this;
        }

        public ProductDetailVO build() {
            return vo;
        }
    }
}
