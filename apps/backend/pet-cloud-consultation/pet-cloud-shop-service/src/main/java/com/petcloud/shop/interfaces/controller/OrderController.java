package com.petcloud.shop.interfaces.controller;

import com.petcloud.common.core.response.Response;
import com.petcloud.common.web.utils.UserContextHolderWeb;
import com.petcloud.shop.application.WxPayService;
import com.petcloud.shop.domain.dto.OrderCancelDTO;
import com.petcloud.shop.domain.dto.OrderConfirmDTO;
import com.petcloud.shop.domain.dto.OrderPayDTO;
import com.petcloud.shop.domain.dto.OrderReceiveDTO;
import com.petcloud.shop.domain.dto.OrderSubmitDTO;
import com.petcloud.shop.domain.service.OrderService;
import com.petcloud.shop.domain.vo.OrderConfirmVO;
import com.petcloud.shop.domain.vo.OrderDetailVO;
import com.petcloud.shop.domain.vo.OrderTimelineVO;
import com.petcloud.shop.domain.vo.PendingReviewOrderVO;
import com.petcloud.shop.domain.vo.WxPayParamsVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单控制器
 *
 * @author luohao
 */
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;
    private final UserContextHolderWeb userContextHolder;
    private final WxPayService wxPayService;

    /**
     * 获取订单确认页信息
     */
    @PostMapping("/confirm")
    public Response<OrderConfirmVO> getOrderConfirm(HttpServletRequest request,
                                                      @RequestBody OrderConfirmDTO req) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("获取订单确认页信息，userId: {}", userId);
        OrderConfirmVO confirmVO = orderService.getOrderConfirm(
                userId,
                req.getProductIds(),
                req.getQuantities(),
                req.getCartIds(),
                req.getSpecLabels()
        );
        return Response.succeed(confirmVO);
    }

    /**
     * 提交订单
     */
    @PostMapping("/submit")
    public Response<Long> submitOrder(HttpServletRequest request,
                                       @RequestBody OrderSubmitDTO req) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("提交订单，userId: {}", userId);
        Long orderId = orderService.submitOrder(userId, req);
        return Response.succeed(orderId);
    }

    /**
     * 获取订单列表
     */
    @GetMapping("/list")
    public Response<List<OrderDetailVO>> getOrderList(HttpServletRequest request,
                                                        @RequestParam(required = false) Integer status) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("获取订单列表，userId: {}, status: {}", userId, status);
        List<OrderDetailVO> orderList = orderService.getOrderList(userId, status);
        return Response.succeed(orderList);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{id}")
    public Response<OrderDetailVO> getOrderDetail(HttpServletRequest request,
                                                    @PathVariable Long id) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("获取订单详情，userId: {}, orderId: {}", userId, id);
        OrderDetailVO orderDetail = orderService.getOrderDetail(userId, id);
        return Response.succeed(orderDetail);
    }

    @GetMapping("/{id}/timeline")
    public Response<List<OrderTimelineVO>> getOrderTimeline(HttpServletRequest request,
                                                            @PathVariable Long id) {
        Long userId = userContextHolder.getRequiredUserId(request);
        orderService.getOrderDetail(userId, id);
        return Response.succeed(orderService.getOrderTimeline(id));
    }

    /**
     * 取消订单
     */
    @PutMapping("/cancel")
    public Response<Void> cancelOrder(HttpServletRequest request,
                                       @RequestBody OrderCancelDTO req) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("取消订单，userId: {}, orderId: {}", userId, req.getOrderId());
        orderService.cancelOrder(userId, req.getOrderId());
        return Response.succeed();
    }

    /**
     * 确认收货
     */
    @PutMapping("/confirm-receive")
    public Response<Void> confirmReceive(HttpServletRequest request,
                                          @RequestBody OrderReceiveDTO req) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("确认收货，userId: {}, orderId: {}", userId, req.getOrderId());
        orderService.confirmReceive(userId, req.getOrderId());
        return Response.succeed();
    }

    /**
     * 发起支付 - 返回 wx.requestPayment() 所需参数（BE-4.1）
     * 金额从数据库读取，防止前端篡改
     */
    @PostMapping("/pay")
    public Response<WxPayParamsVO> payOrder(HttpServletRequest request,
                                            @RequestBody OrderPayDTO req) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("发起支付，userId: {}, orderId: {}", userId, req.getOrderId());

        // 1. 从数据库获取订单金额（防篡改），转换为分
        OrderDetailVO order = orderService.getOrderDetail(userId, req.getOrderId());
        // 防止 NPE：如果 getPayAmount() 返回 null，默认为 0
        java.math.BigDecimal payAmount = order.getPayAmount() != null ? order.getPayAmount() : java.math.BigDecimal.ZERO;
        int totalFee = payAmount.multiply(java.math.BigDecimal.valueOf(100)).intValue();

        // 2. 调用 WxPayService 生成支付参数（沙箱模式或真实微信支付）
        WxPayParamsVO payParams = wxPayService.createOrder(req.getOrderId(), totalFee, null);

        // 3. 标记订单为支付中（可选：锁定状态）
        orderService.payOrder(userId, req.getOrderId());

        return Response.succeed(payParams);
    }

    /**
     * 获取各状态订单数量
     */
    @GetMapping("/count")
    public Response<Object> getOrderCount(HttpServletRequest request) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("获取各状态订单数量，userId: {}", userId);
        Object count = orderService.getOrderCount(userId);
        return Response.succeed(count);
    }

    /**
     * 获取待评价订单列表
     */
    @GetMapping("/pending-review")
    public Response<List<PendingReviewOrderVO>> getPendingReviewOrders(HttpServletRequest request,
                                                                         @RequestParam(defaultValue = "1") Integer page,
                                                                         @RequestParam(defaultValue = "10") Integer size) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("获取待评价订单列表，userId: {}", userId);
        List<PendingReviewOrderVO> orders = orderService.getPendingReviewOrders(userId, page, size);
        return Response.succeed(orders);
    }

    /**
     * 获取待评价商品数量
     */
    @GetMapping("/pending-review/count")
    public Response<Integer> getPendingReviewCount(HttpServletRequest request) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("获取待评价商品数量，userId: {}", userId);
        Integer count = orderService.getPendingReviewCount(userId);
        return Response.succeed(count);
    }
}
