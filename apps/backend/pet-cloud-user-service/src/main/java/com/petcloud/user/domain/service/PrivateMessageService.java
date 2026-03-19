package com.petcloud.user.domain.service;

import com.petcloud.common.core.response.PageVO;
import com.petcloud.user.domain.dto.PrivateMessageDTO;
import com.petcloud.user.domain.vo.PrivateConversationVO;
import com.petcloud.user.domain.vo.PrivateMessageVO;

/**
 * 私信服务接口
 *
 * @author luohao
 */
public interface PrivateMessageService {

    /**
     * 发送私信
     *
     * @param senderId 发送者ID
     * @param dto      私信DTO
     * @return 消息ID
     */
    Long sendMessage(Long senderId, PrivateMessageDTO dto);

    /**
     * 获取会话列表
     *
     * @param userId   用户ID
     * @param page     页码
     * @param pageSize 每页数量
     * @return 会话列表
     */
    PageVO<PrivateConversationVO> getConversations(Long userId, int page, int pageSize);

    /**
     * 获取会话消息列表
     *
     * @param userId         用户ID
     * @param conversationId 会话ID
     * @param page           页码
     * @param pageSize       每页数量
     * @return 消息列表
     */
    PageVO<PrivateMessageVO> getMessages(Long userId, Long conversationId, int page, int pageSize);

    /**
     * 获取与某用户的会话消息
     *
     * @param userId   当前用户ID
     * @param targetId 目标用户ID
     * @param page     页码
     * @param pageSize 每页数量
     * @return 消息列表
     */
    PageVO<PrivateMessageVO> getMessagesWithUser(Long userId, Long targetId, int page, int pageSize);

    /**
     * 标记会话已读
     *
     * @param userId         用户ID
     * @param conversationId 会话ID
     */
    void markAsRead(Long userId, Long conversationId);

    /**
     * 获取未读消息总数
     *
     * @param userId 用户ID
     * @return 未读消息数
     */
    int getUnreadCount(Long userId);

    /**
     * 删除会话
     *
     * @param userId         用户ID
     * @param conversationId 会话ID
     */
    void deleteConversation(Long userId, Long conversationId);
}
