package com.petcloud.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 课程学习进度实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course_progress")
public class CourseProgress extends BaseEntity {

    @TableField("user_id")
    private Long userId;

    @TableField("course_id")
    private Long courseId;

    /** 当前章节ID */
    @TableField("chapter_id")
    private String chapterId;

    /** 进度百分比 0-100 */
    @TableField("progress")
    private Integer progress;

    /** 已观看秒数 */
    @TableField("watch_seconds")
    private Integer watchSeconds;

    /** 是否完成: 0否 1是 */
    @TableField("is_completed")
    private Integer isCompleted;
}
