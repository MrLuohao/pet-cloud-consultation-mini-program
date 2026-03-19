package com.petcloud.ai.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum AiTaskType {
    DIAGNOSIS_GENERATE("diagnosis_generate"),
    DIAGNOSIS_EXTRACT("diagnosis_extract"),
    MEDIA_MODERATION("media_moderation"),
    FEATURED_CONTENT_DRAFT_GENERATE("featured_content_draft_generate");

    private final String code;

    public static AiTaskType fromCode(String code) {
        return Arrays.stream(values())
                .filter(item -> item.code.equalsIgnoreCase(code))
                .findFirst()
                .orElse(DIAGNOSIS_GENERATE);
    }
}
