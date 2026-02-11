package com.petcloud.user.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 课程评价VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseReviewVO {

    private Long id;

    private Long userId;

    private String userNickname;

    private String userAvatar;

    private Integer rating;

    private String content;

    private Date createTime;
}
