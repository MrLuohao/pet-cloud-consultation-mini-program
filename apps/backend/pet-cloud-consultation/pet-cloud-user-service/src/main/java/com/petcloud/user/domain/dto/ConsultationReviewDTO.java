package com.petcloud.user.domain.dto;

import lombok.Data;

/**
 * 咨询评价提交DTO
 */
@Data
public class ConsultationReviewDTO {

    /** 评分 1-5 */
    private Integer rating;

    private String content;
}
