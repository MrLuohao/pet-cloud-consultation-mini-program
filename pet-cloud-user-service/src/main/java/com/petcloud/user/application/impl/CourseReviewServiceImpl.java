package com.petcloud.user.application.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcloud.common.core.exception.BusinessException;
import com.petcloud.common.core.exception.RespType;
import com.petcloud.user.domain.entity.CourseReview;
import com.petcloud.user.domain.service.CourseReviewService;
import com.petcloud.user.domain.vo.CourseReviewVO;
import com.petcloud.user.infrastructure.persistence.mapper.CourseReviewMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseReviewServiceImpl implements CourseReviewService {

    private final CourseReviewMapper courseReviewMapper;

    @Override
    public void submitReview(Long userId, String userNickname, Long courseId, Integer rating, String content) {
        // 每人每课只能评价一次
        LambdaQueryWrapper<CourseReview> check = new LambdaQueryWrapper<>();
        check.eq(CourseReview::getUserId, userId)
                .eq(CourseReview::getCourseId, courseId);
        if (courseReviewMapper.selectCount(check) > 0) {
            throw new BusinessException(RespType.COURSE_ALREADY_REVIEWED);
        }

        CourseReview review = new CourseReview();
        review.setCourseId(courseId);
        review.setUserId(userId);
        review.setUserNickname(userNickname);
        review.setRating(rating == null ? 5 : Math.min(5, Math.max(1, rating)));
        review.setContent(content);
        courseReviewMapper.insert(review);
    }

    @Override
    public List<CourseReviewVO> getReviews(Long courseId) {
        LambdaQueryWrapper<CourseReview> query = new LambdaQueryWrapper<>();
        query.eq(CourseReview::getCourseId, courseId)
                .orderByDesc(CourseReview::getCreateTime);
        return courseReviewMapper.selectList(query).stream()
                .map(r -> CourseReviewVO.builder()
                        .id(r.getId())
                        .userId(r.getUserId())
                        .userNickname(r.getUserNickname())
                        .rating(r.getRating())
                        .content(r.getContent())
                        .createTime(r.getCreateTime())
                        .build())
                .collect(Collectors.toList());
    }
}
