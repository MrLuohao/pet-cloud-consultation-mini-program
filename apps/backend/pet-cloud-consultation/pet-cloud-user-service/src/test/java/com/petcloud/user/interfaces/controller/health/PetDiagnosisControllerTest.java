package com.petcloud.user.interfaces.controller.health;

import com.petcloud.common.core.response.Response;
import com.petcloud.common.web.utils.UserContextHolderWeb;
import com.petcloud.user.domain.service.PetService;
import com.petcloud.user.domain.vo.DiagnosisMedicalRecordVO;
import com.petcloud.user.domain.vo.DiagnosisSummaryVO;
import com.petcloud.user.domain.vo.UserPetVO;
import com.petcloud.user.interfaces.controller.health.PetDiagnosisController;
import com.petcloud.user.infrastructure.feign.AiServiceClient;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PetDiagnosisControllerTest {

    @Mock
    private UserContextHolderWeb userContextHolderWeb;

    @Mock
    private PetService petService;

    @Mock
    private AiServiceClient aiServiceClient;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private PetDiagnosisController petDiagnosisController;

    @Test
    void shouldReturnDiagnosisSummaryForPet() {
        DiagnosisSummaryVO summaryVO = DiagnosisSummaryVO.builder()
                .petId(10L)
                .available(true)
                .recentDiagnosisCount(2)
                .build();
        when(userContextHolderWeb.getCurrentUserId(request)).thenReturn(1L);
        when(petService.getPetDetail(1L, 10L)).thenReturn(UserPetVO.builder().id(10L).build());
        when(aiServiceClient.getDiagnosisSummary(10L, 1L)).thenReturn(Response.succeed(summaryVO));

        Response<DiagnosisSummaryVO> response = petDiagnosisController.getDiagnosisSummary(10L, request);

        assertTrue(response.isSuccess());
        assertEquals(2, response.getData().getRecentDiagnosisCount());
        verify(petService).getPetDetail(1L, 10L);
    }

    @Test
    void shouldReturnMedicalRecordsForPet() {
        DiagnosisMedicalRecordVO recordVO = DiagnosisMedicalRecordVO.builder().recordId(100L).petId(10L).build();
        when(userContextHolderWeb.getCurrentUserId(request)).thenReturn(1L);
        when(petService.getPetDetail(1L, 10L)).thenReturn(UserPetVO.builder().id(10L).build());
        when(aiServiceClient.getMedicalRecords(10L, 1L, 5)).thenReturn(Response.succeed(List.of(recordVO)));

        Response<List<DiagnosisMedicalRecordVO>> response = petDiagnosisController.getMedicalRecords(10L, 5, request);

        assertTrue(response.isSuccess());
        assertEquals(1, response.getData().size());
        assertEquals(100L, response.getData().get(0).getRecordId());
    }
}
