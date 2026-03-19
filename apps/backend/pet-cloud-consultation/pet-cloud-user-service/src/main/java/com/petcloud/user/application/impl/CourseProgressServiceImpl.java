package com.petcloud.user.application.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcloud.user.domain.dto.CourseProgressDTO;
import com.petcloud.user.domain.entity.CourseProgress;
import com.petcloud.user.domain.service.CourseProgressService;
import com.petcloud.user.domain.vo.CourseProgressVO;
import com.petcloud.user.infrastructure.persistence.mapper.CourseProgressMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseProgressServiceImpl implements CourseProgressService {

    private final CourseProgressMapper courseProgressMapper;

    @Override
    public CourseProgressVO getProgress(Long userId, Long courseId) {
        LambdaQueryWrapper<CourseProgress> query = new LambdaQueryWrapper<>();
        query.eq(CourseProgress::getUserId, userId)
                .eq(CourseProgress::getCourseId, courseId);
        CourseProgress progress = courseProgressMapper.selectOne(query);
        if (progress == null) {
            return null;
        }
        return CourseProgressVO.builder()
                .courseId(courseId)
                .chapterId(progress.getChapterId())
                .progress(progress.getProgress())
                .watchSeconds(progress.getWatchSeconds())
                .isCompleted(Integer.valueOf(1).equals(progress.getIsCompleted()))
                .build();
    }

    @Override
    public void updateProgress(Long userId, Long courseId, CourseProgressDTO dto) {
        LambdaQueryWrapper<CourseProgress> query = new LambdaQueryWrapper<>();
        query.eq(CourseProgress::getUserId, userId)
                .eq(CourseProgress::getCourseId, courseId);
        CourseProgress existing = courseProgressMapper.selectOne(query);

        int safeProgress = Math.min(100, Math.max(0, dto.getProgress() == null ? 0 : dto.getProgress()));

        if (existing == null) {
            CourseProgress newProgress = new CourseProgress();
            newProgress.setUserId(userId);
            newProgress.setCourseId(courseId);
            newProgress.setChapterId(dto.getChapterId());
            newProgress.setProgress(safeProgress);
            newProgress.setWatchSeconds(dto.getWatchSeconds() == null ? 0 : dto.getWatchSeconds());
            newProgress.setIsCompleted(safeProgress >= 100 ? 1 : 0);
            courseProgressMapper.insert(newProgress);
        } else {
            existing.setChapterId(dto.getChapterId());
            existing.setProgress(safeProgress);
            if (dto.getWatchSeconds() != null && dto.getWatchSeconds() > (existing.getWatchSeconds() == null ? 0 : existing.getWatchSeconds())) {
                existing.setWatchSeconds(dto.getWatchSeconds());
            }
            if (safeProgress >= 100) {
                existing.setIsCompleted(1);
            }
            courseProgressMapper.updateById(existing);
        }
    }
}
