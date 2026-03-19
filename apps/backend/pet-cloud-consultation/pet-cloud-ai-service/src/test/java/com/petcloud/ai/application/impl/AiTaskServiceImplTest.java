package com.petcloud.ai.application.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petcloud.ai.application.config.AliYunAiConfig;
import com.petcloud.ai.domain.dto.CreateDiagnosisTaskRequest;
import com.petcloud.ai.domain.dto.RegisterMediaAssetRequest;
import com.petcloud.ai.domain.entity.AiTask;
import com.petcloud.ai.domain.entity.DiagnosisExtractedInfo;
import com.petcloud.ai.domain.entity.DiagnosisRecord;
import com.petcloud.ai.domain.entity.MediaAsset;
import com.petcloud.ai.domain.enums.AiTaskStatus;
import com.petcloud.ai.domain.enums.MediaModerationStatus;
import com.petcloud.ai.domain.vo.DiagnosisMedicalRecordVO;
import com.petcloud.ai.domain.vo.DiagnosisSummaryVO;
import com.petcloud.ai.domain.vo.DiagnosisTaskVO;
import com.petcloud.ai.domain.vo.MediaAssetVO;
import com.petcloud.ai.infrastructure.persistence.mapper.AiTaskMapper;
import com.petcloud.ai.infrastructure.persistence.mapper.DiagnosisExtractedInfoMapper;
import com.petcloud.ai.infrastructure.persistence.mapper.DiagnosisRecordMapper;
import com.petcloud.ai.infrastructure.persistence.mapper.MediaAssetMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiTaskServiceImplTest {

    @Mock
    private AiTaskMapper aiTaskMapper;

    @Mock
    private DiagnosisRecordMapper diagnosisRecordMapper;

    @Mock
    private DiagnosisExtractedInfoMapper diagnosisExtractedInfoMapper;

    @Mock
    private MediaAssetMapper mediaAssetMapper;

    @Mock
    private AliYunAiConfig aliYunAiConfig;

    @InjectMocks
    private AiTaskServiceImpl aiTaskService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(aiTaskService, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(aiTaskService, "promptVersion", "v-test");
    }

    @Test
    void shouldCreateDiagnosisTaskAndPersistCompletedState() {
        when(aliYunAiConfig.getApiKey()).thenReturn("test-key");
        doAnswer(invocation -> {
            AiTask task = invocation.getArgument(0);
            task.setId(1001L);
            return 1;
        }).when(aiTaskMapper).insert(any(AiTask.class));
        doAnswer(invocation -> {
            DiagnosisRecord record = invocation.getArgument(0);
            record.setId(2002L);
            return 1;
        }).when(diagnosisRecordMapper).insert(any(DiagnosisRecord.class));

        CreateDiagnosisTaskRequest request = new CreateDiagnosisTaskRequest();
        request.setUserId(10L);
        request.setPetId(20L);
        request.setGuestDeviceHash("device-a");
        request.setSymptomTags(List.of("呕吐"));
        request.setSymptomDescription("今天开始出现呕吐和精神不好");

        DiagnosisTaskVO result = aiTaskService.createDiagnosisTask(request);

        assertEquals(AiTaskStatus.COMPLETED.getCode(), result.getStatus());
        assertEquals(1001L, result.getTaskId());
        assertNotNull(result.getKeyInfo());

        ArgumentCaptor<AiTask> updateCaptor = ArgumentCaptor.forClass(AiTask.class);
        verify(aiTaskMapper).updateById(updateCaptor.capture());
        assertEquals(AiTaskStatus.COMPLETED.getCode(), updateCaptor.getValue().getStatus());
        assertEquals(2002L, updateCaptor.getValue().getBizId());
    }

    @Test
    void shouldMarkTaskFailedWhenRecordPersistenceThrows() {
        when(aliYunAiConfig.getApiKey()).thenReturn("test-key");
        doAnswer(invocation -> {
            AiTask task = invocation.getArgument(0);
            task.setId(1001L);
            return 1;
        }).when(aiTaskMapper).insert(any(AiTask.class));
        doAnswer(invocation -> {
            throw new IllegalStateException("record insert failed");
        }).when(diagnosisRecordMapper).insert(any(DiagnosisRecord.class));

        CreateDiagnosisTaskRequest request = new CreateDiagnosisTaskRequest();
        request.setPetId(20L);
        request.setSymptomDescription("持续呕吐");

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> aiTaskService.createDiagnosisTask(request));
        assertEquals("record insert failed", exception.getMessage());

        ArgumentCaptor<AiTask> updateCaptor = ArgumentCaptor.forClass(AiTask.class);
        verify(aiTaskMapper).updateById(updateCaptor.capture());
        assertEquals(AiTaskStatus.FAILED.getCode(), updateCaptor.getValue().getStatus());
        assertEquals("record insert failed", updateCaptor.getValue().getErrorMessage());
    }

    @Test
    void shouldReturnReviewStatusForUnclearMedia() {
        doAnswer(invocation -> {
            MediaAsset asset = invocation.getArgument(0);
            asset.setId(3003L);
            return 1;
        }).when(mediaAssetMapper).insert(any(MediaAsset.class));

        RegisterMediaAssetRequest request = new RegisterMediaAssetRequest();
        request.setMediaType("image");
        request.setOriginalFilename("unclear_scan.jpg");
        request.setUrl("https://cdn.test/unclear_scan.jpg");

        MediaAssetVO result = aiTaskService.registerMediaAsset(request);

        assertEquals(3003L, result.getAssetId());
        assertEquals(MediaModerationStatus.REVIEW.getCode(), result.getModerationStatus());
        assertEquals(Boolean.FALSE, result.getAvailableForSubmit());
        assertEquals("素材已上传，审核中，请稍后再试", result.getReason());
    }

    @Test
    void shouldReturnPassStatusForSafeMedia() {
        doAnswer(invocation -> {
            MediaAsset asset = invocation.getArgument(0);
            asset.setId(3004L);
            return 1;
        }).when(mediaAssetMapper).insert(any(MediaAsset.class));

        RegisterMediaAssetRequest request = new RegisterMediaAssetRequest();
        request.setMediaType("image");
        request.setOriginalFilename("pet_photo.jpg");
        request.setUrl("https://cdn.test/pet_photo.jpg");

        MediaAssetVO result = aiTaskService.registerMediaAsset(request);

        assertEquals(MediaModerationStatus.PASS.getCode(), result.getModerationStatus());
        assertEquals(Boolean.TRUE, result.getAvailableForSubmit());
    }

    @Test
    void shouldReturnRejectStatusForRiskMedia() {
        doAnswer(invocation -> {
            MediaAsset asset = invocation.getArgument(0);
            asset.setId(3005L);
            return 1;
        }).when(mediaAssetMapper).insert(any(MediaAsset.class));

        RegisterMediaAssetRequest request = new RegisterMediaAssetRequest();
        request.setMediaType("image");
        request.setOriginalFilename("violent_blood_scene.jpg");
        request.setUrl("https://cdn.test/violent_blood_scene.jpg");

        MediaAssetVO result = aiTaskService.registerMediaAsset(request);

        assertEquals(MediaModerationStatus.REJECT.getCode(), result.getModerationStatus());
        assertEquals(Boolean.FALSE, result.getAvailableForSubmit());
    }

    @Test
    void shouldReturnEmptyDiagnosisSummaryWhenNoHistory() {
        when(diagnosisRecordMapper.selectList(any())).thenReturn(List.of());

        DiagnosisSummaryVO result = aiTaskService.getDiagnosisSummary(10L, 1L);

        assertEquals(Boolean.FALSE, result.getAvailable());
        assertEquals(0, result.getRecentDiagnosisCount());
        assertTrue(result.getRecentSymptoms().isEmpty());
    }

    @Test
    void shouldBuildDiagnosisSummaryFromSingleHistory() {
        DiagnosisRecord record = new DiagnosisRecord();
        record.setId(1L);
        record.setPetId(10L);
        record.setDiagnosisTime(new Date());
        record.setSymptomTagsJson("[\"呕吐\",\"食欲下降\"]");
        when(diagnosisRecordMapper.selectList(any())).thenReturn(List.of(record));
        when(diagnosisExtractedInfoMapper.selectList(any())).thenReturn(List.of());

        DiagnosisSummaryVO result = aiTaskService.getDiagnosisSummary(10L, 1L);

        assertEquals(Boolean.TRUE, result.getAvailable());
        assertEquals(1, result.getRecentDiagnosisCount());
        assertEquals(List.of("呕吐", "食欲下降"), result.getRecentSymptoms());
    }

    @Test
    void shouldReturnMedicalRecordsWithExtractedInfoMergedAndOrdered() {
        DiagnosisRecord latest = new DiagnosisRecord();
        latest.setId(2L);
        latest.setTaskId(20L);
        latest.setPetId(10L);
        latest.setDiagnosisTime(new Date(2000L));
        latest.setRiskLevel("medium");
        latest.setSummary("最近一次诊断");
        latest.setShouldConsultDoctor(1);
        latest.setSymptomTagsJson("[\"腹泻\"]");

        DiagnosisRecord earlier = new DiagnosisRecord();
        earlier.setId(1L);
        earlier.setTaskId(10L);
        earlier.setPetId(10L);
        earlier.setDiagnosisTime(new Date(1000L));
        earlier.setRiskLevel("low");
        earlier.setSummary("上一次诊断");
        earlier.setShouldConsultDoctor(0);
        earlier.setSymptomTagsJson("[\"咳嗽\"]");

        DiagnosisExtractedInfo extractedInfo = new DiagnosisExtractedInfo();
        extractedInfo.setRecordId(2L);
        extractedInfo.setPrimarySymptomsJson("[\"腹泻\"]");
        extractedInfo.setSeverity("中度");
        extractedInfo.setSuspectedIssuesJson("[\"胃肠不适\"]");
        extractedInfo.setAffectedPartsJson("[\"腹部\"]");
        extractedInfo.setFollowUpFocusJson("[\"精神状态\"]");

        when(diagnosisRecordMapper.selectList(any())).thenReturn(List.of(latest, earlier));
        when(diagnosisExtractedInfoMapper.selectList(any())).thenReturn(List.of(extractedInfo));

        List<DiagnosisMedicalRecordVO> result = aiTaskService.getMedicalRecords(10L, 1L, 10);

        assertEquals(2, result.size());
        assertEquals(2L, result.get(0).getRecordId());
        assertEquals("中度", result.get(0).getSeverity());
        assertEquals(List.of("精神状态"), result.get(0).getFollowUpFocus());
        assertEquals(1L, result.get(1).getRecordId());
        assertEquals(List.of("咳嗽"), result.get(1).getPrimarySymptoms());
    }
}
