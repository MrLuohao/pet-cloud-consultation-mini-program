package com.petcloud.user.infrastructure.feign;

import com.petcloud.common.core.response.Response;
import com.petcloud.user.domain.dto.MediaAssetQueryRequest;
import com.petcloud.user.domain.vo.DiagnosisMedicalRecordVO;
import com.petcloud.user.domain.vo.DiagnosisSummaryVO;
import com.petcloud.user.infrastructure.feign.dto.CreateDiagnosisTaskRequest;
import com.petcloud.user.infrastructure.feign.dto.DiagnosisTaskVO;
import com.petcloud.user.infrastructure.feign.dto.MediaAssetVO;
import com.petcloud.user.infrastructure.feign.dto.RegisterMediaAssetRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "ai-service", url = "${ai-service.url:http://localhost:8119}")
public interface AiServiceClient {

    @PostMapping("/internal/ai/diagnosis/tasks")
    Response<DiagnosisTaskVO> createDiagnosisTask(@RequestBody CreateDiagnosisTaskRequest request);

    @GetMapping("/internal/ai/tasks/{taskId}")
    Response<DiagnosisTaskVO> getDiagnosisTask(@PathVariable("taskId") Long taskId);

    @PostMapping("/internal/ai/media/assets/register")
    Response<MediaAssetVO> registerMediaAsset(@RequestBody RegisterMediaAssetRequest request);

    @PostMapping("/internal/ai/media/assets/query")
    Response<List<MediaAssetVO>> getMediaAssets(@RequestBody MediaAssetQueryRequest request);

    @GetMapping("/internal/ai/pets/{petId}/diagnosis-summary")
    Response<DiagnosisSummaryVO> getDiagnosisSummary(@PathVariable("petId") Long petId,
                                                    @RequestParam("userId") Long userId);

    @GetMapping("/internal/ai/pets/{petId}/medical-records")
    Response<List<DiagnosisMedicalRecordVO>> getMedicalRecords(@PathVariable("petId") Long petId,
                                                               @RequestParam("userId") Long userId,
                                                               @RequestParam("limit") Integer limit);
}
