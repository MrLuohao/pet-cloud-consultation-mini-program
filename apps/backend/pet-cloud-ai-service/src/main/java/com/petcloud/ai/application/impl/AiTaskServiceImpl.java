package com.petcloud.ai.application.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petcloud.ai.application.config.AliYunAiConfig;
import com.petcloud.ai.domain.dto.CreateDiagnosisTaskRequest;
import com.petcloud.ai.domain.dto.MediaAssetQueryRequest;
import com.petcloud.ai.domain.dto.RegisterMediaAssetRequest;
import com.petcloud.ai.domain.entity.AiTask;
import com.petcloud.ai.domain.entity.DiagnosisExtractedInfo;
import com.petcloud.ai.domain.entity.DiagnosisRecord;
import com.petcloud.ai.domain.entity.MediaAsset;
import com.petcloud.ai.domain.enums.AiBizType;
import com.petcloud.ai.domain.enums.AiModelProvider;
import com.petcloud.ai.domain.enums.AiTaskStatus;
import com.petcloud.ai.domain.enums.AiTaskType;
import com.petcloud.ai.domain.enums.DiagnosisRecordStatus;
import com.petcloud.ai.domain.enums.DiagnosisRiskLevel;
import com.petcloud.ai.domain.enums.MediaModerationStatus;
import com.petcloud.ai.domain.enums.MediaType;
import com.petcloud.ai.domain.enums.MediaUploadStatus;
import com.petcloud.ai.domain.service.AiTaskService;
import com.petcloud.ai.domain.vo.DiagnosisMedicalRecordVO;
import com.petcloud.ai.domain.vo.DiagnosisSummaryVO;
import com.petcloud.ai.domain.vo.DiagnosisTaskVO;
import com.petcloud.ai.domain.vo.MediaAssetVO;
import com.petcloud.ai.infrastructure.persistence.mapper.AiTaskMapper;
import com.petcloud.ai.infrastructure.persistence.mapper.DiagnosisExtractedInfoMapper;
import com.petcloud.ai.infrastructure.persistence.mapper.DiagnosisRecordMapper;
import com.petcloud.ai.infrastructure.persistence.mapper.MediaAssetMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HexFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiTaskServiceImpl implements AiTaskService {

    private final AiTaskMapper aiTaskMapper;
    private final DiagnosisRecordMapper diagnosisRecordMapper;
    private final DiagnosisExtractedInfoMapper diagnosisExtractedInfoMapper;
    private final MediaAssetMapper mediaAssetMapper;
    private final ObjectMapper objectMapper;
    private final AliYunAiConfig aliYunAiConfig;

    @Value("${ai.prompt.version.default:v1}")
    private String promptVersion;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DiagnosisTaskVO createDiagnosisTask(CreateDiagnosisTaskRequest request) {
        long start = System.currentTimeMillis();
        AiModelProvider modelProvider = resolveModelProvider();

        AiTask task = new AiTask();
        task.setTaskNo("diag_" + UUID.randomUUID().toString().replace("-", ""));
        task.setTaskType(AiTaskType.DIAGNOSIS_GENERATE.getCode());
        task.setBizType(AiBizType.DIAGNOSIS.getCode());
        task.setUserId(request.getUserId());
        task.setGuestDeviceHash(hashGuestDevice(request.getGuestDeviceHash()));
        task.setModelProvider(modelProvider.getCode());
        task.setModelName(modelProvider.getDefaultModelName());
        task.setPromptVersion(promptVersion);
        task.setTemplateVersion("diagnosis.v1");
        task.setStatus(AiTaskStatus.PROCESSING.getCode());
        task.setTraceId(UUID.randomUUID().toString());
        task.setInputSnapshot(writeJsonSafe(request));
        task.setIsDeleted(0);
        aiTaskMapper.insert(task);
        try {
            DiagnosisTaskVO result = buildDiagnosisResult(request);

            DiagnosisRecord record = new DiagnosisRecord();
            record.setTaskId(task.getId());
            record.setUserId(request.getUserId());
            record.setPetId(request.getPetId());
            record.setGuestDeviceHash(hashGuestDevice(request.getGuestDeviceHash()));
            record.setSymptomTagsJson(writeJsonSafe(request.getSymptomTags()));
            record.setSymptomDescription(request.getSymptomDescription());
            record.setRiskLevel(result.getRiskLevel());
            record.setSummary(result.getSummary());
            record.setPossibleCausesJson(writeJsonSafe(result.getPossibleCauses()));
            record.setCareSuggestionsJson(writeJsonSafe(result.getCareSuggestions()));
            record.setNextActionsJson(writeJsonSafe(result.getNextActions()));
            record.setObservationTableJson(writeJsonSafe(result.getObservationTable()));
            record.setShouldConsultDoctor(resolveRiskLevel(result.getRiskLevel()).requiresDoctor() ? 1 : 0);
            record.setStatus(DiagnosisRecordStatus.OBSERVING.getCode());
            record.setDiagnosisTime(new Date());
            record.setIsDeleted(0);
            diagnosisRecordMapper.insert(record);

            DiagnosisExtractedInfo extractedInfo = new DiagnosisExtractedInfo();
            extractedInfo.setRecordId(record.getId());
            extractedInfo.setPrimarySymptomsJson(writeJsonSafe(result.getKeyInfo().getPrimarySymptoms()));
            extractedInfo.setDurationText(result.getKeyInfo().getDuration());
            extractedInfo.setSeverity(result.getKeyInfo().getSeverity());
            extractedInfo.setSuspectedIssuesJson(writeJsonSafe(result.getKeyInfo().getSuspectedIssues()));
            extractedInfo.setAffectedPartsJson(writeJsonSafe(result.getKeyInfo().getAffectedParts()));
            extractedInfo.setFollowUpFocusJson(writeJsonSafe(result.getKeyInfo().getFollowUpFocus()));
            extractedInfo.setExtractVersion("extract.v1");
            extractedInfo.setIsDeleted(0);
            diagnosisExtractedInfoMapper.insert(extractedInfo);

            result = DiagnosisTaskVO.builder()
                    .taskId(task.getId())
                    .taskNo(task.getTaskNo())
                    .status(AiTaskStatus.COMPLETED.getCode())
                    .riskLevel(result.getRiskLevel())
                    .summary(result.getSummary())
                    .possibleCauses(result.getPossibleCauses())
                    .careSuggestions(result.getCareSuggestions())
                    .nextActions(result.getNextActions())
                    .observationTable(result.getObservationTable())
                    .keyInfo(result.getKeyInfo())
                    .build();

            task.setBizId(record.getId());
            task.setOutputSnapshot(writeJsonSafe(result));
            task.setStatus(AiTaskStatus.COMPLETED.getCode());
            task.setLatencyMs(System.currentTimeMillis() - start);
            aiTaskMapper.updateById(task);
            return result;
        } catch (RuntimeException e) {
            task.setStatus(AiTaskStatus.FAILED.getCode());
            task.setErrorMessage(resolveTaskErrorMessage(e));
            task.setLatencyMs(System.currentTimeMillis() - start);
            aiTaskMapper.updateById(task);
            throw e;
        }
    }

    @Override
    public DiagnosisTaskVO getDiagnosisTask(Long taskId) {
        AiTask task = aiTaskMapper.selectById(taskId);
        if (task == null) {
            return DiagnosisTaskVO.builder().taskId(taskId).status(AiTaskStatus.NOT_FOUND.getCode()).build();
        }
        if (!StringUtils.hasText(task.getOutputSnapshot())) {
            return DiagnosisTaskVO.builder()
                    .taskId(task.getId())
                    .taskNo(task.getTaskNo())
                    .status(task.getStatus())
                    .build();
        }
        try {
            DiagnosisTaskVO result = objectMapper.readValue(task.getOutputSnapshot(), DiagnosisTaskVO.class);
            result.setTaskId(task.getId());
            result.setTaskNo(task.getTaskNo());
            result.setStatus(task.getStatus());
            return result;
        } catch (JsonProcessingException e) {
            log.warn("解析AI任务结果失败, taskId: {}", taskId, e);
            return DiagnosisTaskVO.builder()
                    .taskId(task.getId())
                    .taskNo(task.getTaskNo())
                    .status(task.getStatus())
                    .build();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MediaAssetVO registerMediaAsset(RegisterMediaAssetRequest request) {
        List<String> riskTags = new ArrayList<>();
        MediaType mediaType = MediaType.fromCode(request.getMediaType());
        MediaModerationStatus moderationStatus = MediaModerationStatus.PASS;
        String reason = "内容审核通过";

        String normalizedName = String.valueOf(request.getOriginalFilename()).toLowerCase(Locale.ROOT);
        if (normalizedName.contains("blood") || normalizedName.contains("gore") || normalizedName.contains("violent")) {
            moderationStatus = MediaModerationStatus.REJECT;
            reason = "内容不符合发布规范，请更换素材后重试";
            riskTags.add("bloody");
            riskTags.add("violence");
        } else if (normalizedName.contains("scan") || normalizedName.contains("report") || normalizedName.contains("unclear")) {
            moderationStatus = MediaModerationStatus.REVIEW;
            reason = "素材已上传，审核中，请稍后再试";
            riskTags.add("manual_review");
        }

        MediaAsset asset = new MediaAsset();
        asset.setAssetNo("asset_" + UUID.randomUUID().toString().replace("-", ""));
        asset.setOwnerType(request.getOwnerType());
        asset.setOwnerId(request.getOwnerId());
        asset.setUserId(request.getUserId());
        asset.setMediaType(mediaType.getCode());
        asset.setUrl(request.getUrl());
        asset.setMimeType(request.getMimeType());
        asset.setFileSize(request.getFileSize());
        asset.setUploadStatus(MediaUploadStatus.UPLOADED.getCode());
        asset.setModerationStatus(moderationStatus.getCode());
        asset.setRiskTagsJson(writeJsonSafe(riskTags));
        asset.setReason(reason);
        asset.setIsDeleted(0);
        mediaAssetMapper.insert(asset);

        return MediaAssetVO.builder()
                .assetId(asset.getId())
                .assetNo(asset.getAssetNo())
                .url(asset.getUrl())
                .mediaType(mediaType.getCode())
                .uploadStatus(asset.getUploadStatus())
                .moderationStatus(moderationStatus.getCode())
                .availableForSubmit(moderationStatus.isAllowedForBizSubmission())
                .riskTags(riskTags)
                .reason(asset.getReason())
                .build();
    }

    @Override
    public List<MediaAssetVO> getMediaAssets(MediaAssetQueryRequest request) {
        if (request == null || request.getAssetIds() == null || request.getAssetIds().isEmpty()) {
            return List.of();
        }
        List<Long> assetIds = request.getAssetIds().stream().filter(Objects::nonNull).distinct().toList();
        if (assetIds.isEmpty()) {
            return List.of();
        }
        return mediaAssetMapper.selectBatchIds(assetIds).stream()
                .filter(asset -> asset.getIsDeleted() == null || asset.getIsDeleted() == 0)
                .map(this::toMediaAssetVO)
                .toList();
    }

    @Override
    public DiagnosisSummaryVO getDiagnosisSummary(Long petId, Long userId) {
        List<DiagnosisRecord> records = listDiagnosisRecords(petId, userId, 5);
        if (records.isEmpty()) {
            return DiagnosisSummaryVO.builder()
                    .petId(petId)
                    .available(false)
                    .recentDiagnosisCount(0)
                    .recentSymptoms(List.of())
                    .suggestedFocus(List.of())
                    .lastDiagnosisTime(null)
                    .build();
        }

        Map<Long, DiagnosisExtractedInfo> extractedInfoMap = getExtractedInfoMap(records);
        List<String> recentSymptoms = records.stream()
                .flatMap(record -> readStringList(record.getSymptomTagsJson()).stream())
                .distinct()
                .limit(5)
                .toList();
        List<String> suggestedFocus = records.stream()
                .map(record -> extractedInfoMap.get(record.getId()))
                .filter(Objects::nonNull)
                .flatMap(info -> readStringList(info.getFollowUpFocusJson()).stream())
                .distinct()
                .limit(5)
                .toList();

        return DiagnosisSummaryVO.builder()
                .petId(petId)
                .available(true)
                .recentDiagnosisCount(records.size())
                .recentSymptoms(recentSymptoms)
                .suggestedFocus(suggestedFocus)
                .lastDiagnosisTime(records.get(0).getDiagnosisTime())
                .build();
    }

    @Override
    public List<DiagnosisMedicalRecordVO> getMedicalRecords(Long petId, Long userId, Integer limit) {
        List<DiagnosisRecord> records = listDiagnosisRecords(petId, userId, normalizeLimit(limit));
        if (records.isEmpty()) {
            return List.of();
        }
        Map<Long, DiagnosisExtractedInfo> extractedInfoMap = getExtractedInfoMap(records);
        return records.stream()
                .map(record -> toDiagnosisMedicalRecord(record, extractedInfoMap.get(record.getId())))
                .toList();
    }

    private String resolveTaskErrorMessage(RuntimeException e) {
        String message = e.getMessage();
        if (!StringUtils.hasText(message)) {
            return "AI任务执行失败";
        }
        return message.length() > 500 ? message.substring(0, 500) : message;
    }

    private MediaAssetVO toMediaAssetVO(MediaAsset asset) {
        MediaModerationStatus moderationStatus = MediaModerationStatus.fromCode(asset.getModerationStatus());
        return MediaAssetVO.builder()
                .assetId(asset.getId())
                .assetNo(asset.getAssetNo())
                .url(asset.getUrl())
                .mediaType(asset.getMediaType())
                .uploadStatus(asset.getUploadStatus())
                .moderationStatus(moderationStatus.getCode())
                .availableForSubmit(moderationStatus.isAllowedForBizSubmission())
                .riskTags(readStringList(asset.getRiskTagsJson()))
                .reason(asset.getReason())
                .build();
    }

    private List<DiagnosisRecord> listDiagnosisRecords(Long petId, Long userId, int limit) {
        LambdaQueryWrapper<DiagnosisRecord> wrapper = new LambdaQueryWrapper<DiagnosisRecord>()
                .eq(DiagnosisRecord::getPetId, petId)
                .orderByDesc(DiagnosisRecord::getDiagnosisTime)
                .last("limit " + limit);
        if (userId != null) {
            wrapper.eq(DiagnosisRecord::getUserId, userId);
        }
        return diagnosisRecordMapper.selectList(wrapper);
    }

    private Map<Long, DiagnosisExtractedInfo> getExtractedInfoMap(List<DiagnosisRecord> records) {
        List<Long> recordIds = records.stream().map(DiagnosisRecord::getId).filter(Objects::nonNull).toList();
        if (recordIds.isEmpty()) {
            return Map.of();
        }
        return diagnosisExtractedInfoMapper.selectList(new LambdaQueryWrapper<DiagnosisExtractedInfo>()
                        .in(DiagnosisExtractedInfo::getRecordId, recordIds))
                .stream()
                .collect(Collectors.toMap(DiagnosisExtractedInfo::getRecordId, info -> info, (left, right) -> left, HashMap::new));
    }

    private DiagnosisMedicalRecordVO toDiagnosisMedicalRecord(DiagnosisRecord record, DiagnosisExtractedInfo extractedInfo) {
        return DiagnosisMedicalRecordVO.builder()
                .recordId(record.getId())
                .taskId(record.getTaskId())
                .petId(record.getPetId())
                .diagnosisTime(record.getDiagnosisTime())
                .riskLevel(record.getRiskLevel())
                .summary(record.getSummary())
                .shouldConsultDoctor(record.getShouldConsultDoctor())
                .primarySymptoms(extractedInfo == null ? readStringList(record.getSymptomTagsJson()) : readStringList(extractedInfo.getPrimarySymptomsJson()))
                .severity(extractedInfo == null ? null : extractedInfo.getSeverity())
                .suspectedIssues(extractedInfo == null ? List.of() : readStringList(extractedInfo.getSuspectedIssuesJson()))
                .affectedParts(extractedInfo == null ? List.of() : readStringList(extractedInfo.getAffectedPartsJson()))
                .followUpFocus(extractedInfo == null ? List.of() : readStringList(extractedInfo.getFollowUpFocusJson()))
                .build();
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return 10;
        }
        return Math.min(limit, 50);
    }

    private DiagnosisTaskVO buildDiagnosisResult(CreateDiagnosisTaskRequest request) {
        String symptomText = String.valueOf(request.getSymptomDescription()).toLowerCase(Locale.ROOT);
        List<String> sourceSymptoms = request.getSymptomTags() == null ? List.of() : request.getSymptomTags();
        DiagnosisRiskLevel riskLevel = DiagnosisRiskLevel.LOW;
        String severity = "轻度";
        List<String> possibleCauses = new ArrayList<>(List.of("环境刺激", "短期状态波动"));
        List<String> careSuggestions = new ArrayList<>(List.of("记录 24-48 小时内变化", "保持饮食和饮水规律"));
        List<String> nextActions = new ArrayList<>(List.of("若症状持续或加重，建议及时问诊"));

        if (symptomText.contains("呕吐") || symptomText.contains("腹泻") || symptomText.contains("精神") || containsAny(sourceSymptoms, "呕吐", "腹泻", "精神萎靡")) {
            riskLevel = DiagnosisRiskLevel.MEDIUM;
            severity = "中度";
            possibleCauses = new ArrayList<>(List.of("胃肠道不适", "饮食刺激或应激反应"));
            careSuggestions = new ArrayList<>(List.of("先观察进食饮水和排便情况", "避免短时间内频繁更换饮食"));
            nextActions = new ArrayList<>(List.of("若 24 小时仍无改善，建议发起人工问诊", "出现脱水或持续呕吐时尽快就医"));
        }
        if (symptomText.contains("血") || symptomText.contains("抽搐") || symptomText.contains("呼吸") || containsAny(sourceSymptoms, "呼吸困难", "抽搐", "便血")) {
            riskLevel = DiagnosisRiskLevel.HIGH;
            severity = "较高";
            possibleCauses = new ArrayList<>(List.of("急性异常反应", "潜在器官或神经系统风险"));
            careSuggestions = new ArrayList<>(List.of("减少移动和刺激，保持安静", "不要继续自行尝试复杂处理"));
            nextActions = new ArrayList<>(List.of("建议尽快线下就医", "必要时立即联系专业医生"));
        }

        List<Map<String, String>> observationTable = new ArrayList<>();
        observationTable.add(row("观察项", "建议补充"));
        observationTable.add(row("持续时间", "记录首次出现时间和最近是否加重"));
        observationTable.add(row("伴随表现", "补充食欲、饮水、精神状态变化"));
        observationTable.add(row("触发因素", "回忆是否更换粮食、环境或洗护用品"));

        List<String> primarySymptoms = sourceSymptoms.isEmpty() ? List.of("症状待进一步确认") : sourceSymptoms;
        return DiagnosisTaskVO.builder()
                .riskLevel(riskLevel.getCode())
                .summary(buildSummary(riskLevel, primarySymptoms))
                .possibleCauses(possibleCauses)
                .careSuggestions(careSuggestions)
                .nextActions(nextActions)
                .observationTable(observationTable)
                .keyInfo(DiagnosisTaskVO.KeyInfoVO.builder()
                        .primarySymptoms(primarySymptoms)
                        .duration("待补充")
                        .severity(severity)
                        .suspectedIssues(possibleCauses)
                        .affectedParts(extractAffectedParts(request.getSymptomDescription()))
                        .followUpFocus(List.of("是否持续加重", "是否出现新症状", "精神和食欲变化"))
                        .build())
                .build();
    }

    private boolean containsAny(List<String> source, String... targets) {
        for (String item : source) {
            for (String target : targets) {
                if (target.equals(item)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String buildSummary(DiagnosisRiskLevel riskLevel, List<String> symptoms) {
        String symptomSummary = symptoms.isEmpty() ? "当前症状" : String.join("、", symptoms);
        return switch (riskLevel) {
            case HIGH -> symptomSummary + " 相关风险较高，建议优先线下就医或尽快联系专业医生。";
            case MEDIUM -> symptomSummary + " 存在持续发展风险，建议短时间内密切观察并准备进一步问诊。";
            default -> symptomSummary + " 暂未显示出强烈高危信号，但仍建议持续观察并记录变化。";
        };
    }

    private AiModelProvider resolveModelProvider() {
        return StringUtils.hasText(aliYunAiConfig.getApiKey()) ? AiModelProvider.DASHSCOPE : AiModelProvider.RULE;
    }

    private DiagnosisRiskLevel resolveRiskLevel(String riskLevel) {
        if (DiagnosisRiskLevel.HIGH.getCode().equalsIgnoreCase(riskLevel)) {
            return DiagnosisRiskLevel.HIGH;
        }
        if (DiagnosisRiskLevel.MEDIUM.getCode().equalsIgnoreCase(riskLevel)) {
            return DiagnosisRiskLevel.MEDIUM;
        }
        return DiagnosisRiskLevel.LOW;
    }

    private List<String> extractAffectedParts(String description) {
        if (!StringUtils.hasText(description)) {
            return List.of("待补充");
        }
        String normalized = description.toLowerCase(Locale.ROOT);
        List<String> parts = new ArrayList<>();
        if (normalized.contains("耳")) {
            parts.add("耳部");
        }
        if (normalized.contains("腹")) {
            parts.add("腹部");
        }
        if (normalized.contains("皮肤")) {
            parts.add("皮肤");
        }
        if (parts.isEmpty()) {
            parts.add("待补充");
        }
        return parts;
    }

    private Map<String, String> row(String key, String value) {
        Map<String, String> row = new LinkedHashMap<>();
        row.put("label", key);
        row.put("value", value);
        return row;
    }

    private String writeJsonSafe(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private List<String> readStringList(String value) {
        if (!StringUtils.hasText(value)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(value, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }

    private String hashGuestDevice(String guestDevice) {
        if (!StringUtils.hasText(guestDevice)) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(guestDevice.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            return guestDevice;
        }
    }
}
