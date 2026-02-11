package com.petcloud.user.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

/**
 * 健康提醒VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthReminderVO {

    private Long id;

    private Long petId;

    private String petName;

    private String reminderType;

    private String title;

    private LocalDate remindDate;

    private Boolean isDone;

    private String note;

    private Date createTime;
}
