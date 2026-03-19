package com.petcloud.user.application.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcloud.common.core.exception.BusinessException;
import com.petcloud.common.core.exception.RespType;
import com.petcloud.user.domain.entity.ConsultationReview;
import com.petcloud.user.domain.service.ConsultationReviewService;
import com.petcloud.user.domain.vo.ConsultationReviewVO;
import com.petcloud.user.infrastructure.persistence.mapper.ConsultationReviewMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultationReviewServiceImpl implements ConsultationReviewService {

    private final ConsultationReviewMapper consultationReviewMapper;

    @Override
    public void submitReview(Long userId, String userNickname, Long consultationId, Long doctorId, Integer rating, String content) {
        LambdaQueryWrapper<ConsultationReview> check = new LambdaQueryWrapper<>();
        check.eq(ConsultationReview::getConsultationId, consultationId);
        Long existCount = consultationReviewMapper.selectCount(check);
        // 防止 NPE：如果 selectCount 返回 null，视为 0
        if (existCount != null && existCount > 0) {
            throw new BusinessException(RespType.CONSULTATION_ALREADY_REVIEWED);
        }

        int safeRating = rating == null ? 5 : Math.min(5, Math.max(1, rating));
        ConsultationReview review = new ConsultationReview();
        review.setConsultationId(consultationId);
        review.setDoctorId(doctorId);
        review.setUserId(userId);
        review.setUserNickname(userNickname);
        review.setRating(safeRating);
        review.setIsGood(safeRating >= 4 ? 1 : 0);
        review.setContent(content);
        consultationReviewMapper.insert(review);
    }

    @Override
    public List<ConsultationReviewVO> getDoctorReviews(Long doctorId) {
        LambdaQueryWrapper<ConsultationReview> query = new LambdaQueryWrapper<>();
        query.eq(ConsultationReview::getDoctorId, doctorId)
                .orderByDesc(ConsultationReview::getCreateTime);
        return consultationReviewMapper.selectList(query).stream()
                .map(r -> ConsultationReviewVO.builder()
                        .id(r.getId())
                        .userId(r.getUserId())
                        .userNickname(r.getUserNickname())
                        .rating(r.getRating())
                        .isGood(Integer.valueOf(1).equals(r.getIsGood()))
                        .content(r.getContent())
                        .createTime(r.getCreateTime())
                        .build())
                .collect(Collectors.toList());
    }
}
