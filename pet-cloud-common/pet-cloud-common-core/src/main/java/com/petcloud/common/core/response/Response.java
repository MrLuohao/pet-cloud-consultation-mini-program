package com.petcloud.common.core.response;

import cn.hutool.core.util.ArrayUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.petcloud.common.core.exception.BusinessException;
import com.petcloud.common.core.exception.RespType;
import com.petcloud.common.core.utils.NullSafeUtil;
import lombok.Data;
import org.slf4j.MDC;
import org.slf4j.helpers.MessageFormatter;

/**
 * 统一响应结果
 *
 * @author luohao
 */
@Data
public class Response<T> {
    private boolean status = false;
    private String code;
    private String msg;
    protected T data;
    private String traceId;

    @JsonIgnore
    public boolean isSuccess() {
        return status;
    }

    @JsonIgnore
    public boolean isFailure() {
        return !status;
    }

    public static Response<?> succeed() {
        return of(RespType.SUCCESS);
    }

    public static <T> Response<T> succeed(T data) {
        return of(RespType.SUCCESS, data, null);
    }

    public static <T> Response<T> error(BusinessException exception) {
        return of(exception.getRespType(), exception.getMessage());
    }

    public static <T> Response<T> error(IRespType respType, Object... argArray) {
        return of(respType, null, null, argArray);
    }

    public static <T> Response<T> of(IRespType type) {
        return of(type, null);
    }

    public static <T> Response<T> of(IRespType type, String msgPattern, Object... argArray) {
        return of(type, null, msgPattern, argArray);
    }

    public static <T> Response<T> of(IRespType type, T data, String msgPattern, Object... argArray) {
        String baseMsg = NullSafeUtil.string(msgPattern).orElse(type.getMessage());
        String finalMsg = ArrayUtil.isEmpty(argArray) ? baseMsg : MessageFormatter.arrayFormat(baseMsg, argArray).getMessage();

        Response<T> response = new Response<>();
        response.setCode(type.getCode());
        response.setMsg(finalMsg);
        response.setStatus(type.isSuccess());
        response.setData(data);
        response.setTraceId(MDC.get("EagleEye-TraceID"));
        return response;
    }

    @Override
    public String toString() {
        return "Response(status=" + status + ", code=" + code + ", msg=" + msg + ", data=" + data + ", traceId=" + traceId + ")";
    }
}
