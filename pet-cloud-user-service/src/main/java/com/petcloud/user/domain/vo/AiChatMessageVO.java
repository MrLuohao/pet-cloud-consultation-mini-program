package com.petcloud.user.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI聊天消息VO
 *
 * @author luohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatMessageVO {

    /**
     * 消息ID
     */
    private Long id;

    /**
     * 会话ID
     */
    private Long conversationId;

    /**
     * 角色: user/assistant
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 模型类型
     */
    private String modelType;

    /**
     * 创建时间
     */
    private String createTime;
}
