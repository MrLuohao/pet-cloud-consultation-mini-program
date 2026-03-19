package com.petcloud.user.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@Builder
public class DiagnosisEntryVO {

    private Boolean loggedIn;
    private Integer remainingCount;
    private List<UserPetVO> pets;
    private Long defaultPetId;
    private ArchiveSummaryVO archiveSummary;

    @Data
    @Builder
    public static class ArchiveSummaryVO {
        private Boolean available;
        private Long petId;
        private Integer recentDiagnosisCount;
        private List<String> recentSymptoms;
        private List<String> suggestedFocus;
        private Date lastDiagnosisTime;
        private String note;
    }
}
