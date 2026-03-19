package com.petcloud.user.interfaces.controller;

import com.petcloud.common.core.exception.BusinessException;
import com.petcloud.common.core.response.Response;
import com.petcloud.common.web.utils.UserContextHolderWeb;
import com.petcloud.user.domain.dto.AiDiagnosisDTO;
import com.petcloud.user.domain.enums.DiagnosisGuestLimitType;
import com.petcloud.user.domain.enums.UserTaskCode;
import com.petcloud.user.domain.enums.UserRespType;
import com.petcloud.user.domain.service.AiDiagnosisService;
import com.petcloud.user.domain.service.GuestLimitService;
import com.petcloud.user.domain.service.TaskService;
import com.petcloud.user.domain.vo.AiDiagnosisResultVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * AI诊断控制器
 *
 * 访客限制说明：
 * - 未登录用户可免费使用3次AI诊断功能
 * - 次数基于设备ID统计，24小时后重置
 * - 登录用户无限制
 *
 * @author luohao
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiDiagnosisController {

    private final AiDiagnosisService aiDiagnosisService;
    private final TaskService taskService;
    private final GuestLimitService guestLimitService;
    private final UserContextHolderWeb userContextHolderWeb;

    /**
     * 宠物AI健康诊断
     *
     * @param diagnosisDTO 诊断请求
     * @param deviceId 设备ID（访客必传）
     * @param request HttpServletRequest
     * @return 诊断结果
     */
    @PostMapping("/diagnosis")
    public Response<AiDiagnosisResultVO> diagnose(
            @Valid @RequestBody AiDiagnosisDTO diagnosisDTO,
            @RequestParam(required = false) String deviceId,
            HttpServletRequest request) {

        log.info("AI诊断请求，petType: {}, symptoms: {}, deviceId: {}",
                diagnosisDTO.getPetType(), diagnosisDTO.getSymptoms(), deviceId);

        // 获取当前用户ID
        Long userId = userContextHolderWeb.getCurrentUserId(request);
        boolean isLoggedIn = userId != null;

        // 访客检查使用限制
        if (!isLoggedIn) {
            // 检查设备ID
            if (deviceId == null || deviceId.trim().isEmpty()) {
                throw new BusinessException(UserRespType.DIAGNOSIS_DEVICE_ID_REQUIRED);
            }

            // 检查是否还有使用次数
            if (!guestLimitService.canUse(deviceId, DiagnosisGuestLimitType.AI_DIAGNOSIS.getCode())) {
                log.info("访客已达使用上限，deviceId: {}", deviceId);
                AiDiagnosisResultVO result = AiDiagnosisResultVO.builder()
                        .isLoggedIn(false)
                        .remainingCount(0)
                        .limitReached(true)
                        .result(null)
                        .build();
                return Response.succeed(result);
            }
        }

        // 执行诊断
        String diagnosisResult = aiDiagnosisService.diagnose(diagnosisDTO);

        // 记录访客使用次数
        int remainingCount = -1;
        if (!isLoggedIn && deviceId != null) {
            remainingCount = guestLimitService.recordUsage(deviceId, DiagnosisGuestLimitType.AI_DIAGNOSIS.getCode());
        }

        // 登录用户完成任务
        if (isLoggedIn) {
            try {
                taskService.completeTaskByCode(userId, UserTaskCode.AI_DIAGNOSIS.getCode());
                log.info("用户 {} 完成健康检测任务", userId);
            } catch (Exception e) {
                log.warn("完成健康检测任务失败: {}", e.getMessage());
            }
        }

        // 构建返回结果
        AiDiagnosisResultVO result = AiDiagnosisResultVO.builder()
                .result(diagnosisResult)
                .isLoggedIn(isLoggedIn)
                .remainingCount(isLoggedIn ? -1 : remainingCount)
                .limitReached(false)
                .build();

        return Response.succeed(result);
    }

    /**
     * 获取访客剩余使用次数
     *
     * @param deviceId 设备ID
     * @return 剩余次数信息
     */
    @GetMapping("/diagnosis/remaining")
    public Response<AiDiagnosisResultVO> getRemainingCount(
            @RequestParam String deviceId,
            HttpServletRequest request) {

        Long userId = userContextHolderWeb.getCurrentUserId(request);
        boolean isLoggedIn = userId != null;

        if (isLoggedIn) {
            // 登录用户无限制
            return Response.succeed(AiDiagnosisResultVO.builder()
                    .isLoggedIn(true)
                    .remainingCount(-1)
                    .limitReached(false)
                    .build());
        }

        // 访客返回剩余次数
        int remainingCount = guestLimitService.getRemainingCount(deviceId, DiagnosisGuestLimitType.AI_DIAGNOSIS.getCode());

        return Response.succeed(AiDiagnosisResultVO.builder()
                .isLoggedIn(false)
                .remainingCount(remainingCount)
                .limitReached(remainingCount <= 0)
                .build());
    }

    /**
     * 重置访客使用次数（测试用）
     *
     * @param deviceId 设备ID
     */
    @PostMapping("/diagnosis/reset")
    public Response<Void> resetGuestUsage(@RequestParam String deviceId) {
        guestLimitService.resetUsage(deviceId, DiagnosisGuestLimitType.AI_DIAGNOSIS.getCode());
        return Response.succeed();
    }
}
