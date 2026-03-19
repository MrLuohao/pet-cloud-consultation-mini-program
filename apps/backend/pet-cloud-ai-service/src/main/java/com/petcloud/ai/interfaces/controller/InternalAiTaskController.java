package com.petcloud.ai.interfaces.controller;

import com.petcloud.ai.domain.dto.CreateDiagnosisTaskRequest;
import com.petcloud.ai.domain.dto.MediaAssetQueryRequest;
import com.petcloud.ai.domain.dto.RegisterMediaAssetRequest;
import com.petcloud.ai.domain.service.AiTaskService;
import com.petcloud.ai.domain.vo.DiagnosisMedicalRecordVO;
import com.petcloud.ai.domain.vo.DiagnosisSummaryVO;
import com.petcloud.ai.domain.vo.DiagnosisTaskVO;
import com.petcloud.ai.domain.vo.MediaAssetVO;
import com.petcloud.common.core.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/ai")
public class InternalAiTaskController {

    private final AiTaskService aiTaskService;

    @PostMapping("/diagnosis/tasks")
    public Response<DiagnosisTaskVO> createDiagnosisTask(@RequestBody CreateDiagnosisTaskRequest request) {
        log.info("创建AI诊断任务, userId: {}, petId: {}", request.getUserId(), request.getPetId());
        return Response.succeed(aiTaskService.createDiagnosisTask(request));
    }

    @GetMapping("/tasks/{taskId}")
    public Response<DiagnosisTaskVO> getDiagnosisTask(@PathVariable Long taskId) {
        return Response.succeed(aiTaskService.getDiagnosisTask(taskId));
    }

    @PostMapping("/media/assets/register")
    public Response<MediaAssetVO> registerMediaAsset(@RequestBody RegisterMediaAssetRequest request) {
        return Response.succeed(aiTaskService.registerMediaAsset(request));
    }

    @PostMapping("/media/assets/query")
    public Response<List<MediaAssetVO>> getMediaAssets(@RequestBody MediaAssetQueryRequest request) {
        return Response.succeed(aiTaskService.getMediaAssets(request));
    }

    @GetMapping("/pets/{petId}/diagnosis-summary")
    public Response<DiagnosisSummaryVO> getDiagnosisSummary(@PathVariable Long petId,
                                                            @RequestParam(required = false) Long userId) {
        return Response.succeed(aiTaskService.getDiagnosisSummary(petId, userId));
    }

    @GetMapping("/pets/{petId}/medical-records")
    public Response<List<DiagnosisMedicalRecordVO>> getMedicalRecords(@PathVariable Long petId,
                                                                      @RequestParam(required = false) Long userId,
                                                                      @RequestParam(defaultValue = "10") Integer limit) {
        return Response.succeed(aiTaskService.getMedicalRecords(petId, userId, limit));
    }
}
