package com.petcloud.shop.domain.service;

import com.petcloud.shop.domain.dto.OrderSubmitDTO;
import com.petcloud.shop.domain.vo.OrderConfirmVO;
import com.petcloud.shop.domain.vo.OrderDetailVO;
import com.petcloud.shop.domain.vo.OrderTimelineVO;
import com.petcloud.shop.domain.vo.PendingReviewOrderVO;

import java.util.List;

/**
 * 订单服务接口
 *
 * @author luohao
 */
public interface OrderService {

    /**
     * 获取订单确认页信息
     *
     * @param userId     用户ID
     * @param productIds 商品ID列表
     * @param quantities 数量列表
     * @param cartIds    购物车ID列表（从购物车下单时使用）
     * @return 订单确认VO
     */
    OrderConfirmVO getOrderConfirm(Long userId, List<Long> productIds, List<Integer> quantities, List<Long> cartIds, List<String> specLabels);

    /**
     * 提交订单
     *
     * @param userId 用户ID
     * @param dto    订单提交DTO
     * @return 订单ID
     */
    Long submitOrder(Long userId, OrderSubmitDTO dto);

    /**
     * 获取订单列表
     *
     * @param userId 用户ID
     * @param status 状态（可选）
     * @return 订单列表
     */
    List<OrderDetailVO> getOrderList(Long userId, Integer status);

    /**
     * 获取订单详情
     *
     * @param userId  用户ID
     * @param orderId 订单ID
     * @return 订单详情
     */
    OrderDetailVO getOrderDetail(Long userId, Long orderId);

    /**
     * 取消订单
     *
     * @param userId  用户ID
     * @param orderId 订单ID
     */
    void cancelOrder(Long userId, Long orderId);

    /**
     * 确认收货
     *
     * @param userId  用户ID
     * @param orderId 订单ID
     */
    void confirmReceive(Long userId, Long orderId);

    /**
     * 支付订单
     *
     * @param userId  用户ID
     * @param orderId 订单ID
     * @return 支付结果
     */
    Boolean payOrder(Long userId, Long orderId);

    /**
     * 获取各状态订单数量
     *
     * @param userId 用户ID
     * @return 各状态订单数量
     */
    Object getOrderCount(Long userId);

    /**
     * 获取待评价订单列表
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页数量
     * @return 待评价订单列表
     */
    List<PendingReviewOrderVO> getPendingReviewOrders(Long userId, Integer page, Integer size);

    /**
     * 获取待评价商品数量
     *
     * @param userId 用户ID
     * @return 待评价商品数量
     */
    Integer getPendingReviewCount(Long userId);

    void shipOrder(Long orderId, String logisticsCompany, String trackingNo, String remark, Long operatorId, String operatorName);

    List<OrderTimelineVO> getOrderTimeline(Long orderId);
}
