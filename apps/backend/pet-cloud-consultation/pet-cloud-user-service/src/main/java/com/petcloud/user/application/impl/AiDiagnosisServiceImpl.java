package com.petcloud.user.application.impl;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.petcloud.common.core.exception.BusinessException;
import com.petcloud.common.core.exception.RespType;
import com.petcloud.user.application.config.AliYunAiConfig;
import com.petcloud.user.domain.dto.AiDiagnosisDTO;
import com.petcloud.user.domain.service.AiDiagnosisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * AI诊断服务实现类
 *
 * @author luohao
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiDiagnosisServiceImpl implements AiDiagnosisService {

    private final AliYunAiConfig aliyunAiConfig;

    @Override
    public String diagnose(AiDiagnosisDTO diagnosisDTO) {
        try {
            // 构建宠物类型描述
            String petTypeDesc = getPetTypeDescription(diagnosisDTO.getPetType());

            // 构建AI诊断prompt
            String systemPrompt = "你是一位专业的宠物健康顾问。请根据用户描述的宠物症状，" +
                    "提供初步的健康分析和建议。注意：这只是初步建议，不能替代专业兽医的诊断。" +
                    "如果症状严重，请建议用户尽快就医。" +
                    "请用友好、专业的语气回答，结构化地组织内容。";

            String userPrompt = String.format(
                    "宠物信息：\n" +
                            "- 类型：%s\n" +
                            "- 年龄：%d个月\n" +
                            "- 症状描述：%s\n\n" +
                            "请提供健康分析和建议。",
                    petTypeDesc,
                    diagnosisDTO.getPetAge() != null ? diagnosisDTO.getPetAge() : 0,
                    diagnosisDTO.getSymptoms()
            );

            Generation gen = new Generation();
            Message systemMsg = Message.builder()
                    .role(Role.SYSTEM.getValue())
                    .content(systemPrompt)
                    .build();
            Message userMsg = Message.builder()
                    .role(Role.USER.getValue())
                    .content(userPrompt)
                    .build();

            GenerationParam param = GenerationParam.builder()
                    .apiKey(aliyunAiConfig.getApiKey())
                    .model(Generation.Models.QWEN_MAX)
                    .messages(Arrays.asList(systemMsg, userMsg))
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                    .build();

            GenerationResult result = gen.call(param);
            String response = result.getOutput().getChoices().get(0).getMessage().getContent();

            log.info("AI诊断请求成功，症状: {}", diagnosisDTO.getSymptoms());
            return response;

        } catch (Exception e) {
            log.error("AI诊断调用失败", e);
            throw new BusinessException(RespType.AI_DIAGNOSIS_ERROR);
        }
    }

    /**
     * 获取宠物类型描述
     */
    private String getPetTypeDescription(Integer petType) {
        if (petType == null) {
            return "未知";
        }
        return switch (petType) {
            case 1 -> "狗";
            case 2 -> "猫";
            case 3 -> "其他";
            default -> "未知";
        };
    }
}
