package com.petcloud.user.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 会话VO
 *
 * @author luohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationVO {

    /**
     * 会话ID
     */
    private Long id;

    /**
     * 会话类型: ai_chat/doctor_consultation/customer_service
     */
    private String type;

    /**
     * 类型描述
     */
    private String typeDesc;

    /**
     * 目标ID
     */
    private Long targetId;

    /**
     * 目标名称
     */
    private String targetName;

    /**
     * 目标头像
     */
    private String targetAvatar;

    /**
     * 最后一条消息
     */
    private String lastMessage;

    /**
     * 最后消息时间
     */
    private Date lastMessageTime;

    /**
     * 格式化的时间
     */
    private String lastMessageTimeStr;

    /**
     * 未读消息数
     */
    private Integer unreadCount;

    /**
     * 是否置顶
     */
    private Boolean isPinned;

    /**
     * 跳转路径
     */
    private String navigateUrl;
}
