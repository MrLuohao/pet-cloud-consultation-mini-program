package com.petcloud.user.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI宠物诊断DTO
 *
 * @author luohao
 */
@Data
public class AiDiagnosisDTO {

    /**
     * 宠物类型(1狗/2猫/3其他)
     */
    private Integer petType;

    /**
     * 宠物年龄（月）
     */
    private Integer petAge;

    /**
     * 症状描述
     */
    @NotBlank(message = "症状描述不能为空")
    private String symptoms;

    /**
     * 图片URL（可选）
     */
    private String imageUrl;
}
