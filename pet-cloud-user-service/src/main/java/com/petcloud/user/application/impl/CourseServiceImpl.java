package com.petcloud.user.application.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petcloud.common.core.exception.BusinessException;
import com.petcloud.common.core.exception.RespType;
import com.petcloud.user.domain.entity.Course;
import com.petcloud.user.domain.entity.CourseProgress;
import com.petcloud.user.domain.service.CourseService;
import com.petcloud.user.domain.vo.ChapterVO;
import com.petcloud.user.domain.vo.CourseProgressVO;
import com.petcloud.user.domain.vo.CourseVO;
import com.petcloud.user.infrastructure.persistence.mapper.CourseMapper;
import com.petcloud.user.infrastructure.persistence.mapper.CourseProgressMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 课程服务实现类
 *
 * @author luohao
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseMapper courseMapper;
    private final CourseProgressMapper courseProgressMapper;
    private final ObjectMapper objectMapper;

    @Override
    public List<CourseVO> getCourseList() {
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Course::getStatus, Course.Status.ONLINE.getCode())
                .orderByAsc(Course::getSortOrder)
                .orderByDesc(Course::getCreateTime);
        List<Course> courses = courseMapper.selectList(queryWrapper);
        return courses.stream()
                .map(c -> convertToVO(c, null))
                .collect(Collectors.toList());
    }

    @Override
    public CourseVO getCourseDetail(Long courseId) {
        return getCourseDetail(courseId, null);
    }

    @Override
    public CourseVO getCourseDetail(Long courseId, Long userId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException(RespType.COURSE_NOT_FOUND);
        }
        CourseProgressVO progress = null;
        if (userId != null) {
            LambdaQueryWrapper<CourseProgress> pq = new LambdaQueryWrapper<>();
            pq.eq(CourseProgress::getUserId, userId)
              .eq(CourseProgress::getCourseId, courseId);
            CourseProgress cp = courseProgressMapper.selectOne(pq);
            if (cp != null) {
                progress = CourseProgressVO.builder()
                        .courseId(courseId)
                        .chapterId(cp.getChapterId())
                        .progress(cp.getProgress())
                        .watchSeconds(cp.getWatchSeconds())
                        .isCompleted(cp.getIsCompleted() != null && cp.getIsCompleted() == 1)
                        .build();
            }
        }
        return convertToVO(course, progress);
    }

    private CourseVO convertToVO(Course course, CourseProgressVO progress) {
        List<ChapterVO> chapters = parseChapters(course.getChapters());
        return CourseVO.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .coverUrl(course.getCoverUrl())
                .lessonCount(course.getLessonCount())
                .studentCount(course.getStudentCount())
                .price(course.getPrice())
                .tag(course.getTag())
                .instructorName(course.getInstructorName())
                .instructorAvatar(course.getInstructorAvatar())
                .instructorBio(course.getInstructorBio())
                .chapters(chapters)
                .userProgress(progress != null ? progress.getProgress() : null)
                .isCompleted(progress != null && Boolean.TRUE.equals(progress.getIsCompleted()))
                .build();
    }

    private List<ChapterVO> parseChapters(String chaptersJson) {
        if (!StringUtils.hasText(chaptersJson)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(chaptersJson, new TypeReference<List<ChapterVO>>() {});
        } catch (Exception e) {
            log.warn("解析章节JSON失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
