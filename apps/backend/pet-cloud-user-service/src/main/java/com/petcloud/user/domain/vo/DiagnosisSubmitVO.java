package com.petcloud.user.domain.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiagnosisSubmitVO {

    private Long taskId;
    private String taskNo;
    private String status;
    private Integer remainingCount;
    private Boolean limitReached;
}
