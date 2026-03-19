package com.petcloud.user.domain.service;

import com.petcloud.user.domain.vo.ConsultationReviewVO;

import java.util.List;

/**
 * 咨询评价服务接口
 */
public interface ConsultationReviewService {

    void submitReview(Long userId, String userNickname, Long consultationId, Long doctorId, Integer rating, String content);

    List<ConsultationReviewVO> getDoctorReviews(Long doctorId);
}
