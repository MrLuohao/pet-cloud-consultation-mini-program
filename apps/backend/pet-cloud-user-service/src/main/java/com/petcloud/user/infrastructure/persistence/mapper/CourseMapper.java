package com.petcloud.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.user.domain.entity.Course;
import org.apache.ibatis.annotations.Mapper;

/**
 * 课程Mapper接口
 *
 * @author luohao
 */
@Mapper
public interface CourseMapper extends BaseMapper<Course> {
}
