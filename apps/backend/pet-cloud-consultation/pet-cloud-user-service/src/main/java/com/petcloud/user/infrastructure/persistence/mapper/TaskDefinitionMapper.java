package com.petcloud.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.user.domain.entity.TaskDefinition;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务定义Mapper接口
 *
 * @author luohao
 */
@Mapper
public interface TaskDefinitionMapper extends BaseMapper<TaskDefinition> {
}
