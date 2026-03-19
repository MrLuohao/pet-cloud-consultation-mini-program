package com.petcloud.user.domain.dto;

import lombok.Data;

/**
 * 课程评价提交DTO
 */
@Data
public class CourseReviewDTO {

    /** 评分 1-5 */
    private Integer rating;

    private String content;
}
