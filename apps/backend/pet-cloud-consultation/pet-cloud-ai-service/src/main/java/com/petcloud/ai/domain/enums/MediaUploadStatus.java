package com.petcloud.ai.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum MediaUploadStatus {
    UPLOADED("uploaded"),
    FAILED("failed");

    private final String code;

    public static MediaUploadStatus fromCode(String code) {
        return Arrays.stream(values())
                .filter(item -> item.code.equalsIgnoreCase(code))
                .findFirst()
                .orElse(UPLOADED);
    }
}
