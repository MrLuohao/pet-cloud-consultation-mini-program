package com.petcloud.user.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum DiagnosisTaskStatus {
    PROCESSING("processing"),
    COMPLETED("completed"),
    FAILED("failed"),
    NOT_FOUND("not_found"),
    REJECTED("rejected");

    private final String code;

    public static DiagnosisTaskStatus fromCode(String code) {
        return Arrays.stream(values())
                .filter(item -> item.code.equalsIgnoreCase(code))
                .findFirst()
                .orElse(PROCESSING);
    }
}
