package com.petcloud.common.core.response;

import cn.hutool.core.util.ArrayUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
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

    /**
     * 成功响应（无数据）
     * 适用于 void 返回类型的接口
     *
     * @param <T> 泛型类型
     * @return 成功响应
     */
    @SuppressWarnings("unchecked")
    public static <T> Response<T> succeed() {
        return (Response<T>) of(RespType.SUCCESS);
    }

    /**
     * 成功响应（带数据）
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 成功响应
     */
    public static <T> Response<T> succeed(T data) {
        return of(RespType.SUCCESS, data, null);
    }

    /**
     * 失败响应（通过 IRespType）
     *
     * @param respType 响应类型
     * @param args     格式化参数
     * @param <T>      泛型类型
     * @return 失败响应
     */
    public static <T> Response<T> error(IRespType respType, Object... args) {
        return of(respType, null, null, args);
    }

    /**
     * 创建基础响应
     *
     * @param type 响应类型
     * @param <T>  泛型类型
     * @return 响应对象
     */
    public static <T> Response<T> of(IRespType type) {
        return of(type, null, null);
    }

    /**
     * 创建自定义响应
     *
     * @param type       响应类型
     * @param msgPattern 自定义消息模板
     * @param argArray   消息参数
     * @param <T>        泛型类型
     * @return 响应对象
     */
    public static <T> Response<T> of(IRespType type, String msgPattern, Object... argArray) {
        return of(type, null, msgPattern, argArray);
    }

    /**
     * 创建自定义响应（带数据）
     *
     * @param type       响应类型
     * @param data       响应数据
     * @param msgPattern 自定义消息模板
     * @param argArray   消息参数
     * @param <T>        泛型类型
     * @return 响应对象
     */
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
