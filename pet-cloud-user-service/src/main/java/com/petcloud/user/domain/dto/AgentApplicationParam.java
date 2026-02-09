package com.petcloud.user.domain.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Agent应用请求DTO
 *
 * @author luohao
 */
@Data
public class AgentApplicationParam {

    /**
     * 用户消息
     */
    @NotEmpty(message = "用户消息不能为空")
    private String userMessage;

    /**
     * 应用ID【Agent应用appId】
     */
    @NotEmpty(message = "应用ID不能为空")
    private String appId;

    /**
     * 业务参数【自定义变量名+类型】
     * 适用场景：工作流调用
     */
    private Map<String, Object> bizParams;
}
