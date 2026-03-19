package com.petcloud.ai.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DiagnosisRiskLevel {
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high");

    private final String code;

    public boolean requiresDoctor() {
        return this == MEDIUM || this == HIGH;
    }
}
