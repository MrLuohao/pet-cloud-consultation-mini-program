package com.petcloud.user.interfaces.controller;

import com.petcloud.common.core.response.Response;
import com.petcloud.common.web.utils.UserContextHolderWeb;
import com.petcloud.user.domain.service.PetService;
import com.petcloud.user.domain.vo.DiagnosisMedicalRecordVO;
import com.petcloud.user.domain.vo.DiagnosisSummaryVO;
import com.petcloud.user.infrastructure.feign.AiServiceClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pets")
public class PetDiagnosisController {

    private final UserContextHolderWeb userContextHolderWeb;
    private final PetService petService;
    private final AiServiceClient aiServiceClient;

    @GetMapping("/{petId}/diagnosis-summary")
    public Response<DiagnosisSummaryVO> getDiagnosisSummary(@PathVariable Long petId,
                                                            HttpServletRequest request) {
        Long userId = userContextHolderWeb.getCurrentUserId(request);
        petService.getPetDetail(userId, petId);
        return Response.succeed(aiServiceClient.getDiagnosisSummary(petId, userId).getData());
    }

    @GetMapping("/{petId}/medical-records")
    public Response<List<DiagnosisMedicalRecordVO>> getMedicalRecords(@PathVariable Long petId,
                                                                      @RequestParam(defaultValue = "10") Integer limit,
                                                                      HttpServletRequest request) {
        Long userId = userContextHolderWeb.getCurrentUserId(request);
        petService.getPetDetail(userId, petId);
        return Response.succeed(aiServiceClient.getMedicalRecords(petId, userId, limit).getData());
    }
}
