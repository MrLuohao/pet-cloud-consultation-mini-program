package com.petcloud.user.interfaces.controller;

import com.petcloud.common.core.response.Response;
import com.petcloud.common.web.utils.UserContextHolderWeb;
import com.petcloud.user.domain.dto.PetCreateDTO;
import com.petcloud.user.domain.dto.PetUpdateDTO;
import com.petcloud.user.domain.service.PetService;
import com.petcloud.user.domain.vo.PetMonthlyReportVO;
import com.petcloud.user.domain.vo.PetTimelineVO;
import com.petcloud.user.domain.vo.UserPetVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 宠物管理控制器
 *
 * @author luohao
 */
@Slf4j
@RestController
@RequestMapping("/api/pet")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;
    private final UserContextHolderWeb userContextHolderWeb;

    @GetMapping("/list")
    public Response<List<UserPetVO>> getPetList(HttpServletRequest request) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        return Response.succeed(petService.getPetList(userId));
    }

    @GetMapping("/{id}")
    public Response<UserPetVO> getPetDetail(HttpServletRequest request, @PathVariable Long id) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        return Response.succeed(petService.getPetDetail(userId, id));
    }

    @PostMapping("/create")
    public Response<Long> createPet(HttpServletRequest request, @RequestBody PetCreateDTO petRequest) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        Long petId = petService.createPet(
                userId, petRequest.getName(), petRequest.getType(), petRequest.getBreed(),
                petRequest.getGender(), petRequest.getBirthday(), petRequest.getWeight(),
                petRequest.getAvatarUrl(), petRequest.getHealthStatus(),
                petRequest.getPersonality(), petRequest.getMotto()
        );
        return Response.succeed(petId);
    }

    @PutMapping("/update")
    public Response<Void> updatePet(HttpServletRequest request, @RequestBody PetUpdateDTO petUpdateRequest) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        petService.updatePet(
                petUpdateRequest.getId(), userId, petUpdateRequest.getName(), petUpdateRequest.getType(),
                petUpdateRequest.getBreed(), petUpdateRequest.getGender(), petUpdateRequest.getBirthday(),
                petUpdateRequest.getWeight(), petUpdateRequest.getAvatarUrl(), petUpdateRequest.getHealthStatus(),
                petUpdateRequest.getPersonality(), petUpdateRequest.getMotto()
        );
        return Response.succeed();
    }

    @DeleteMapping("/delete")
    public Response<Void> deletePet(HttpServletRequest request, @RequestParam Long petId) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        petService.deletePet(petId, userId);
        return Response.succeed();
    }

    /** 宠物时间轴 */
    @GetMapping("/{petId}/timeline")
    public Response<List<PetTimelineVO>> getPetTimeline(HttpServletRequest request, @PathVariable Long petId) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        return Response.succeed(petService.getPetTimeline(userId, petId));
    }

    /** 宠物月度报告 */
    @GetMapping("/{petId}/monthly-report")
    public Response<PetMonthlyReportVO> getMonthlyReport(HttpServletRequest request,
                                                          @PathVariable Long petId,
                                                          @RequestParam Integer year,
                                                          @RequestParam Integer month) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        return Response.succeed(petService.getMonthlyReport(userId, petId, year, month));
    }
}
