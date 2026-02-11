package com.petcloud.user.domain.service;

import com.petcloud.user.domain.vo.HealthReminderVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 健康提醒服务接口
 */
public interface HealthReminderService {

    List<HealthReminderVO> getList(Long userId);

    Long create(Long userId, Long petId, String petName, String reminderType, String title, LocalDate remindDate, String note);

    void markDone(Long reminderId, Long userId);

    void delete(Long reminderId, Long userId);
}
