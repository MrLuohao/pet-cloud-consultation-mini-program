package com.petcloud.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 美容门店实体类
 *
 * @author luohao
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("beauty_store")
public class BeautyStore extends BaseEntity {

    /**
     * 门店名称
     */
    @TableField("name")
    private String name;

    /**
     * 封面图
     */
    @TableField("cover_url")
    private String coverUrl;

    /**
     * 评分
     */
    @TableField("rating")
    private BigDecimal rating;

    /**
     * 距离
     */
    @TableField("distance")
    private String distance;

    /**
     * 地址
     */
    @TableField("address")
    private String address;

    /**
     * 服务标签（逗号分隔）
     */
    @TableField("tags")
    private String tags;

    /**
     * 纬度
     */
    @TableField("latitude")
    private BigDecimal latitude;

    /**
     * 经度
     */
    @TableField("longitude")
    private BigDecimal longitude;

    /**
     * 联系电话
     */
    @TableField("phone")
    private String phone;

    /**
     * 营业时间
     */
    @TableField("business_hours")
    private String businessHours;

    /**
     * 状态(0关闭/1营业)
     */
    @TableField("status")
    private Integer status;

    /**
     * 状态枚举
     */
    public enum Status {
        CLOSED(0, "关闭"),
        OPEN(1, "营业");

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
}
