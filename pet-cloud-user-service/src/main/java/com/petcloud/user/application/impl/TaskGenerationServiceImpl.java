package com.petcloud.user.application.impl;

import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.petcloud.user.application.config.AliYunAiConfig;
import com.petcloud.user.domain.dto.ImageEditDTO;
import com.petcloud.user.domain.service.TaskGenerationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 任务生成应用服务实现类
 *
 * @author luohao
 */
@Slf4j
@Service
public class TaskGenerationServiceImpl implements TaskGenerationService {

    @Autowired
    private AliYunAiConfig aliyunAiConfig;

    @Override
    public String textToImage(String userMessage) {
        try {
            MultiModalConversation conv = new MultiModalConversation();
            MultiModalMessage message = MultiModalMessage.builder()
                    .role(Role.USER.getValue())
                    .content(List.of(
                            Collections.singletonMap("text", userMessage)
                    )).build();

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("watermark", false);
            parameters.put("prompt_extend", true);
            parameters.put("negative_prompt", "");
            parameters.put("size", "1328*1328");

            MultiModalConversationParam param = MultiModalConversationParam.builder()
                    .apiKey(aliyunAiConfig.getApiKey())
                    .model("qwen-image-plus")
                    .messages(Collections.singletonList(message))
                    .parameters(parameters)
                    .build();

            return conv.call(param).getOutput().getChoices().get(0).getMessage().getContent().get(0).get("image").toString();
        } catch (Exception e) {
            log.error("文生图失败", e);
            throw new RuntimeException("文生图失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<String> imageEdit(ImageEditDTO dto) {
        try {
            List<String> images = new ArrayList<>();
            MultiModalConversation conv = new MultiModalConversation();

            // 模型支持输入1-3张图片
            MultiModalMessage userMessage = MultiModalMessage.builder()
                    .role(Role.USER.getValue())
                    .content(Stream.concat(
                            dto.getImageUrls().stream()
                                    .map(url -> Collections.singletonMap("image", url)),
                            Stream.of(Collections.singletonMap("text", dto.getUserMessage()))
                    ).collect(Collectors.toList()))
                    .build();

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("watermark", false);
            parameters.put("negative_prompt", dto.getPrompt());
            parameters.put("n", dto.getPutSize());
            parameters.put("prompt_extend", true);

            if (dto.getPutSize() == 1 && dto.getSize() != null) {
                parameters.put("size", dto.getSize());
            }

            MultiModalConversationParam param = MultiModalConversationParam.builder()
                    .apiKey(aliyunAiConfig.getApiKey())
                    .model(dto.getModel())
                    .messages(Collections.singletonList(userMessage))
                    .parameters(parameters)
                    .build();

            MultiModalConversationResult result = conv.call(param);
            List<Map<String, Object>> contentList = result.getOutput().getChoices().get(0).getMessage().getContent();

            for (Map<String, Object> content : contentList) {
                if (content.containsKey("image")) {
                    String image = content.get("image").toString();
                    images.add(image);
                }
            }
            return images;
        } catch (Exception e) {
            log.error("图片编辑失败", e);
            throw new RuntimeException("图片编辑失败: " + e.getMessage(), e);
        }
    }

    @Override
    public ImageSynthesisResult textToImageV2(String userMessage) {
        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("prompt_extend", true);
            parameters.put("watermark", false);
            parameters.put("seed", new Random().nextInt(10000));

            ImageSynthesisParam param = ImageSynthesisParam.builder()
                    .apiKey(aliyunAiConfig.getApiKey())
                    .model("wan2.5-t2i-preview")
                    .prompt(userMessage)
                    .n(1)
                    .size("1024*1024")
                    .negativePrompt("")
                    .parameters(parameters)
                    .build();

            ImageSynthesis imageSynthesis = new ImageSynthesis();
            return imageSynthesis.call(param);
        } catch (ApiException | NoApiKeyException e) {
            log.error("文生图V2失败", e);
            throw new RuntimeException("文生图V2失败: " + e.getMessage(), e);
        }
    }
}
