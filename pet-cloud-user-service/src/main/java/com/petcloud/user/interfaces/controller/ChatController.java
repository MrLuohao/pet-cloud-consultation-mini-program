package com.petcloud.user.interfaces.controller;

import com.petcloud.user.domain.dto.AgentApplicationParam;
import com.petcloud.user.domain.service.ChatService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * 聊天控制器
 *
 * @author luohao
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin
public class ChatController {

    private final ChatService chatService;

    /**
     * 聊天 qwen3Max
     */
    @PostMapping("/qwen3Max")
    public String chatWithQwen3Max(@RequestBody String userMessage) {
        try {
            return chatService.callWithMessageQwenMax3(userMessage);
        } catch (Exception e) {
            log.error("QwenMax3聊天失败", e);
            return "聊天接口异常：" + e.getMessage();
        }
    }

    /**
     * 聊天 DeepSeekV3
     */
    @PostMapping("/deepSeekV3")
    public String chatWithDeepSeekV3(@RequestBody String userMessage) {
        try {
            return chatService.callWithMessageDeepSeekV3(userMessage);
        } catch (Exception e) {
            log.error("DeepSeekV3聊天失败", e);
            return "聊天接口异常：" + e.getMessage();
        }
    }

    /**
     * 聊天 DeepSeekV3 - 流式接口
     */
    @PostMapping(value = "/deepSeekV3/streaming", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter callWithMessageDeepSeekV3Streaming(@RequestBody String userMessage,
                                                         HttpServletResponse response) {
        // 设置响应头
        response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        // 禁用Nginx缓冲
        response.setHeader("X-Accel-Buffering", "no");

        // 创建 SseEmitter，设置超时时间
        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L);

        // 异步处理流式响应
        CompletableFuture.runAsync(() -> {
            try {
                chatService.callWithMessageDeepSeekV3Streaming(userMessage, emitter);
            } catch (Exception e) {
                log.error("流式处理异常", e);
                try {
                    emitter.send(SseEmitter.event()
                            .data("【系统错误】: " + e.getMessage())
                            .name("error")
                            .id("error"));
                    emitter.completeWithError(e);
                } catch (Exception ex) {
                    log.error("发送错误信息失败", ex);
                }
            }
        });

        // 设置完成和超时回调
        emitter.onCompletion(() -> log.info("SSE连接完成"));
        emitter.onTimeout(() -> {
            log.warn("SSE连接超时");
            emitter.complete();
        });
        emitter.onError((ex) -> log.error("SSE连接错误", ex));

        return emitter;
    }

    /**
     * 访问Agent应用
     */
    @PostMapping("/agent")
    public String agentApplication(@RequestBody @Valid AgentApplicationParam param) {
        try {
            return chatService.agentApplication(param);
        } catch (Exception e) {
            log.error("Agent应用调用失败", e);
            return "访问agent应用失败：" + e.getMessage();
        }
    }
}
