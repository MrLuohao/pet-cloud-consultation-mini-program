package com.petcloud.shop.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商品实体类
 *
 * @author luohao
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product")
public class Product extends BaseEntity {

    /**
     * 分类ID
     */
    @TableField("category_id")
    private Long categoryId;

    /**
     * 商品名称
     */
    @TableField("name")
    private String name;

    /**
     * 封面图
     */
    @TableField("cover_url")
    private String coverUrl;

    /**
     * 商品图片（JSON数组）
     */
    @TableField("image_urls")
    private String imageUrls;

    /**
     * 商品简介
     */
    @TableField("summary")
    private String summary;

    /**
     * 商品价格
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 原价
     */
    @TableField("original_price")
    private BigDecimal originalPrice;

    /**
     * 库存
     */
    @TableField("stock")
    private Integer stock;

    /**
     * 销量
     */
    @TableField("sales")
    private Integer sales;

    /**
     * 评分
     */
    @TableField("rating")
    private java.math.BigDecimal rating;

    /**
     * 评价数
     */
    @TableField("review_count")
    private Integer reviewCount;

    /**
     * 标签(热门/新品/推荐)
     */
    @TableField("tag")
    private String tag;

    /**
     * 状态(0下架/1上架)
     */
    @TableField("status")
    private Integer status;

    /**
     * 排序
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 状态枚举
     */
    public enum Status {
        OFFLINE(0, "下架"),
        ONLINE(1, "上架");

        private final Integer code;
        private final String desc;

        Status(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 是否有折扣
     */
    public boolean hasDiscount() {
        return originalPrice != null && originalPrice.compareTo(price) > 0;
    }
}
