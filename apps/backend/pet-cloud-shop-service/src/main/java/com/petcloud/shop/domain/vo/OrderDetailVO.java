package com.petcloud.shop.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 订单详情VO
 *
 * @author luohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailVO {

    /**
     * 订单ID
     */
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 状态：0待付款 1待发货 2待收货 3已完成 4已取消
     */
    private Integer status;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 商品总金额
     */
    private BigDecimal totalAmount;

    /**
     * 优惠券优惠
     */
    private BigDecimal couponDiscount;

    /**
     * 实付金额
     */
    private BigDecimal payAmount;

    /**
     * 收货人
     */
    private String receiverName;

    /**
     * 收货电话
     */
    private String receiverPhone;

    /**
     * 收货地址
     */
    private String receiverAddress;

    /**
     * 订单备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 支付时间
     */
    private Date payTime;

    /**
     * 订单商品列表
     */
    private List<OrderItemVO> items;

    // Builder pattern support
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private OrderDetailVO vo = new OrderDetailVO();

        public Builder id(Long id) {
            vo.id = id;
            return this;
        }

        public Builder orderNo(String orderNo) {
            vo.orderNo = orderNo;
            return this;
        }

        public Builder status(Integer status) {
            vo.status = status;
            return this;
        }

        public Builder statusDesc(String statusDesc) {
            vo.statusDesc = statusDesc;
            return this;
        }

        public Builder totalAmount(BigDecimal totalAmount) {
            vo.totalAmount = totalAmount;
            return this;
        }

        public Builder couponDiscount(BigDecimal couponDiscount) {
            vo.couponDiscount = couponDiscount;
            return this;
        }

        public Builder payAmount(BigDecimal payAmount) {
            vo.payAmount = payAmount;
            return this;
        }

        public Builder receiverName(String receiverName) {
            vo.receiverName = receiverName;
            return this;
        }

        public Builder receiverPhone(String receiverPhone) {
            vo.receiverPhone = receiverPhone;
            return this;
        }

        public Builder receiverAddress(String receiverAddress) {
            vo.receiverAddress = receiverAddress;
            return this;
        }

        public Builder remark(String remark) {
            vo.remark = remark;
            return this;
        }

        public Builder createTime(Date createTime) {
            vo.createTime = createTime;
            return this;
        }

        public Builder payTime(Date payTime) {
            vo.payTime = payTime;
            return this;
        }

        public Builder items(List<OrderItemVO> items) {
            vo.items = items;
            return this;
        }

        public OrderDetailVO build() {
            return vo;
        }
    }
}
