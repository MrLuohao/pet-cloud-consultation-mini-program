package com.petcloud.user.domain.service;

import com.petcloud.user.domain.dto.AgentApplicationParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 聊天服务接口
 *
 * @author luohao
 */
public interface ChatService {

    /**
     * 调用 Qwen Max 3 模型
     */
    String callWithMessageQwenMax3(String userMessage);

    /**
     * 调用 DeepSeek V3 模型
     */
    String callWithMessageDeepSeekV3(String userMessage);

    /**
     * 调用 DeepSeek V3 流式接口
     */
    void callWithMessageDeepSeekV3Streaming(String userMessage, SseEmitter emitter);

    /**
     * 调用 Agent 应用
     */
    String agentApplication(AgentApplicationParam param);
}
