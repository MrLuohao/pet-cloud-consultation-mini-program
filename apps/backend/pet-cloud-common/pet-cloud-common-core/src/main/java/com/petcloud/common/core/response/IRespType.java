package com.petcloud.common.core.response;

/**
 * 响应类型接口
 *
 * @author luohao
 */
public interface IRespType {
    /**
     * 是否成功
     */
    boolean isSuccess();

    /**
     * 获取响应码
     */
    String getCode();

    /**
     * 获取响应消息
     */
    String getMessage();
}
