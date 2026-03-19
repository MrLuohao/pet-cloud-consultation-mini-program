package com.petcloud.ai.domain.enums;

import com.petcloud.common.core.response.IRespType;
import lombok.Getter;

@Getter
public enum AiRespType implements IRespType {
    AI_TASK_NOT_FOUND(false, "42000001", "AI任务不存在"),
    AI_MEDIA_TYPE_UNSUPPORTED(false, "42000002", "不支持的媒体类型: {}");

    private final boolean success;
    private final String code;
    private final String message;

    AiRespType(boolean success, String code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }
}
