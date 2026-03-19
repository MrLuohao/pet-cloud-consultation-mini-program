package com.petcloud.user.domain.service;

import com.petcloud.user.domain.vo.AiChatMessageVO;
import com.petcloud.user.domain.vo.ConversationVO;
import com.petcloud.user.domain.vo.MessageCenterVO;

import java.util.List;

/**
 * 会话服务接口
 *
 * @author luohao
 */
public interface ConversationService {

    /**
     * 获取消息中心聚合数据
     *
     * @param userId 用户ID
     * @return 消息中心数据
     */
    MessageCenterVO getMessageCenter(Long userId);

    /**
     * 获取会话列表
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    List<ConversationVO> getConversationList(Long userId);

    /**
     * 获取或创建AI会话
     *
     * @param userId 用户ID
     * @return 会话信息
     */
    ConversationVO getOrCreateAiConversation(Long userId);

    /**
     * 获取会话详情
     *
     * @param userId         用户ID
     * @param conversationId 会话ID
     * @return 会话详情
     */
    ConversationVO getConversationDetail(Long userId, Long conversationId);

    /**
     * 标记会话已读
     *
     * @param userId         用户ID
     * @param conversationId 会话ID
     */
    void markAsRead(Long userId, Long conversationId);

    /**
     * 删除会话
     *
     * @param userId         用户ID
     * @param conversationId 会话ID
     */
    void deleteConversation(Long userId, Long conversationId);

    /**
     * 置顶/取消置顶会话
     *
     * @param userId         用户ID
     * @param conversationId 会话ID
     * @param pinned         是否置顶
     */
    void togglePin(Long userId, Long conversationId, Boolean pinned);

    /**
     * 发送AI消息并更新会话
     *
     * @param userId         用户ID
     * @param conversationId 会话ID
     * @param content        消息内容
     * @param modelType      模型类型
     * @return AI回复内容
     */
    String sendAiMessage(Long userId, Long conversationId, String content, String modelType);

    /**
     * 获取AI聊天历史
     *
     * @param userId         用户ID
     * @param conversationId 会话ID
     * @return 聊天记录列表
     */
    List<AiChatMessageVO> getAiChatHistory(Long userId, Long conversationId);

    /**
     * 更新会话最后消息（内部方法，供ChatService调用）
     *
     * @param conversationId 会话ID
     * @param message        消息内容
     */
    void updateConversationLastMessage(Long conversationId, String message);
}
