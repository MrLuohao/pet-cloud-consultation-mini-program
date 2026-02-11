package com.petcloud.shop.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商品收藏实体类
 * 注意：收藏不使用逻辑删除，删除即真正删除
 *
 * @author luohao
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product_collection")
public class ProductCollection extends BaseEntity {

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

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
     * 商品图片
     */
    @TableField("product_image")
    private String productImage;

    /**
     * 商品价格
     */
    @TableField("product_price")
    private BigDecimal productPrice;

    /**
     * 覆盖父类的逻辑删除字段，不使用 @TableLogic 注解
     * 收藏删除时直接物理删除，不做软删除
     */
    @TableField(value = "is_deleted", exist = false)
    private Integer isDeleted;
}
