package com.petcloud.user.interfaces.controller;

import com.petcloud.common.core.response.Response;
import com.petcloud.common.web.utils.UserContextHolderWeb;
import com.petcloud.user.domain.dto.DiagnosisSubmitDTO;
import com.petcloud.user.domain.dto.MediaAssetQueryRequest;
import com.petcloud.user.domain.enums.DiagnosisGuestLimitType;
import com.petcloud.user.domain.vo.DiagnosisSubmitVO;
import com.petcloud.user.domain.service.GuestLimitService;
import com.petcloud.user.domain.service.PetService;
import com.petcloud.user.domain.vo.DiagnosisEntryVO;
import com.petcloud.user.domain.vo.UserPetVO;
import com.petcloud.user.infrastructure.feign.AiServiceClient;
import com.petcloud.user.infrastructure.feign.MediaServiceClient;
import com.petcloud.user.infrastructure.feign.dto.DiagnosisTaskVO;
import com.petcloud.user.infrastructure.feign.dto.MediaAssetVO;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiagnosisControllerTest {

    @Mock
    private UserContextHolderWeb userContextHolderWeb;

    @Mock
    private GuestLimitService guestLimitService;

    @Mock
    private PetService petService;

    @Mock
    private AiServiceClient aiServiceClient;

    @Mock
    private MediaServiceClient mediaServiceClient;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private DiagnosisController diagnosisController;

    @Test
    void shouldReturnLoggedInEntryWithDefaultPetAndArchivePlaceholder() {
        UserPetVO pet = UserPetVO.builder().id(101L).name("团子").build();
        when(userContextHolderWeb.getCurrentUserId(request)).thenReturn(1L);
        when(petService.getPetList(1L)).thenReturn(List.of(pet));

        Response<DiagnosisEntryVO> response = diagnosisController.getEntry(request, null);

        assertTrue(response.isSuccess());
        assertTrue(response.getData().getLoggedIn());
        assertEquals(-1, response.getData().getRemainingCount());
        assertEquals(101L, response.getData().getDefaultPetId());
        assertEquals(1, response.getData().getPets().size());
        assertNotNull(response.getData().getArchiveSummary());
        assertFalse(response.getData().getArchiveSummary().getAvailable());
        assertEquals("诊断档案摘要待接入", response.getData().getArchiveSummary().getNote());
    }

    @Test
    void shouldReturnGuestEntryWithDeviceLimit() {
        when(userContextHolderWeb.getCurrentUserId(request)).thenReturn(null);
        when(guestLimitService.getRemainingCount("device-1", DiagnosisGuestLimitType.AI_DIAGNOSIS.getCode())).thenReturn(2);

        Response<DiagnosisEntryVO> response = diagnosisController.getEntry(request, "device-1");

        assertTrue(response.isSuccess());
        assertFalse(response.getData().getLoggedIn());
        assertEquals(2, response.getData().getRemainingCount());
        assertTrue(response.getData().getPets().isEmpty());
        assertNull(response.getData().getDefaultPetId());
        assertNull(response.getData().getArchiveSummary());
        verify(guestLimitService).getRemainingCount("device-1", DiagnosisGuestLimitType.AI_DIAGNOSIS.getCode());
    }

    @Test
    void shouldReturnGuestEntryWithoutDeviceId() {
        when(userContextHolderWeb.getCurrentUserId(request)).thenReturn(null);

        Response<DiagnosisEntryVO> response = diagnosisController.getEntry(request, null);

        assertTrue(response.isSuccess());
        assertFalse(response.getData().getLoggedIn());
        assertEquals(0, response.getData().getRemainingCount());
        assertTrue(response.getData().getPets().isEmpty());
        assertNull(response.getData().getDefaultPetId());
        assertNull(response.getData().getArchiveSummary());
    }

    @Test
    void shouldRejectGuestSubmitWhenLimitReached() {
        DiagnosisSubmitDTO dto = new DiagnosisSubmitDTO();
        dto.setSymptomDescription("食欲下降");
        when(userContextHolderWeb.getCurrentUserId(request)).thenReturn(null);
        when(guestLimitService.canUse("device-1", DiagnosisGuestLimitType.AI_DIAGNOSIS.getCode())).thenReturn(false);

        Response<DiagnosisSubmitVO> response = diagnosisController.submitDiagnosis(dto, "device-1", request);

        assertTrue(response.isSuccess());
        assertEquals("rejected", response.getData().getStatus());
        assertTrue(response.getData().getLimitReached());
        assertEquals(0, response.getData().getRemainingCount());
    }

    @Test
    void shouldRejectGuestSubmitWhenDeviceIdMissing() {
        DiagnosisSubmitDTO dto = new DiagnosisSubmitDTO();
        dto.setSymptomDescription("食欲下降");
        when(userContextHolderWeb.getCurrentUserId(request)).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> diagnosisController.submitDiagnosis(dto, null, request));
        assertEquals("设备ID不能为空", exception.getMessage());
    }

    @Test
    void shouldSubmitDiagnosisSuccessfully() {
        DiagnosisSubmitDTO dto = new DiagnosisSubmitDTO();
        dto.setSymptomDescription("食欲下降");
        dto.setMediaAssetIds(List.of(11L));

        MediaAssetVO assetVO = new MediaAssetVO();
        assetVO.setAssetId(11L);
        assetVO.setAvailableForSubmit(true);
        DiagnosisTaskVO taskVO = new DiagnosisTaskVO();
        taskVO.setTaskId(200L);
        taskVO.setTaskNo("diag_200");
        taskVO.setStatus("completed");

        when(userContextHolderWeb.getCurrentUserId(request)).thenReturn(null);
        when(guestLimitService.canUse("device-1", DiagnosisGuestLimitType.AI_DIAGNOSIS.getCode())).thenReturn(true);
        when(mediaServiceClient.getMediaAssets(any(MediaAssetQueryRequest.class))).thenReturn(Response.succeed(List.of(assetVO)));
        when(aiServiceClient.createDiagnosisTask(any())).thenReturn(Response.succeed(taskVO));
        when(guestLimitService.recordUsage("device-1", DiagnosisGuestLimitType.AI_DIAGNOSIS.getCode())).thenReturn(2);

        Response<DiagnosisSubmitVO> response = diagnosisController.submitDiagnosis(dto, "device-1", request);

        assertTrue(response.isSuccess());
        assertEquals(200L, response.getData().getTaskId());
        assertEquals(2, response.getData().getRemainingCount());
        assertFalse(response.getData().getLimitReached());
    }

    @Test
    void shouldRejectDiagnosisSubmitWhenAssetUnavailable() {
        DiagnosisSubmitDTO dto = new DiagnosisSubmitDTO();
        dto.setSymptomDescription("食欲下降");
        dto.setMediaAssetIds(List.of(11L));

        MediaAssetVO assetVO = new MediaAssetVO();
        assetVO.setAssetId(11L);
        assetVO.setAvailableForSubmit(false);
        assetVO.setReason("素材已上传，审核中，请稍后再试");

        when(userContextHolderWeb.getCurrentUserId(request)).thenReturn(1L);
        when(mediaServiceClient.getMediaAssets(any(MediaAssetQueryRequest.class))).thenReturn(Response.succeed(List.of(assetVO)));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> diagnosisController.submitDiagnosis(dto, null, request));
        assertEquals("存在不可用的诊断图片: 素材已上传，审核中，请稍后再试", exception.getMessage());
    }
}
