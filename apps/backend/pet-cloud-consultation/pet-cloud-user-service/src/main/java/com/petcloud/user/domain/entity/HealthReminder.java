package com.petcloud.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 健康提醒实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("health_reminder")
public class HealthReminder extends BaseEntity {

    @TableField("user_id")
    private Long userId;

    @TableField("pet_id")
    private Long petId;

    @TableField("pet_name")
    private String petName;

    /** 类型: vaccine, checkup, medicine, deworming, other */
    @TableField("reminder_type")
    private String reminderType;

    @TableField("title")
    private String title;

    @TableField("remind_date")
    private LocalDate remindDate;

    /** 是否完成: 0否 1是 */
    @TableField("is_done")
    private Integer isDone;

    @TableField("note")
    private String note;
}
