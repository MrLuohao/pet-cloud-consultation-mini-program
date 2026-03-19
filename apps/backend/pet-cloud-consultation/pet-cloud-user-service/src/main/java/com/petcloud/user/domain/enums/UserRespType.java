package com.petcloud.user.domain.enums;

import com.petcloud.common.core.response.IRespType;
import lombok.Getter;

@Getter
public enum UserRespType implements IRespType {
    DIAGNOSIS_DEVICE_ID_REQUIRED(false, "41000001", "设备ID不能为空"),
    DIAGNOSIS_TASK_NOT_FOUND(false, "41000002", "诊断任务不存在"),
    FILE_UPLOAD_FAILED(false, "41000003", "文件上传失败: {}"),
    IMAGE_UPLOAD_FAILED(false, "41000004", "图片上传失败: {}"),
    MEDIA_UPLOAD_FAILED(false, "41000005", "媒体上传失败: {}"),
    VIP_PLAN_UNSUPPORTED(false, "41000006", "不支持的会员套餐类型: {}"),
    VIP_PAYMENT_NOT_CONFIRMED(false, "41000007", "请先完成支付"),
    WECHAT_LOGIN_CODE_REQUIRED(false, "41000008", "微信登录code不能为空"),
    WECHAT_LOGIN_CONFIG_MISSING(false, "41000009", "微信登录配置缺失"),
    WECHAT_LOGIN_EMPTY_RESPONSE(false, "41000010", "微信登录服务响应为空"),
    WECHAT_LOGIN_FAILED(false, "41000011", "微信登录失败，errCode={}"),
    WECHAT_LOGIN_OPENID_MISSING(false, "41000012", "微信登录失败，未获取到openid"),
    WECHAT_LOGIN_SERVICE_ERROR(false, "41000013", "微信登录服务异常"),
    LOGIN_REQUIRED(false, "41000014", "请先登录"),
    DIAGNOSIS_MEDIA_ASSET_INVALID(false, "41000015", "存在不可用的诊断图片: {}"),
    COMMUNITY_MEDIA_ASSET_INVALID(false, "41000016", "存在不可用的社区媒体: {}"),

    FOLLOW_SELF_FORBIDDEN(false, "41010001", "不能关注自己"),
    FOLLOW_TARGET_USER_NOT_FOUND(false, "41010002", "用户不存在"),

    PRIVATE_MESSAGE_SEND_TO_SELF_FORBIDDEN(false, "41020001", "不能给自己发送私信"),
    PRIVATE_MESSAGE_RECEIVER_NOT_FOUND(false, "41020002", "接收者不存在"),
    PRIVATE_CONVERSATION_NOT_FOUND(false, "41020003", "会话不存在"),
    PRIVATE_CONVERSATION_ACCESS_DENIED(false, "41020004", "无权访问该会话"),
    PRIVATE_CONVERSATION_DELETE_DENIED(false, "41020005", "无权删除该会话"),

    CHAT_QWEN_MAX_CALL_FAILED(false, "41030001", "QwenMax3调用失败: {}"),
    CHAT_DEEPSEEK_CALL_FAILED(false, "41030002", "DeepSeekV3调用失败: {}"),
    CHAT_AGENT_CALL_FAILED(false, "41030003", "Agent应用调用失败: {}"),

    COMMUNITY_POST_NOT_FOUND(false, "41040001", "动态不存在"),
    COMMUNITY_POST_UPDATE_FORBIDDEN(false, "41040002", "无权修改他人动态"),
    COMMUNITY_POST_DELETE_FORBIDDEN(false, "41040003", "无权删除他人动态"),
    COMMUNITY_POST_ALREADY_REPORTED(false, "41040004", "您已举报过该动态"),
    COMMUNITY_COMMENT_CONTENT_REQUIRED(false, "41040005", "评论内容不能为空"),
    COMMUNITY_COMMENT_NOT_FOUND(false, "41040006", "评论不存在"),
    COMMUNITY_COMMENT_DELETE_FORBIDDEN(false, "41040007", "无权删除他人评论"),
    COMMUNITY_TOPIC_NOT_FOUND(false, "41040008", "话题不存在");

    private final boolean success;
    private final String code;
    private final String message;

    UserRespType(boolean success, String code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }
}
