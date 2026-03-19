package com.petcloud.user.application.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petcloud.user.domain.entity.FeaturedContentPublish;
import com.petcloud.user.domain.service.FeaturedContentService;
import com.petcloud.user.domain.vo.FeaturedContentVO;
import com.petcloud.user.infrastructure.persistence.mapper.FeaturedContentPublishMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeaturedContentServiceImpl implements FeaturedContentService {

    private static final int DEFAULT_LIMIT = 6;

    private final FeaturedContentPublishMapper featuredContentPublishMapper;

    @Override
    public List<FeaturedContentVO> getPublishedContents(int limit) {
        int effectiveLimit = limit > 0 ? Math.min(limit, 10) : DEFAULT_LIMIT;
        Date now = new Date();

        LambdaQueryWrapper<FeaturedContentPublish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FeaturedContentPublish::getStatus, FeaturedContentPublish.Status.PUBLISHED.getCode())
                .eq(FeaturedContentPublish::getIsDeleted, 0)
                .le(FeaturedContentPublish::getStartTime, now)
                .and(wrapper -> wrapper.isNull(FeaturedContentPublish::getEndTime)
                        .or()
                        .ge(FeaturedContentPublish::getEndTime, now))
                .orderByAsc(FeaturedContentPublish::getPositionNo)
                .orderByDesc(FeaturedContentPublish::getPublishDate)
                .orderByDesc(FeaturedContentPublish::getId);

        Page<FeaturedContentPublish> page = featuredContentPublishMapper.selectPage(new Page<>(1, effectiveLimit), queryWrapper);
        return page.getRecords().stream()
                .map(item -> FeaturedContentVO.builder()
                        .id(item.getId())
                        .draftId(item.getDraftId())
                        .title(item.getTitle())
                        .summary(item.getSummary())
                        .coverUrl(item.getCoverUrl())
                        .tag(item.getTag())
                        .reasonLabel(item.getReasonLabel())
                        .targetPage(item.getTargetPage())
                        .targetId(item.getTargetId())
                        .positionNo(item.getPositionNo())
                        .startTime(item.getStartTime())
                        .endTime(item.getEndTime())
                        .build())
                .toList();
    }
}
