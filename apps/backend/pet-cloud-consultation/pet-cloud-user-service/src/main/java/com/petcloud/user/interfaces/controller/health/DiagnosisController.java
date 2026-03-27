package com.petcloud.user.interfaces.controller.health;

import com.petcloud.common.core.exception.BusinessException;
import com.petcloud.common.core.response.Response;
import com.petcloud.common.web.utils.UserContextHolderWeb;
import com.petcloud.user.domain.dto.DiagnosisSubmitDTO;
import com.petcloud.user.domain.dto.MediaAssetQueryRequest;
import com.petcloud.user.domain.enums.DiagnosisGuestLimitType;
import com.petcloud.user.domain.enums.DiagnosisTaskStatus;
import com.petcloud.user.domain.enums.DiagnosisSubmitStatus;
import com.petcloud.user.domain.enums.UserRespType;
import com.petcloud.user.domain.service.GuestLimitService;
import com.petcloud.user.domain.service.PetService;
import com.petcloud.user.domain.vo.DiagnosisEntryVO;
import com.petcloud.user.domain.vo.DiagnosisSubmitVO;
import com.petcloud.user.domain.vo.DiagnosisTaskDetailVO;
import com.petcloud.user.domain.vo.UserPetVO;
import com.petcloud.user.infrastructure.feign.AiServiceClient;
import com.petcloud.user.infrastructure.feign.MediaServiceClient;
import com.petcloud.user.infrastructure.feign.dto.CreateDiagnosisTaskRequest;
import com.petcloud.user.infrastructure.feign.dto.DiagnosisTaskVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/api/diagnosis")
@RequiredArgsConstructor
public class DiagnosisController {

    private final UserContextHolderWeb userContextHolderWeb;
    private final GuestLimitService guestLimitService;
    private final PetService petService;
    private final AiServiceClient aiServiceClient;
    private final MediaServiceClient mediaServiceClient;

    @GetMapping("/entry")
    public Response<DiagnosisEntryVO> getEntry(HttpServletRequest request,
                                               @RequestParam(required = false) String deviceId) {
        Long userId = userContextHolderWeb.getCurrentUserId(request);
        if (userId != null) {
            List<UserPetVO> pets = petService.getPetList(userId);
            Long defaultPetId = pets.isEmpty() ? null : pets.get(0).getId();
            return Response.succeed(DiagnosisEntryVO.builder()
                    .loggedIn(true)
                    .remainingCount(-1)
                    .pets(pets)
                    .defaultPetId(defaultPetId)
                    .archiveSummary(buildArchiveSummaryPlaceholder(defaultPetId))
                    .build());
        }

        int remainingCount = 0;
        if (deviceId != null && !deviceId.isBlank()) {
            remainingCount = guestLimitService.getRemainingCount(deviceId, DiagnosisGuestLimitType.AI_DIAGNOSIS.getCode());
        }

        return Response.succeed(DiagnosisEntryVO.builder()
                .loggedIn(false)
                .remainingCount(remainingCount)
                .pets(Collections.emptyList())
                .defaultPetId(null)
                .archiveSummary(null)
                .build());
    }

    @PostMapping("/submit")
    public Response<DiagnosisSubmitVO> submitDiagnosis(@Valid @RequestBody DiagnosisSubmitDTO dto,
                                                       @RequestParam(required = false) String deviceId,
                                                       HttpServletRequest request) {
        Long userId = userContextHolderWeb.getCurrentUserId(request);
        boolean loggedIn = userId != null;

        if (!loggedIn) {
            if (deviceId == null || deviceId.isBlank()) {
                throw new BusinessException(UserRespType.DIAGNOSIS_DEVICE_ID_REQUIRED);
            }
            if (!guestLimitService.canUse(deviceId, DiagnosisGuestLimitType.AI_DIAGNOSIS.getCode())) {
                return Response.succeed(DiagnosisSubmitVO.builder()
                        .status(DiagnosisSubmitStatus.REJECTED.getCode())
                        .limitReached(true)
                        .remainingCount(0)
                        .build());
            }
        }

        validateDiagnosisMediaAssets(dto.getMediaAssetIds());

        CreateDiagnosisTaskRequest aiRequest = new CreateDiagnosisTaskRequest();
        aiRequest.setUserId(userId);
        aiRequest.setPetId(dto.getPetId());
        aiRequest.setGuestDeviceHash(deviceId);
        aiRequest.setPetType(dto.getPetType());
        aiRequest.setPetAgeMonths(dto.getPetAgeMonths());
        aiRequest.setSymptomTags(dto.getSymptomTags());
        aiRequest.setSymptomDescription(dto.getSymptomDescription());
        aiRequest.setMediaAssetIds(dto.getMediaAssetIds());

        DiagnosisTaskVO taskVO = aiServiceClient.createDiagnosisTask(aiRequest).getData();

        int remainingCount = -1;
        if (!loggedIn) {
            remainingCount = guestLimitService.recordUsage(deviceId, DiagnosisGuestLimitType.AI_DIAGNOSIS.getCode());
        }

        return Response.succeed(DiagnosisSubmitVO.builder()
                .taskId(taskVO.getTaskId())
                .taskNo(taskVO.getTaskNo())
                .status(taskVO.getStatus())
                .remainingCount(remainingCount)
                .limitReached(false)
                .build());
    }

