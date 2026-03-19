package com.petcloud.user.domain.service;

import com.petcloud.user.domain.dto.CourseProgressDTO;
import com.petcloud.user.domain.vo.CourseProgressVO;

/**
 * 课程进度服务接口
 */
public interface CourseProgressService {

    CourseProgressVO getProgress(Long userId, Long courseId);

    void updateProgress(Long userId, Long courseId, CourseProgressDTO dto);
}
