package com.petcloud.user.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DiagnosisSubmitStatus {
    REJECTED("rejected");

    private final String code;
}
