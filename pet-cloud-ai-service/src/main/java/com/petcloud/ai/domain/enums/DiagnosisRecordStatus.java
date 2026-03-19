package com.petcloud.ai.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DiagnosisRecordStatus {
    OBSERVING("observing");

    private final String code;
}
