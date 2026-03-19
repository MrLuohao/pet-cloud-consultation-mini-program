package com.petcloud.ai.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AiBizType {
    DIAGNOSIS("diagnosis");

    private final String code;
}
