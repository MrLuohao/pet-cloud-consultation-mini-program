package com.petcloud.common.core.exception;

import com.petcloud.common.core.response.IRespType;
import lombok.Getter;

/**
 * 响应类型枚举
 *
 * @author luohao
 */
@Getter
public enum RespType implements IRespType {
    SUCCESS(true, "00000000", "成功"),
    FAILURE(false, "99999999", "系统错误, 请联系管理员!"),
    BUSINESS_ERROR(false, "99990000", "业务执行异常"),
    API_CALL_ERROR(false, "99990001", "内部Api调用异常"),
    REQUEST_ERROR(false, "99990002", "请求方式错误"),
    PARAMETER_ERROR(false, "99990003", "参数校验失败"),
    DATABASE_ERROR(false, "99990004", "数据库执行异常"),
    JSON_ERROR(false, "99990005", "JSON转换异常"),
    ALI_AI_TEXT_TO_IMAGE_ERROR(false, "99990006", "通义千问-文生图异常{}"),
    ALI_AI_IMAGE_EDIT_ERROR(false, "99990007", "通义千问-图像编辑失败{}");

    private final boolean success;
    private final String code;
    private final String message;

    RespType(boolean success, String code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }
}
