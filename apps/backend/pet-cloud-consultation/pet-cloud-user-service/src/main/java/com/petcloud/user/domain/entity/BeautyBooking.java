package com.petcloud.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 美容预约实体类
 *
 * @author luohao
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("beauty_booking")
public class BeautyBooking extends BaseEntity {

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 门店ID
     */
    @TableField("store_id")
    private Long storeId;

    /**
     * 宠物ID
     */
    @TableField("pet_id")
    private Long petId;

    /**
     * 预约日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @TableField("booking_date")
    private LocalDate bookingDate;

    /**
     * 预约时间段
     */
    @TableField("booking_time")
    private String bookingTime;

    /**
     * 服务项目（逗号分隔）
     */
    @TableField("services")
    private String services;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 状态(0待确认/1已确认/2已完成/3已取消)
     */
    @TableField("status")
    private Integer status;

    /**
     * 服务前照片
     */
    @TableField("before_photo")
    private String beforePhoto;

    /**
     * 服务后照片
     */
    @TableField("after_photo")
    private String afterPhoto;

    /**
     * 服务过程照片（JSON数组）
     */
    @TableField("service_photos")
    private String servicePhotos;

    /**
     * 状态枚举
     */
    public enum Status {
        PENDING(0, "待确认"),
        CONFIRMED(1, "已确认"),
        COMPLETED(2, "已完成"),
        CANCELLED(3, "已取消");

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
