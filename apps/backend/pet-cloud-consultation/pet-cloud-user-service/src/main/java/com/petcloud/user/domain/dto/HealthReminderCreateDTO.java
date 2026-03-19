package com.petcloud.user.domain.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 健康提醒创建DTO
 */
@Data
public class HealthReminderCreateDTO {

    private Long petId;

    private String petName;

    private String reminderType;

    private String title;

    private LocalDate remindDate;

    private String note;
}
