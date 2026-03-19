package com.petcloud.user.infrastructure.feign.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateDiagnosisTaskRequest {

    private Long userId;
    private Long petId;
    private String guestDeviceHash;
    private Integer petType;
    private Integer petAgeMonths;
    private List<String> symptomTags;
    private String symptomDescription;
    private List<Long> mediaAssetIds;
}
