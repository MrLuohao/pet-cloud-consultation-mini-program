package com.petcloud.user.interfaces.controller.task;

import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.petcloud.common.core.exception.RespType;
import com.petcloud.common.core.response.Response;
import com.petcloud.user.domain.dto.ImageEditDTO;
import com.petcloud.user.domain.service.TaskGenerationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 任务生成控制器
 *
 * @author luohao
 */
@Slf4j
@RestController
@RequestMapping("/api/image")
@RequiredArgsConstructor
public class TaskGenerationController {

    private final TaskGenerationService taskGenerationService;

    /**
     * 通义千问-文生图
     */
    @PostMapping("/text")
    public String textToImage(@RequestBody String userMessage) {
        try {
            return taskGenerationService.textToImage(userMessage);
        } catch (Exception e) {
            log.error("文生图失败", e);
            return "通义千问-文生图失败，请稍后重试";
        }
    }

    /**
     * 通义千问-文生图 V2
     */
    @PostMapping("/textV2")
    public Response<ImageSynthesisResult> textToImageV2(@RequestBody String userMessage) {
        try {
            return Response.succeed(taskGenerationService.textToImageV2(userMessage));
        } catch (Exception e) {
            log.error("文生图V2失败", e);
            return Response.error(RespType.ALI_AI_TEXT_TO_IMAGE_ERROR);
        }
    }

    /**
     * 通义千问-图像编辑
     */
    @PostMapping("/imageEdit")
    public Response<List<String>> imageEdit(@RequestBody @Valid ImageEditDTO dto) {
        try {
            return Response.succeed(taskGenerationService.imageEdit(dto));
        } catch (Exception e) {
            log.error("图像编辑失败", e);
            return Response.error(RespType.ALI_AI_IMAGE_EDIT_ERROR);
        }
    }
}
