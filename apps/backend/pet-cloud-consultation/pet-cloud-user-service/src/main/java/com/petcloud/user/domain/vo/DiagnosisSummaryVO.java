package com.petcloud.user.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Builder
public class DiagnosisSummaryVO {

    private Long petId;
    private Boolean available;
    private Integer recentDiagnosisCount;
    private List<String> recentSymptoms;
    private List<String> suggestedFocus;
    private Date lastDiagnosisTime;
}
