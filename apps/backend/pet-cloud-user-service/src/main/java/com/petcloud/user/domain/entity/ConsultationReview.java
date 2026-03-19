package com.petcloud.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 咨询评价实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("consultation_review")
public class ConsultationReview extends BaseEntity {

    @TableField("consultation_id")
    private Long consultationId;

    @TableField("doctor_id")
    private Long doctorId;

    @TableField("user_id")
    private Long userId;

    @TableField("user_nickname")
    private String userNickname;

    /** 评分 1-5 */
    @TableField("rating")
    private Integer rating;

    /** 是否好评 */
    @TableField("is_good")
    private Integer isGood;

    @TableField("content")
    private String content;
}
