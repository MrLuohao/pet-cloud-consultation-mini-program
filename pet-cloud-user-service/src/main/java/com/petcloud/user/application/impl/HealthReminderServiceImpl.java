package com.petcloud.user.application.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petcloud.common.core.exception.BusinessException;
import com.petcloud.common.core.exception.RespType;
import com.petcloud.user.domain.entity.HealthReminder;
import com.petcloud.user.domain.service.HealthReminderService;
import com.petcloud.user.domain.vo.HealthReminderVO;
import com.petcloud.user.infrastructure.persistence.mapper.HealthReminderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthReminderServiceImpl implements HealthReminderService {

    private final HealthReminderMapper healthReminderMapper;

    @Override
    public List<HealthReminderVO> getList(Long userId) {
        LambdaQueryWrapper<HealthReminder> query = new LambdaQueryWrapper<>();
        query.eq(HealthReminder::getUserId, userId)
                .orderByAsc(HealthReminder::getRemindDate);
        return healthReminderMapper.selectList(query).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public Long create(Long userId, Long petId, String petName, String reminderType, String title, LocalDate remindDate, String note) {
        HealthReminder reminder = new HealthReminder();
        reminder.setUserId(userId);
        reminder.setPetId(petId);
        reminder.setPetName(petName);
        reminder.setReminderType(reminderType);
        reminder.setTitle(title);
        reminder.setRemindDate(remindDate);
        reminder.setNote(note);
        reminder.setIsDone(0);
        healthReminderMapper.insert(reminder);
        return reminder.getId();
    }

    @Override
    public void markDone(Long reminderId, Long userId) {
        HealthReminder reminder = healthReminderMapper.selectById(reminderId);
        if (reminder == null || !userId.equals(reminder.getUserId())) {
            throw new BusinessException(RespType.REMINDER_NOT_FOUND);
        }
        reminder.setIsDone(1);
        healthReminderMapper.updateById(reminder);
    }

    @Override
    public void delete(Long reminderId, Long userId) {
        HealthReminder reminder = healthReminderMapper.selectById(reminderId);
        if (reminder == null || !userId.equals(reminder.getUserId())) {
            throw new BusinessException(RespType.REMINDER_NOT_FOUND);
        }
        healthReminderMapper.deleteById(reminderId);
    }

    private HealthReminderVO toVO(HealthReminder r) {
        return HealthReminderVO.builder()
                .id(r.getId())
                .petId(r.getPetId())
                .petName(r.getPetName())
                .reminderType(r.getReminderType())
                .title(r.getTitle())
                .remindDate(r.getRemindDate())
                .isDone(r.getIsDone() != null && r.getIsDone() == 1)
                .note(r.getNote())
                .createTime(r.getCreateTime())
                .build();
    }
}
