package com.petcloud.shop.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 购物车实体类
 * 注意：购物车不使用逻辑删除，删除即真正删除
 *
 * @author luohao
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("shopping_cart")
public class ShoppingCart extends BaseEntity {

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
     * 数量
     */
    @TableField("quantity")
    private Integer quantity;

    /**
     * 覆盖父类的逻辑删除字段，不使用 @TableLogic 注解
     * 购物车删除时直接物理删除，不做软删除
     */
    @TableField(value = "is_deleted", exist = false)
    private Integer isDeleted;
}
