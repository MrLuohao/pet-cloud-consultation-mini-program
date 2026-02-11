package com.petcloud.user.application.impl;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.app.Application;
import com.alibaba.dashscope.app.ApplicationParam;
import com.alibaba.dashscope.app.ApplicationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.petcloud.user.application.config.AliYunAiConfig;
import com.petcloud.user.domain.dto.AgentApplicationDTO;
import com.petcloud.user.domain.service.ChatService;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 聊天应用服务实现类
 *
 * @author luohao
 */
@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AliYunAiConfig aliyunAiConfig;

    @Override
    public String callWithMessageQwenMax3(String content) {
        try {
            Generation gen = new Generation();
            Message systemMsg = Message.builder()
                    .role(Role.SYSTEM.getValue())
                    .content("You are a helpful assistant.")
                    .build();
            Message userMsg = Message.builder()
                    .role(Role.USER.getValue())
                    .content(content)
                    .build();
            GenerationParam param = GenerationParam.builder()
                    .apiKey(aliyunAiConfig.getApiKey())
                    .model(Generation.Models.QWEN_MAX)
                    .messages(Arrays.asList(systemMsg, userMsg))
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                    .build();
            GenerationResult result = gen.call(param);
            log.info("QwenMax3对话:【input】: {} \n【output】: {}", content, JsonUtils.toJson(result));
            return result.getOutput().getChoices().get(0).getMessage().getContent();
        } catch (Exception e) {
            log.error("QwenMax3调用失败", e);
            throw new RuntimeException("QwenMax3调用失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String callWithMessageDeepSeekV3(String content) {
        try {
            Generation gen = new Generation();
            Message systemMsg = Message.builder()
                    .role(Role.SYSTEM.getValue())
                    .content("You are a helpful assistant.")
                    .build();
            Message userMsg = Message.builder()
                    .role(Role.USER.getValue())
                    .content(content)
                    .build();
            GenerationParam param = GenerationParam.builder()
                    .apiKey(aliyunAiConfig.getApiKey())
                    .model("deepseek-v3.2")
                    .messages(Arrays.asList(systemMsg, userMsg))
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                    .build();
            GenerationResult result = gen.call(param);
            log.info("DeepSeekV3对话:【input】: {} \n【output】: {}", content, JsonUtils.toJson(result));
            return result.getOutput().getChoices().get(0).getMessage().getContent();
        } catch (Exception e) {
            log.error("DeepSeekV3调用失败", e);
            throw new RuntimeException("DeepSeekV3调用失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void callWithMessageDeepSeekV3Streaming(String input, SseEmitter emitter) {
        Generation gen = new Generation();

        GenerationParam param = GenerationParam.builder()
                .apiKey(aliyunAiConfig.getApiKey())
                .model("qwen-plus")
                .messages(Collections.singletonList(
                        Message.builder()
                                .role(Role.USER.getValue())
                                .content(input)
                                .build()
                ))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .incrementalOutput(true)
                .build();

        String[] requestIdHolder = new String[]{""};
        StringBuilder fullContent = new StringBuilder();
        Disposable[] disposableHolder = new Disposable[1];

        try {
            Flowable<GenerationResult> result = gen.streamCall(param);

            Disposable disposable = result
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.computation())
                    .subscribe(
                            message -> {
                                try {
                                    String content = message.getOutput().getChoices().get(0).getMessage().getContent();
                                    String finishReason = message.getOutput().getChoices().get(0).getFinishReason();

                                    if (content != null) {
                                        fullContent.append(content);
                                    }

                                    Map<String, Object> data = new HashMap<>();
                                    data.put("content", content);
                                    data.put("requestId", message.getRequestId());

                                    if (requestIdHolder[0].isEmpty() && message.getRequestId() != null) {
                                        requestIdHolder[0] = message.getRequestId();
                                    }

                                    if (finishReason != null && !"null".equals(finishReason)) {
                                        Map<String, Object> usageData = new HashMap<>();
                                        usageData.put("type", "complete");
                                        usageData.put("requestId", message.getRequestId());
                                        usageData.put("inputTokens", message.getUsage().getInputTokens());
                                        usageData.put("outputTokens", message.getUsage().getOutputTokens());
                                        usageData.put("fullContent", fullContent.toString());

                                        String jsonData = objectMapper.writeValueAsString(usageData);
                                        emitter.send(SseEmitter.event()
                                                .data(jsonData)
                                                .name("complete"));
                                    } else {
                                        String jsonData = objectMapper.writeValueAsString(data);
                                        emitter.send(SseEmitter.event()
                                                .data(jsonData)
                                                .name("message"));
                                    }
                                } catch (Exception e) {
                                    log.error("处理流式响应异常", e);
                                    try {
                                        Map<String, Object> errorData = new HashMap<>();
                                        errorData.put("type", "error");
                                        errorData.put("message", e.getMessage());

                                        String jsonData = objectMapper.writeValueAsString(errorData);
                                        emitter.send(SseEmitter.event()
                                                .data(jsonData)
                                                .name("error"));
                                    } catch (IOException ioException) {
                                        log.error("发送错误信息失败", ioException);
                                    }
                                }
                            },
                            error -> {
                                log.error("流式请求失败", error);
                                try {
                                    Map<String, Object> errorData = new HashMap<>();
                                    errorData.put("type", "error");
                                    errorData.put("message", error.getMessage());
                                    errorData.put("fullContent", fullContent.toString());

                                    String jsonData = objectMapper.writeValueAsString(errorData);
                                    emitter.send(SseEmitter.event()
                                            .data(jsonData)
                                            .name("error"));
                                    emitter.completeWithError(error);
                                } catch (IOException e) {
                                    log.error("发送错误信息失败", e);
                                } finally {
                                    if (disposableHolder[0] != null && !disposableHolder[0].isDisposed()) {
                                        disposableHolder[0].dispose();
                                    }
                                }
                            },
                            () -> {
                                try {
                                    Map<String, Object> completeData = new HashMap<>();
                                    completeData.put("type", "finished");
                                    completeData.put("message", "Stream completed");
                                    completeData.put("fullContent", fullContent.toString());

                                    if (!requestIdHolder[0].isEmpty()) {
                                        completeData.put("requestId", requestIdHolder[0]);
                                    }

                                    String jsonData = objectMapper.writeValueAsString(completeData);
                                    emitter.send(SseEmitter.event()
                                            .data(jsonData)
                                            .name("finished"));
                                    emitter.complete();

                                    log.info("流式响应完成，完整内容长度: {}, requestId: {}",
                                            fullContent.length(), requestIdHolder[0]);
                                } catch (IOException e) {
                                    log.error("发送完成信息失败", e);
                                } finally {
                                    if (disposableHolder[0] != null && !disposableHolder[0].isDisposed()) {
                                        disposableHolder[0].dispose();
                                    }
                                }
                            }
                    );

            disposableHolder[0] = disposable;

            emitter.onCompletion(() -> {
                log.info("SSE连接被客户端关闭");
                if (disposableHolder[0] != null && !disposableHolder[0].isDisposed()) {
                    disposableHolder[0].dispose();
                }
            });

            emitter.onTimeout(() -> {
                log.warn("SSE连接超时");
                if (disposableHolder[0] != null && !disposableHolder[0].isDisposed()) {
                    disposableHolder[0].dispose();
                }
            });

        } catch (Exception e) {
            log.error("发起流式请求异常", e);
            try {
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("type", "error");
                errorData.put("message", e.getMessage());

                String jsonData = objectMapper.writeValueAsString(errorData);
                emitter.send(SseEmitter.event()
                        .data(jsonData)
                        .name("error"));
                emitter.completeWithError(e);
            } catch (IOException ioException) {
                log.error("发送错误信息失败", ioException);
            } finally {
                if (disposableHolder[0] != null && !disposableHolder[0].isDisposed()) {
                    disposableHolder[0].dispose();
                }
            }
        }
    }

    @Override
    public String agentApplication(AgentApplicationDTO param) {
        try {
            ApplicationParam requestParam = ApplicationParam.builder()
                    .apiKey(aliyunAiConfig.getApiKey())
                    .appId(param.getAppId())
                    .prompt(param.getUserMessage())
                    .bizParams(new Gson().toJsonTree(param.getBizParams()).getAsJsonObject())
                    .build();

            Application application = new Application();
            ApplicationResult result = application.call(requestParam);
            return result.getOutput().getText();
        } catch (Exception e) {
            log.error("Agent应用调用失败", e);
            throw new RuntimeException("Agent应用调用失败: " + e.getMessage(), e);
        }
    }
}
