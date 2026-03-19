package com.petcloud.user.domain.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 私信会话VO
 *
 * @author luohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrivateConversationVO {

    /**
     * 会话ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 对方用户ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long targetUserId;

    /**
     * 对方昵称
     */
    private String targetNickname;

    /**
     * 对方头像
     */
    private String targetAvatarUrl;

    /**
     * 最后一条消息内容
     */
    private String lastMessage;

    /**
     * 最后消息时间（格式化字符串）
     */
    private String lastTime;

    /**
     * 未读消息数
     */
    private Integer unreadCount;
}
