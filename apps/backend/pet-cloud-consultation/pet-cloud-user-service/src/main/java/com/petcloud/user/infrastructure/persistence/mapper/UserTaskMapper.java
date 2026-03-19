package com.petcloud.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.user.domain.entity.UserTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户任务Mapper接口
 *
 * @author luohao
 */
@Mapper
public interface UserTaskMapper extends BaseMapper<UserTask> {
}
