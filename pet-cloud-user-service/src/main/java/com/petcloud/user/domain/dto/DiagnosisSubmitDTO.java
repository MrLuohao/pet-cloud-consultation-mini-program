package com.petcloud.user.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class DiagnosisSubmitDTO {

    private Long petId;

    private Integer petType;

    private Integer petAgeMonths;

    private List<String> symptomTags;

    @NotBlank(message = "症状描述不能为空")
    private String symptomDescription;

    private List<Long> mediaAssetIds;
}
