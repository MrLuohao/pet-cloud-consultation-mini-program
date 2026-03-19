package com.petcloud.ai.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum AiTaskStatus {
    PROCESSING("processing"),
    COMPLETED("completed"),
    FAILED("failed"),
    NOT_FOUND("not_found");

    private final String code;

    public static AiTaskStatus fromCode(String code) {
        return Arrays.stream(values())
                .filter(item -> item.code.equalsIgnoreCase(code))
                .findFirst()
                .orElse(PROCESSING);
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == NOT_FOUND;
    }
}
