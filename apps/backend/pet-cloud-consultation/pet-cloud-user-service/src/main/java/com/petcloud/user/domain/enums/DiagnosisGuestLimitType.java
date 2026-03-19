package com.petcloud.user.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DiagnosisGuestLimitType {
    AI_DIAGNOSIS("ai_diagnosis");

    private final String code;
}
