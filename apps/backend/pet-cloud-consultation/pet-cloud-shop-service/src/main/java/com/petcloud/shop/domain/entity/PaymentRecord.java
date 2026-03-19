package com.petcloud.shop.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 支付记录实体类
 *
 * @author luohao
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("payment_record")
public class PaymentRecord extends BaseEntity {

    /**
     * 订单ID
     */
    @TableField("order_id")
    private Long orderId;

    /**
     * 订单号
     */
    @TableField("order_no")
    private String orderNo;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 支付金额
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 支付方式：wechat,balance
     */
    @TableField("payment_method")
    private String paymentMethod;

    /**
     * 支付渠道
     */
    @TableField("payment_channel")
    private String paymentChannel;

    /**
     * 验证方式
     */
    @TableField("verify_type")
    private String verifyType;

    /**
     * 状态说明
     */
    @TableField("status_detail")
    private String statusDetail;

    /**
     * 客户端场景
     */
    @TableField("client_scene")
    private String clientScene;

    /**
     * 第三方交易号
     */
    @TableField("transaction_id")
    private String transactionId;

    /**
     * 状态：0待支付 1已支付 2已退款
     */
    @TableField("status")
    private Integer status;

    /**
     * 支付时间
     */
    @TableField("pay_time")
    private String payTime;

    /**
     * 退款时间
     */
    @TableField("refund_time")
    private String refundTime;

    /**
     * 支付状态枚举
     */
    public enum Status {
        PENDING(0, "待支付"),
        PAID(1, "已支付"),
        REFUNDED(2, "已退款");

        private final Integer code;
        private final String desc;

        Status(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 支付方式枚举
     */
    public enum PaymentMethod {
        WECHAT("wechat", "微信支付"),
        ALIPAY("alipay", "支付宝"),
        BANK("bank", "银行卡"),
        CREDIT("credit", "信用卡");

        private final String code;
        private final String desc;

        PaymentMethod(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }
}
