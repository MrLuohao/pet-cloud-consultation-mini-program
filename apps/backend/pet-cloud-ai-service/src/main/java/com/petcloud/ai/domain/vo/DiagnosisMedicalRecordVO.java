package com.petcloud.ai.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Builder
public class DiagnosisMedicalRecordVO {

    private Long recordId;
    private Long taskId;
    private Long petId;
    private Date diagnosisTime;
    private String riskLevel;
    private String summary;
    private Integer shouldConsultDoctor;
    private List<String> primarySymptoms;
    private String severity;
    private List<String> suspectedIssues;
    private List<String> affectedParts;
    private List<String> followUpFocus;
}
