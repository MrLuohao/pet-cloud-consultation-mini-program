package com.petcloud.shop.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 商品订阅实体类
 *
 * @author luohao
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product_subscription")
public class ProductSubscription extends BaseEntity {

    @TableField("user_id")
    private Long userId;

    @TableField("product_id")
    private Long productId;

    @TableField("sku_id")
    private Long skuId;

    @TableField("quantity")
    private Integer quantity;

    /** 配送周期（天） */
    @TableField("cycle_days")
    private Integer cycleDays;

    @TableField("address_id")
    private Long addressId;

    /** 0=正常 1=暂停 2=取消 */
    @TableField("status")
    private Integer status;

    @TableField("next_order_date")
    private LocalDate nextOrderDate;

    /** 折扣率，默认0.90 */
    @TableField("discount_rate")
    private BigDecimal discountRate;

    public enum Status {
        ACTIVE(0, "正常"),
        PAUSED(1, "暂停"),
        CANCELLED(2, "取消");

        private final Integer code;
        private final String desc;

        Status(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() { return code; }
        public String getDesc() { return desc; }
    }
}
