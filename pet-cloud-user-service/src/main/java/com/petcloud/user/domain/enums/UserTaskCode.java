package com.petcloud.user.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserTaskCode {
    AI_DIAGNOSIS("AI_DIAGNOSIS");

    private final String code;
}
