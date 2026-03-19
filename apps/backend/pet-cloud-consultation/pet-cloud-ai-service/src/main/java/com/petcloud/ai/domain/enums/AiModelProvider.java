package com.petcloud.ai.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AiModelProvider {
    DASHSCOPE("dashscope", "qwen-max"),
    RULE("rule", "rule-fallback");

    private final String code;
    private final String defaultModelName;
}
