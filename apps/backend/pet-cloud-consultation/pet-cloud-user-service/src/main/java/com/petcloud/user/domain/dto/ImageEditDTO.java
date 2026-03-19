package com.petcloud.user.domain.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 图片编辑请求DTO
 *
 * @author luohao
 */
@Data
public class ImageEditDTO {

    /**
     * 图片url 支持1-3张图片
     */
    @NotNull(message = "图片url不能为空")
    private List<Object> imageUrls;

    /**
     * 用户消息
     */
    @NotNull(message = "用户消息不能为空")
    private Object userMessage;

    /**
     * 提示词
     */
    @NotEmpty(message = "提示词不能为空")
    private String prompt;

    /**
     * 输出张数 1-6
     */
    @NotNull(message = "输出张数不能为空")
    private Integer putSize;

    /**
     * 输出图片大小 当且仅当putSize为1时生效
     * 256x256 | 512x512 | 1024x1024 | 1024*2048
     */
    private String size;

    /**
     * 模型
     * qwen-image-edit-plus | qwen-image-plus | wan2.5-t2i-preview
     */
    @NotEmpty(message = "模型不能为空")
    private String model;
}
