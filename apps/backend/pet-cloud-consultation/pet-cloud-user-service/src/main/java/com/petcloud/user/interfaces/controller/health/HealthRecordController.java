package com.petcloud.user.interfaces.controller.health;

import com.petcloud.common.core.response.Response;
import com.petcloud.common.web.utils.UserContextHolderWeb;
import com.petcloud.user.domain.dto.HealthRecordCreateDTO;
import com.petcloud.user.domain.dto.HealthRecordUpdateDTO;
import com.petcloud.user.domain.dto.HealthReminderCreateDTO;
import com.petcloud.user.domain.service.HealthRecordService;
import com.petcloud.user.domain.service.HealthReminderService;
import com.petcloud.user.domain.vo.HealthRecordVO;
import com.petcloud.user.domain.vo.HealthReminderVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 健康档案控制器
 *
 * @author luohao
 */
@Slf4j
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthRecordController {

    private final HealthRecordService healthRecordService;
    private final HealthReminderService healthReminderService;
    private final UserContextHolderWeb userContextHolderWeb;

    @GetMapping("/list")
    public Response<List<HealthRecordVO>> getHealthRecordList(HttpServletRequest request) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        return Response.succeed(healthRecordService.getHealthRecordList(userId));
    }

    @GetMapping("/pet/{petId}")
    public Response<List<HealthRecordVO>> getHealthRecordsByPet(HttpServletRequest request,
                                                             @PathVariable Long petId) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        return Response.succeed(healthRecordService.getHealthRecordsByPet(userId, petId));
    }

    @PostMapping("/create")
    public Response<Long> createHealthRecord(HttpServletRequest request,
                                          @RequestBody HealthRecordCreateDTO dto) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        Long recordId = healthRecordService.createHealthRecord(
                userId, dto.getPetId(), dto.getRecordType(), dto.getTitle(),
                dto.getContent(), dto.getHospitalName(), dto.getDoctorName(),
                dto.getRecordDate(), dto.getNextDate(), dto.getImages()
        );
        return Response.succeed(recordId);
    }

    @PutMapping("/update")
    public Response<Void> updateHealthRecord(HttpServletRequest request,
                                          @RequestBody HealthRecordUpdateDTO dto) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        healthRecordService.updateHealthRecord(
                dto.getId(), userId, dto.getRecordType(), dto.getTitle(),
                dto.getContent(), dto.getHospitalName(), dto.getDoctorName(),
                dto.getRecordDate(), dto.getNextDate(), dto.getImages()
        );
        return Response.succeed();
    }

    @DeleteMapping("/delete")
    public Response<Void> deleteHealthRecord(HttpServletRequest request, @RequestParam Long recordId) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        healthRecordService.deleteHealthRecord(recordId, userId);
        return Response.succeed();
    }

    // ==================== 健康提醒 ====================

    @GetMapping("/reminder/list")
    public Response<List<HealthReminderVO>> getReminderList(HttpServletRequest request) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        return Response.succeed(healthReminderService.getList(userId));
    }

    @PostMapping("/reminder/create")
    public Response<Long> createReminder(HttpServletRequest request,
                                         @RequestBody HealthReminderCreateDTO dto) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        Long id = healthReminderService.create(userId, dto);
        return Response.succeed(id);
    }

    @PutMapping("/reminder/{id}/done")
    public Response<Void> markReminderDone(HttpServletRequest request, @PathVariable Long id) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        healthReminderService.markDone(id, userId);
        return Response.succeed();
    }

    @DeleteMapping("/reminder/{id}")
    public Response<Void> deleteReminder(HttpServletRequest request, @PathVariable Long id) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        healthReminderService.delete(id, userId);
        return Response.succeed();
    }
}
