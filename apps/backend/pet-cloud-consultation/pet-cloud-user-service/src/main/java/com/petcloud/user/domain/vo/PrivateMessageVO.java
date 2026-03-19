package com.petcloud.user.domain.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 私信消息VO
 *
 * @author luohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrivateMessageVO {

    /**
     * 消息ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 会话ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long conversationId;

    /**
     * 发送者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long senderId;

    /**
     * 发送者昵称
     */
    private String senderNickname;

    /**
     * 发送者头像
     */
    private String senderAvatarUrl;

    /**
     * 接收者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long receiverId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型: text,image,voice
     */
    private String msgType;

    /**
     * 是否已读
     */
    private Boolean isRead;

    /**
     * 是否是自己发送的
     */
    private Boolean isSelf;

    /**
     * 创建时间（格式化字符串）
     */
    private String createTime;
}
