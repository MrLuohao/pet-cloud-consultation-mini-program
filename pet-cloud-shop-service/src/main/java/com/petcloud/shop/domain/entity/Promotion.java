package com.petcloud.shop.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 满减活动实体类
 *
 * @author luohao
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("promotion")
public class Promotion extends BaseEntity {

    /**
     * 活动名称
     */
    @TableField("name")
    private String name;

    /**
     * 类型：1满减
     */
    @TableField("type")
    private Integer type;

    /**
     * 满减门槛金额
     */
    @TableField("threshold")
    private BigDecimal threshold;

    /**
     * 优惠金额
     */
    @TableField("discount")
    private BigDecimal discount;

    /**
     * 开始时间
     */
    @TableField("start_time")
    private String startTime;

    /**
     * 结束时间
     */
    @TableField("end_time")
    private String endTime;

    /**
     * 状态：0禁用 1启用
     */
    @TableField("status")
    private Integer status;

    /**
     * 活动描述
     */
    @TableField("description")
    private String description;

    /**
     * 活动类型枚举
     */
    public enum Type {
        FULL_REDUCTION(1, "满减");

        private final Integer code;
        private final String desc;

        Type(Integer code, String desc) {
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
}
