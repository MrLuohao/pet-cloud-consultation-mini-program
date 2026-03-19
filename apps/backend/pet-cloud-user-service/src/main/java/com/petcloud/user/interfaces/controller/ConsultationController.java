package com.petcloud.user.interfaces.controller;

import com.petcloud.common.core.response.Response;
import com.petcloud.common.web.utils.UserContextHolderWeb;
import com.petcloud.user.domain.dto.ConsultationCreateDTO;
import com.petcloud.user.domain.dto.ConsultationMessageDTO;
import com.petcloud.user.domain.dto.ConsultationReviewDTO;
import com.petcloud.user.domain.service.ConsultationReviewService;
import com.petcloud.user.domain.service.ConsultationService;
import com.petcloud.user.domain.vo.ConsultationMessageVO;
import com.petcloud.user.domain.vo.ConsultationReviewVO;
import com.petcloud.user.domain.vo.ConsultationVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 咨询控制器
 *
 * @author luohao
 */
@Slf4j
@RestController
@RequestMapping("/api/consultation")
@RequiredArgsConstructor
public class ConsultationController {

    private final ConsultationService consultationService;
    private final ConsultationReviewService consultationReviewService;
    private final UserContextHolderWeb userContextHolderWeb;

    @PostMapping("/create")
    public Response<Long> createConsultation(HttpServletRequest request,
                                          @RequestBody ConsultationCreateDTO consultationRequest) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        Long consultationId = consultationService.createConsultation(userId, consultationRequest);
        return Response.succeed(consultationId);
    }

    @GetMapping("/list")
    public Response<List<ConsultationVO>> getConsultationList(HttpServletRequest request) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        return Response.succeed(consultationService.getConsultationList(userId));
    }

    @GetMapping("/{id}")
    public Response<ConsultationVO> getConsultationDetail(HttpServletRequest request, @PathVariable Long id) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        return Response.succeed(consultationService.getConsultationDetail(userId, id));
    }

    @PostMapping("/message")
    public Response<Long> sendMessage(HttpServletRequest request,
                                   @RequestBody ConsultationMessageDTO messageRequest) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        Long messageId = consultationService.sendMessage(userId, messageRequest);
        return Response.succeed(messageId);
    }

    @GetMapping("/{id}/messages")
    public Response<List<ConsultationMessageVO>> getMessages(HttpServletRequest request, @PathVariable Long id) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        return Response.succeed(consultationService.getMessages(userId, id));
    }

    @PutMapping("/{id}/finish")
    public Response<Void> finishConsultation(HttpServletRequest request, @PathVariable Long id) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        consultationService.finishConsultation(userId, id);
        return Response.succeed();
    }

    @PutMapping("/{id}/cancel")
    public Response<Void> cancelConsultation(HttpServletRequest request, @PathVariable Long id) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        consultationService.cancelConsultation(userId, id);
        return Response.succeed();
    }

    /** 支付咨询费用（模拟支付） */
    @PutMapping("/{id}/pay")
    public Response<Void> payConsultation(HttpServletRequest request, @PathVariable Long id) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        consultationService.payConsultation(userId, id);
        return Response.succeed();
    }

    /** 提交咨询评价 */
    @PostMapping("/{id}/review")
    public Response<Void> submitReview(HttpServletRequest request,
                                       @PathVariable Long id,
                                       @RequestBody ConsultationReviewDTO dto) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        ConsultationVO consultation = consultationService.getConsultationDetail(userId, id);
        consultationReviewService.submitReview(userId, "", id, consultation.getDoctorId(), dto.getRating(), dto.getContent());
        return Response.succeed();
    }

    /** 获取医生评价列表 */
    @GetMapping("/doctor/{doctorId}/reviews")
    public Response<List<ConsultationReviewVO>> getDoctorReviews(@PathVariable Long doctorId) {
        return Response.succeed(consultationReviewService.getDoctorReviews(doctorId));
    }
}
