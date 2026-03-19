package com.petcloud.user.domain.service;

import com.petcloud.user.domain.dto.ImageEditDTO;

import java.util.List;

/**
 * 任务生成服务接口
 *
 * @author luohao
 */
public interface TaskGenerationService {

    /**
     * 文本生成图片 V1
     */
    String textToImage(String userMessage);

    /**
     * 文本生成图片 V2
     */
    com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult textToImageV2(String userMessage);

    /**
     * 图片编辑
     */
    List<String> imageEdit(ImageEditDTO dto);
}
