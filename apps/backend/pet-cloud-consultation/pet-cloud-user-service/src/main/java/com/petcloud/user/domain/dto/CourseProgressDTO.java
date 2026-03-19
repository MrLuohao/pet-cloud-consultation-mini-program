package com.petcloud.user.domain.dto;

import lombok.Data;

/**
 * 课程进度更新DTO
 */
@Data
public class CourseProgressDTO {

    private String chapterId;

    /** 进度百分比 0-100 */
    private Integer progress;

    private Integer watchSeconds;
}
