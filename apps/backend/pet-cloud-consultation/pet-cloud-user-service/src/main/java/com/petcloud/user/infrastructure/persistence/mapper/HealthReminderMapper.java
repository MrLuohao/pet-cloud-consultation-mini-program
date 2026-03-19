package com.petcloud.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.user.domain.entity.HealthReminder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HealthReminderMapper extends BaseMapper<HealthReminder> {
}