    @GetMapping("/tasks/{taskId}")
    public Response<DiagnosisTaskDetailVO> getTaskDetail(@PathVariable Long taskId) {
        DiagnosisTaskVO taskVO = aiServiceClient.getDiagnosisTask(taskId).getData();
        if (taskVO == null || DiagnosisTaskStatus.fromCode(taskVO.getStatus()) == DiagnosisTaskStatus.NOT_FOUND) {
            throw new BusinessException(UserRespType.DIAGNOSIS_TASK_NOT_FOUND);
        }
        return Response.succeed(DiagnosisTaskDetailVO.builder()
                .taskId(taskVO.getTaskId())
                .taskNo(taskVO.getTaskNo())
                .status(taskVO.getStatus())
                .riskLevel(taskVO.getRiskLevel())
                .summary(taskVO.getSummary())
                .possibleCauses(taskVO.getPossibleCauses())
                .careSuggestions(taskVO.getCareSuggestions())
                .nextActions(taskVO.getNextActions())
                .observationTable(taskVO.getObservationTable())
                .keyInfo(taskVO.getKeyInfo() == null ? null : DiagnosisTaskDetailVO.KeyInfoVO.builder()
                        .primarySymptoms(taskVO.getKeyInfo().getPrimarySymptoms())
                        .duration(taskVO.getKeyInfo().getDuration())
                        .severity(taskVO.getKeyInfo().getSeverity())
                        .suspectedIssues(taskVO.getKeyInfo().getSuspectedIssues())
                        .affectedParts(taskVO.getKeyInfo().getAffectedParts())
                        .followUpFocus(taskVO.getKeyInfo().getFollowUpFocus())
                        .build())
                .build());
    }

    private DiagnosisEntryVO.ArchiveSummaryVO buildArchiveSummaryPlaceholder(Long petId) {
        if (petId == null) {
            return null;
        }
        return DiagnosisEntryVO.ArchiveSummaryVO.builder()
                .available(false)
                .petId(petId)
                .recentDiagnosisCount(0)
                .recentSymptoms(Collections.emptyList())
                .suggestedFocus(Collections.emptyList())
                .note("诊断档案摘要待接入")
                .build();
    }

    private void validateDiagnosisMediaAssets(List<Long> mediaAssetIds) {
        if (mediaAssetIds == null || mediaAssetIds.isEmpty()) {
            return;
        }
        MediaAssetQueryRequest queryRequest = new MediaAssetQueryRequest();
        queryRequest.setAssetIds(mediaAssetIds);
        List<com.petcloud.user.infrastructure.feign.dto.MediaAssetVO> assets = mediaServiceClient.getMediaAssets(queryRequest).getData();
        long expectedCount = mediaAssetIds.stream().filter(Objects::nonNull).distinct().count();
        if (assets == null || assets.size() != expectedCount) {
            throw new BusinessException(UserRespType.DIAGNOSIS_MEDIA_ASSET_INVALID, "素材不存在或已失效");
        }
        assets.stream()
                .filter(asset -> !Boolean.TRUE.equals(asset.getAvailableForSubmit()))
                .findFirst()
                .ifPresent(asset -> {
                    String reason = asset.getReason() == null ? "素材审核未通过" : asset.getReason();
                    throw new BusinessException(UserRespType.DIAGNOSIS_MEDIA_ASSET_INVALID, reason);
                });
    }
}
