package com.petcloud.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 医生实体类
 *
 * @author luohao
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("doctor")
public class Doctor extends BaseEntity {

    /**
     * 医生姓名
     */
    @TableField("name")
    private String name;

    /**
     * 头像
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 职称
     */
    @TableField("title")
    private String title;

    /**
     * 专长
     */
    @TableField("specialty")
    private String specialty;

    /**
     * 科室
     */
    @TableField("department")
    private String department;

    /**
     * 从业年限
     */
    @TableField("experience")
    private Integer experience;

    /**
     * 医生简介
     */
    @TableField("description")
    private String description;

    /**
     * 所属医院
     */
    @TableField("hospital_name")
    private String hospitalName;

    /**
     * 咨询费
     */
    @TableField("consultation_fee")
    private BigDecimal consultationFee;

    /**
     * 评分
     */
    @TableField("rating")
    private BigDecimal rating;

    /**
     * 咨询次数
     */
    @TableField("consultation_count")
    private Integer consultationCount;

    /**
     * 标签（逗号分隔）
     */
    @TableField("tags")
    private String tags;

    /**
     * 状态：0禁用 1启用
     */
    @TableField("status")
    private Integer status;

    /**
     * 排序
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 在线状态：0离线 1在线
     */
    @TableField("online_status")
    private Integer onlineStatus;

    /**
     * 平均响应时长（分钟）
     */
    @TableField("avg_response_minutes")
    private java.math.BigDecimal avgResponseMinutes;
}
