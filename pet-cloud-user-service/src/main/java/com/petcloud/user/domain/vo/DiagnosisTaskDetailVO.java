package com.petcloud.user.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class DiagnosisTaskDetailVO {

    private Long taskId;
    private String taskNo;
    private String status;
    private String riskLevel;
    private String summary;
    private List<String> possibleCauses;
    private List<String> careSuggestions;
    private List<String> nextActions;
    private List<Map<String, String>> observationTable;
    private KeyInfoVO keyInfo;

    @Data
    @Builder
    public static class KeyInfoVO {
        private List<String> primarySymptoms;
        private String duration;
        private String severity;
        private List<String> suspectedIssues;
        private List<String> affectedParts;
        private List<String> followUpFocus;
    }
}
