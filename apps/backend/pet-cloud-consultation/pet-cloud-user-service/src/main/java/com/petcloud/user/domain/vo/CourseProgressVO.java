package com.petcloud.user.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 课程学习进度VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseProgressVO {

    private Long courseId;

    private String chapterId;

    /** 进度百分比 0-100 */
    private Integer progress;

    private Integer watchSeconds;

    private Boolean isCompleted;
}
