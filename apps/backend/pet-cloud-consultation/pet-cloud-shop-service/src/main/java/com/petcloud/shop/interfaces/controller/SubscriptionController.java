package com.petcloud.shop.interfaces.controller;

import com.petcloud.common.core.response.Response;
import com.petcloud.common.web.utils.UserContextHolderWeb;
import com.petcloud.shop.domain.dto.SubscriptionConfigDTO;
import com.petcloud.shop.domain.dto.SubscriptionCreateDTO;
import com.petcloud.shop.domain.service.ProductSubscriptionService;
import com.petcloud.shop.domain.vo.ProductSubscriptionVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品订阅控制器
 *
 * @author luohao
 */
@Slf4j
@RestController
@RequestMapping("/api/subscribe")
@RequiredArgsConstructor
public class SubscriptionController {

    private final ProductSubscriptionService subscriptionService;
    private final UserContextHolderWeb userContextHolder;

    /** 创建订阅 */
    @PostMapping("/create")
    public Response<Long> createSubscription(HttpServletRequest request,
                                              @RequestBody SubscriptionCreateDTO dto) {
        Long userId = userContextHolder.getRequiredUserId(request);
        return Response.succeed(subscriptionService.createSubscription(userId, dto));
    }

    /** 查询订阅列表 */
    @GetMapping("/list")
    public Response<List<ProductSubscriptionVO>> getSubscriptionList(HttpServletRequest request) {
        Long userId = userContextHolder.getRequiredUserId(request);
        return Response.succeed(subscriptionService.getSubscriptionList(userId));
    }

    /** 暂停订阅 */
    @PutMapping("/{id}/pause")
    public Response<Void> pause(HttpServletRequest request, @PathVariable Long id) {
        Long userId = userContextHolder.getRequiredUserId(request);
        subscriptionService.pauseSubscription(id, userId);
        return Response.succeed();
    }

    /** 恢复订阅 */
    @PutMapping("/{id}/resume")
    public Response<Void> resume(HttpServletRequest request, @PathVariable Long id) {
        Long userId = userContextHolder.getRequiredUserId(request);
        subscriptionService.resumeSubscription(id, userId);
        return Response.succeed();
    }

    /** 取消订阅 */
    @PutMapping("/{id}/cancel")
    public Response<Void> cancel(HttpServletRequest request, @PathVariable Long id) {
        Long userId = userContextHolder.getRequiredUserId(request);
        subscriptionService.cancelSubscription(id, userId);
        return Response.succeed();
    }

    /** 修改配置（周期/数量/地址） */
    @PutMapping("/{id}/config")
    public Response<Void> updateConfig(HttpServletRequest request, @PathVariable Long id,
                                        @RequestBody SubscriptionConfigDTO dto) {
        Long userId = userContextHolder.getRequiredUserId(request);
        subscriptionService.updateConfig(id, userId, dto);
        return Response.succeed();
    }
}
