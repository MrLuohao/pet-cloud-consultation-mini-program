package com.petcloud.shop.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 订单商品实体类
 *
 * @author luohao
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("order_item")
public class OrderItem extends BaseEntity {

    /**
     * 订单ID
     */
    @TableField("order_id")
    private Long orderId;

    /**
     * 商品ID
     */
    @TableField("product_id")
    private Long productId;

    /**
     * 商品名称
     */
    @TableField("product_name")
    private String productName;

    /**
     * 商品封面
     */
    @TableField("cover_url")
    private String coverUrl;

    /**
     * 店铺标识
     */
    @TableField("shop_id")
    private String shopId;

    /**
     * 店铺名称
     */
    @TableField("shop_name")
    private String shopName;

    /**
     * 服务文案
     */
    @TableField("service_text")
    private String serviceText;

    /**
     * 规格快照
     */
    @TableField("spec_snapshot")
    private String specSnapshot;

    /**
     * sku 快照
     */
    @TableField("sku_snapshot")
    private String skuSnapshot;

    /**
     * 商品单价
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 商品原价
     */
    @TableField("original_price")
    private BigDecimal originalPrice;

    /**
     * 购买数量
     */
    @TableField("quantity")
    private Integer quantity;

    /**
     * 小计
     */
    @TableField("subtotal")
    private BigDecimal subtotal;
}
