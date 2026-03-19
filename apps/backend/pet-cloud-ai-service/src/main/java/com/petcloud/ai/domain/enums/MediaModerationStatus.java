package com.petcloud.ai.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum MediaModerationStatus {
    PASS("pass"),
    REVIEW("review"),
    REJECT("reject");

    private final String code;

    public static MediaModerationStatus fromCode(String code) {
        return Arrays.stream(values())
                .filter(item -> item.code.equalsIgnoreCase(code))
                .findFirst()
                .orElse(REVIEW);
    }

    public boolean isAllowedForBizSubmission() {
        return this == PASS;
    }
}
