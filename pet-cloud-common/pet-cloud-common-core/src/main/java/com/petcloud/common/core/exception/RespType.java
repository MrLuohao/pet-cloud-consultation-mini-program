package com.petcloud.common.core.exception;

import com.petcloud.common.core.response.IRespType;
import lombok.Getter;

/**
 * 响应类型枚举
 *
 * @author luohao
 */
@Getter
public enum RespType implements IRespType {
    // ========== 通用响应 ==========
    SUCCESS(true, "00000000", "成功"),
    FAILURE(false, "99999999", "系统错误, 请联系管理员!"),

    // ========== 系统错误 ==========
    BUSINESS_ERROR(false, "99990000", "业务执行异常"),
    API_CALL_ERROR(false, "99990001", "内部Api调用异常"),
    REQUEST_ERROR(false, "99990002", "请求方式错误"),
    PARAMETER_ERROR(false, "99990003", "参数校验失败"),
    DATABASE_ERROR(false, "99990004", "数据库执行异常"),
    JSON_ERROR(false, "99990005", "JSON转换异常"),

    // ========== AI 相关错误 ==========
    ALI_AI_TEXT_TO_IMAGE_ERROR(false, "99990006", "通义千问-文生图异常{}"),
    ALI_AI_IMAGE_EDIT_ERROR(false, "99990007", "通义千问-图像编辑失败{}"),

    // ========== 用户相关错误 ==========
    USER_NOT_FOUND(false, "40010001", "用户不存在"),
    ACCOUNT_DISABLED(false, "40010002", "账号已被禁用"),
    USER_ALREADY_EXISTS(false, "40010003", "用户已存在"),
    TOKEN_INVALID(false, "40010004", "Token无效或已过期"),
    TOKEN_REFRESH_FAILED(false, "40010005", "Token刷新失败"),

    // ========== 地址相关错误 ==========
    ADDRESS_NOT_FOUND(false, "40020001", "地址不存在"),
    ADDRESS_NO_PERMISSION(false, "40020002", "无权操作此地址"),
    ADDRESS_LIMIT_EXCEEDED(false, "40020003", "地址数量超过限制"),

    // ========== 宠物相关错误 ==========
    PET_NOT_FOUND(false, "40030001", "宠物信息不存在"),
    PET_LIMIT_EXCEEDED(false, "40030002", "宠物数量超过限制"),

    // ========== 文章相关错误 ==========
    ARTICLE_NOT_FOUND(false, "40040001", "文章不存在"),
    ARTICLE_ALREADY_LIKED(false, "40040002", "已经点赞过了"),
    ARTICLE_ALREADY_COLLECTED(false, "40040003", "已经收藏过了"),

    // ========== 评论相关错误 ==========
    COMMENT_NOT_FOUND(false, "40041001", "评论不存在"),
    COMMENT_DELETE_FORBIDDEN(false, "40041002", "无权删除此评论"),

    // ========== 课程相关错误 ==========
    COURSE_NOT_FOUND(false, "40050001", "课程不存在"),
    COURSE_ALREADY_REVIEWED(false, "40050002", "您已评价过该课程"),

    // ========== 美容相关错误 ==========
    STORE_NOT_FOUND(false, "40060001", "门店不存在"),
    BOOKING_NOT_FOUND(false, "40060002", "预约不存在"),
    BOOKING_TIME_UNAVAILABLE(false, "40060003", "该时间段已被预约"),

    // ========== 订单相关错误 ==========
    ORDER_NOT_FOUND(false, "40070001", "订单不存在"),
    ORDER_STATUS_ERROR(false, "40070002", "订单状态错误"),
    PRODUCT_NOT_FOUND(false, "40070003", "商品不存在"),
    STOCK_INSUFFICIENT(false, "40070004", "商品库存不足"),
    CART_ITEM_NOT_FOUND(false, "40070005", "购物车商品不存在"),
    INVALID_QUANTITY(false, "40070006", "商品数量无效"),

    // ========== 优惠券相关错误 ==========
    COUPON_NOT_FOUND(false, "40080001", "优惠券不存在"),
    COUPON_ALREADY_RECEIVED(false, "40080002", "已经领取过该优惠券"),
    COUPON_EXHAUSTED(false, "40080003", "优惠券已领完"),
    COUPON_EXPIRED(false, "40080004", "优惠券已过期"),
    COUPON_NOT_AVAILABLE(false, "40080005", "优惠券不满足使用条件"),

    // ========== 医生相关错误 ==========
    DOCTOR_NOT_FOUND(false, "40090001", "医生不存在"),

    // ========== 咨询相关错误 ==========
    CONSULTATION_NOT_FOUND(false, "40100001", "咨询不存在"),
    CONSULTATION_STATUS_ERROR(false, "40100002", "咨询状态错误"),
    CONSULTATION_ALREADY_REVIEWED(false, "40100003", "该咨询已评价"),

    // ========== 会话相关错误 ==========
    CONVERSATION_NOT_FOUND(false, "40130001", "会话不存在"),

    // ========== 健康档案相关错误 ==========
    HEALTH_RECORD_NOT_FOUND(false, "40110001", "健康档案不存在"),

    // ========== 评价相关错误 ==========
    REVIEW_NOT_FOUND(false, "40120001", "评价不存在"),
    REVIEW_NO_PERMISSION(false, "40120002", "无权操作此评价"),
    REVIEW_ALREADY_EXISTS(false, "40120003", "该订单项已评价"),
    REVIEW_ALREADY_FOLLOW_UP(false, "40120004", "该评价已追评"),
    REVIEW_FOLLOW_UP_EXPIRED(false, "40120005", "超过30天无法追评"),
    REVIEW_RATING_INVALID(false, "40120006", "评分必须在1-5之间"),
    REVIEW_CONTENT_TOO_LONG(false, "40120007", "评价内容不能超过500字"),
    ORDER_ITEM_NOT_FOUND(false, "40120008", "订单项不存在"),
    ORDER_NO_PERMISSION(false, "40120009", "无权评价该订单"),
    ORDER_NOT_COMPLETED(false, "40120010", "只能评价已完成的订单"),
    PRODUCT_NOT_MATCH(false, "40120011", "商品信息不匹配"),

    // ========== 任务相关错误 ==========
    TASK_NOT_FOUND(false, "40140001", "任务不存在"),
    TASK_ALREADY_COMPLETED(false, "40140002", "任务已完成"),

    // ========== 健康提醒相关错误 ==========
    REMINDER_NOT_FOUND(false, "40150001", "提醒不存在"),

    // ========== 订阅相关错误 ==========
    SUBSCRIPTION_NOT_FOUND(false, "40160001", "订阅不存在或无权操作"),
    SUBSCRIPTION_CANNOT_PAUSE(false, "40160002", "仅正常状态的订阅可暂停"),
    SUBSCRIPTION_CANNOT_RESUME(false, "40160003", "仅暂停状态的订阅可恢复");

    private final boolean success;
    private final String code;
    private final String message;

    RespType(boolean success, String code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }
}
