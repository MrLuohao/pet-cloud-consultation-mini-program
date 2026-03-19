package com.petcloud.user.interfaces.controller;

import com.petcloud.common.core.response.Response;
import com.petcloud.common.web.utils.UserContextHolderWeb;
import com.petcloud.user.domain.entity.BeautyBooking;
import com.petcloud.user.domain.service.BeautyService;
import com.petcloud.user.domain.vo.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 美容服务控制器
 *
 * @author luohao
 */
@Slf4j
@RestController
@RequestMapping("/api/beauty")
@RequiredArgsConstructor
public class BeautyController {

    private final BeautyService beautyService;
    private final UserContextHolderWeb userContextHolderWeb;

    /**
     * 获取门店列表（公开）
     */
    @GetMapping("/stores")
    public Response<List<BeautyStoreVO>> getStoreList() {
        log.info("获取美容门店列表");
        return Response.succeed(beautyService.getStoreList());
    }

    /**
     * 获取门店详情（公开）
     */
    @GetMapping("/store/{id}")
    public Response<BeautyStoreVO> getStoreDetail(@PathVariable Long id) {
        log.info("获取门店详情，storeId: {}", id);
        return Response.succeed(beautyService.getStoreDetail(id));
    }

    /**
     * 获取门店服务项目（公开）
     */
    @GetMapping("/store/{storeId}/services")
    public Response<List<BeautyServiceItemVO>> getStoreServices(@PathVariable Long storeId) {
        log.info("获取门店服务项目，storeId: {}", storeId);
        return Response.succeed(beautyService.getStoreServices(storeId));
    }

    /**
     * 获取可预约时间段（公开）
     */
    @GetMapping("/store/{storeId}/available-slots")
    public Response<List<AvailableSlotVO>> getAvailableSlots(
            @PathVariable Long storeId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        log.info("获取可预约时间段，storeId: {}, date: {}", storeId, date);
        return Response.succeed(beautyService.getAvailableSlots(storeId, date));
    }

    /**
     * 创建预约（需登录）
     */
    @PostMapping("/booking")
    public Response<Long> createBooking(HttpServletRequest request,
                                        @RequestBody BeautyBooking booking) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        booking.setUserId(userId);
        log.info("创建美容预约，userId: {}, storeId: {}", userId, booking.getStoreId());
        return Response.succeed(beautyService.createBooking(booking));
    }

    /**
     * 获取我的预约列表（需登录）
     */
    @GetMapping("/bookings")
    public Response<List<BeautyBookingVO>> getBookingList(
            HttpServletRequest request,
            @RequestParam(required = false) Integer status) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        log.info("获取预约列表，userId: {}, status: {}", userId, status);
        return Response.succeed(beautyService.getBookingList(userId, status));
    }

    /**
     * 获取预约详情（需登录）
     */
    @GetMapping("/booking/{id}")
    public Response<BeautyBookingVO> getBookingDetail(HttpServletRequest request,
                                                      @PathVariable Long id) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        log.info("获取预约详情，userId: {}, bookingId: {}", userId, id);
        return Response.succeed(beautyService.getBookingDetail(id, userId));
    }

    /**
     * 取消预约（需登录）
     */
    @PutMapping("/booking/{id}/cancel")
    public Response<Void> cancelBooking(HttpServletRequest request,
                                        @PathVariable Long id) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        log.info("取消预约，userId: {}, bookingId: {}", userId, id);
        beautyService.cancelBooking(id, userId);
        return Response.succeed();
    }

    /**
     * 更新预约状态（服务方使用，需登录）
     */
    @PutMapping("/booking/{id}/status")
    public Response<Void> updateBookingStatus(HttpServletRequest request,
                                              @PathVariable Long id,
                                              @RequestBody Map<String, Integer> body) {
        Long operatorId = userContextHolderWeb.getRequiredUserId(request);
        Integer status = body.get("status");
        log.info("更新预约状态，bookingId: {}, status: {}", id, status);
        beautyService.updateBookingStatus(id, status, operatorId);
        return Response.succeed();
    }

    /**
     * 上传服务照片（需登录）
     */
    @PostMapping("/booking/{id}/photos")
    public Response<Void> uploadBookingPhotos(HttpServletRequest request,
                                              @PathVariable Long id,
                                              @RequestBody Map<String, String> body) {
        userContextHolderWeb.getRequiredUserId(request);
        log.info("上传服务照片，bookingId: {}", id);
        beautyService.uploadBookingPhotos(
                id,
                body.get("beforePhoto"),
                body.get("afterPhoto"),
                body.get("servicePhotos")
        );
        return Response.succeed();
    }
}
