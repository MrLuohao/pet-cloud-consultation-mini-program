package com.petcloud.user.domain.service;

import com.petcloud.user.domain.dto.HealthReminderCreateDTO;
import com.petcloud.user.domain.vo.HealthReminderVO;

import java.util.List;

/**
 * 健康提醒服务接口
 */
public interface HealthReminderService {

    List<HealthReminderVO> getList(Long userId);

    Long create(Long userId, HealthReminderCreateDTO dto);

    void markDone(Long reminderId, Long userId);

    void delete(Long reminderId, Long userId);
}
