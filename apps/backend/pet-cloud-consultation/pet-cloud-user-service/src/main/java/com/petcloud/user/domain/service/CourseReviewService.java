package com.petcloud.user.domain.service;

import com.petcloud.user.domain.vo.CourseReviewVO;

import java.util.List;

/**
 * 课程评价服务接口
 */
public interface CourseReviewService {

    void submitReview(Long userId, String userNickname, Long courseId, Integer rating, String content);

    List<CourseReviewVO> getReviews(Long courseId);
}
