package com.petcloud.user.domain.service;

import com.petcloud.user.domain.vo.CourseVO;

import java.util.List;

/**
 * 课程服务接口
 *
 * @author luohao
 */
public interface CourseService {

    /**
     * 获取课程列表
     *
     * @return 课程VO列表
     */
    List<CourseVO> getCourseList();

    /**
     * 获取课程详情
     *
     * @param courseId 课程ID
     * @return 课程VO
     */
    CourseVO getCourseDetail(Long courseId);

    CourseVO getCourseDetail(Long courseId, Long userId);
}
