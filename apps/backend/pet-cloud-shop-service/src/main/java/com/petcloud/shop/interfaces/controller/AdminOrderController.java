package com.petcloud.shop.interfaces.controller;

import com.petcloud.common.core.response.Response;
import com.petcloud.shop.domain.dto.OrderShipDTO;
import com.petcloud.shop.domain.service.OrderService;
import com.petcloud.shop.domain.vo.OrderTimelineVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @PostMapping("/{orderId}/ship")
    public Response<Void> shipOrder(@PathVariable Long orderId,
                                    @RequestBody OrderShipDTO dto) {
        orderService.shipOrder(
                orderId,
                dto.getLogisticsCompany(),
                dto.getTrackingNo(),
                dto.getRemark(),
                0L,
                "运营后台"
        );
        return Response.succeed();
    }

    @GetMapping("/{orderId}/timeline")
    public Response<List<OrderTimelineVO>> getTimeline(@PathVariable Long orderId) {
        return Response.succeed(orderService.getOrderTimeline(orderId));
    }
}
