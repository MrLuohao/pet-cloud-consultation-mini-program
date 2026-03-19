package com.petcloud.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 美容预约操作日志实体类
 *
 * @author luohao
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("beauty_booking_log")
public class BeautyBookingLog extends BaseEntity {

    @TableField("booking_id")
    private Long bookingId;

    @TableField("status")
    private Integer status;

    @TableField("status_text")
    private String statusText;

    @TableField("operator_id")
    private Long operatorId;

    @TableField("remark")
    private String remark;
}
