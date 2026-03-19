package com.petcloud.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 课程评价实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course_review")
public class CourseReview extends BaseEntity {

    @TableField("course_id")
    private Long courseId;

    @TableField("user_id")
    private Long userId;

    /** 用户昵称（冗余） */
    @TableField("user_nickname")
    private String userNickname;

    /** 评分 1-5 */
    @TableField("rating")
    private Integer rating;

    @TableField("content")
    private String content;
}
