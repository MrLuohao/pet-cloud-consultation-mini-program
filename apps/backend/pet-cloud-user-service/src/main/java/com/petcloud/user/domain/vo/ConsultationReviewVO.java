package com.petcloud.user.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 咨询评价VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationReviewVO {

    private Long id;

    private Long userId;

    private String userNickname;

    private Integer rating;

    private Boolean isGood;

    private String content;

    private Date createTime;
}
