package com.petcloud.ai.domain.service;

import com.petcloud.ai.domain.dto.CreateDiagnosisTaskRequest;
import com.petcloud.ai.domain.dto.MediaAssetQueryRequest;
import com.petcloud.ai.domain.dto.RegisterMediaAssetRequest;
import com.petcloud.ai.domain.vo.DiagnosisMedicalRecordVO;
import com.petcloud.ai.domain.vo.DiagnosisSummaryVO;
import com.petcloud.ai.domain.vo.DiagnosisTaskVO;
import com.petcloud.ai.domain.vo.MediaAssetVO;
import java.util.List;

public interface AiTaskService {

    DiagnosisTaskVO createDiagnosisTask(CreateDiagnosisTaskRequest request);

    DiagnosisTaskVO getDiagnosisTask(Long taskId);

    MediaAssetVO registerMediaAsset(RegisterMediaAssetRequest request);

    List<MediaAssetVO> getMediaAssets(MediaAssetQueryRequest request);

    DiagnosisSummaryVO getDiagnosisSummary(Long petId, Long userId);

    List<DiagnosisMedicalRecordVO> getMedicalRecords(Long petId, Long userId, Integer limit);
}
