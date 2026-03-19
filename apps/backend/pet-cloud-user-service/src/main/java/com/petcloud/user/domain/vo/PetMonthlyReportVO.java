package com.petcloud.user.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 宠物月度报告VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetMonthlyReportVO {

    private Long petId;

    private String petName;

    private Integer year;

    private Integer month;

    private Integer vaccineCount;

    private Integer checkupCount;

    private Integer medicineCount;

    private Integer surgeryCount;

    private Integer consultationCount;

    private Integer reminderDoneCount;

    private Integer totalEvents;
}
