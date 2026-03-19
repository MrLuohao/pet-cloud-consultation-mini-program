package com.petcloud.user.domain.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 更新健康档案请求DTO
 *
 * @author luohao
 */
@Data
public class HealthRecordUpdateDTO {
    private Long id;
    private String recordType;
    private String title;
    private String content;
    private String hospitalName;
    private String doctorName;
    private LocalDate recordDate;
    private LocalDate nextDate;
    private String images;
}
