package com.petcloud.user.interfaces.controller;

import com.petcloud.common.core.response.PageVO;
import com.petcloud.common.core.response.Response;
import com.petcloud.common.web.utils.UserContextHolderWeb;
import com.petcloud.user.domain.dto.PrivateMessageDTO;
import com.petcloud.user.domain.service.PrivateMessageService;
import com.petcloud.user.domain.vo.PrivateConversationVO;
import com.petcloud.user.domain.vo.PrivateMessageVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 私信控制器
 *
 * @author luohao
 */
@Slf4j
@RestController
@RequestMapping("/api/private-message")
@RequiredArgsConstructor
public class PrivateMessageController {

    private final PrivateMessageService privateMessageService;
    private final UserContextHolderWeb userContextHolder;

    /**
     * 发送私信（需登录）
     *
     * @param dto     私信DTO
     * @param request HttpServletRequest
     * @return 消息ID
     */
    @PostMapping("/send")
    public Response<Long> sendMessage(
            @Valid @RequestBody PrivateMessageDTO dto,
            HttpServletRequest request) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("发送私信，senderId: {}, receiverId: {}", userId, dto.getReceiverId());
        Long messageId = privateMessageService.sendMessage(userId, dto);
        return Response.succeed(messageId);
    }

    /**
     * 获取会话列表（需登录）
     *
     * @param page     页码
     * @param pageSize 每页数量
     * @param request  HttpServletRequest
     * @return 会话列表
     */
    @GetMapping("/conversations")
    public Response<PageVO<PrivateConversationVO>> getConversations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            HttpServletRequest request) {
        // 参数校验
        if (page < 1) page = 1;
        if (pageSize < 1 || pageSize > 50) pageSize = 20;

        Long userId = userContextHolder.getRequiredUserId(request);
        log.debug("获取会话列表，userId: {}, page: {}", userId, page);
        PageVO<PrivateConversationVO> result = privateMessageService.getConversations(userId, page, pageSize);
        return Response.succeed(result);
    }

    /**
     * 获取会话消息列表（需登录）
     *
     * @param conversationId 会话ID
     * @param page           页码
     * @param pageSize       每页数量
     * @param request        HttpServletRequest
     * @return 消息列表
     */
    @GetMapping("/conversation/{conversationId}")
    public Response<PageVO<PrivateMessageVO>> getMessages(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            HttpServletRequest request) {
        // 参数校验
        if (page < 1) page = 1;
        if (pageSize < 1 || pageSize > 50) pageSize = 20;

        Long userId = userContextHolder.getRequiredUserId(request);
        log.debug("获取会话消息，userId: {}, conversationId: {}, page: {}", userId, conversationId, page);
        PageVO<PrivateMessageVO> result = privateMessageService.getMessages(userId, conversationId, page, pageSize);
        return Response.succeed(result);
    }

    /**
     * 获取与某用户的消息列表（需登录）
     *
     * @param targetId 目标用户ID
     * @param page     页码
     * @param pageSize 每页数量
     * @param request  HttpServletRequest
     * @return 消息列表
     */
    @GetMapping("/chat/{targetId}")
    public Response<PageVO<PrivateMessageVO>> getMessagesWithUser(
            @PathVariable Long targetId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            HttpServletRequest request) {
        // 参数校验
        if (page < 1) page = 1;
        if (pageSize < 1 || pageSize > 50) pageSize = 20;

        Long userId = userContextHolder.getRequiredUserId(request);
        log.debug("获取与用户的消息，userId: {}, targetId: {}, page: {}", userId, targetId, page);
        PageVO<PrivateMessageVO> result = privateMessageService.getMessagesWithUser(userId, targetId, page, pageSize);
        return Response.succeed(result);
    }

    /**
     * 标记会话已读（需登录）
     *
     * @param conversationId 会话ID
     * @param request        HttpServletRequest
     * @return 操作结果
     */
    @PostMapping("/read/{conversationId}")
    public Response<Void> markAsRead(
            @PathVariable Long conversationId,
            HttpServletRequest request) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("标记会话已读，userId: {}, conversationId: {}", userId, conversationId);
        privateMessageService.markAsRead(userId, conversationId);
        return Response.succeed();
    }

    /**
     * 获取未读消息数（需登录）
     *
     * @param request HttpServletRequest
     * @return 未读消息数
     */
    @GetMapping("/unread-count")
    public Response<Map<String, Integer>> getUnreadCount(HttpServletRequest request) {
        Long userId = userContextHolder.getRequiredUserId(request);
        int count = privateMessageService.getUnreadCount(userId);
        Map<String, Integer> result = new HashMap<>();
        result.put("unreadCount", count);
        return Response.succeed(result);
    }

    /**
     * 删除会话（需登录）
     *
     * @param conversationId 会话ID
     * @param request        HttpServletRequest
     * @return 操作结果
     */
    @DeleteMapping("/conversation/{conversationId}")
    public Response<Void> deleteConversation(
            @PathVariable Long conversationId,
            HttpServletRequest request) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("删除会话，userId: {}, conversationId: {}", userId, conversationId);
        privateMessageService.deleteConversation(userId, conversationId);
        return Response.succeed();
    }
}
