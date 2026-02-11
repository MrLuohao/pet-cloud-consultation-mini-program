package com.petcloud.user.interfaces.controller;

import com.petcloud.common.core.response.Response;
import com.petcloud.common.web.utils.UserContextHolderWeb;
import com.petcloud.user.domain.service.ConversationService;
import com.petcloud.user.domain.vo.AiChatMessageVO;
import com.petcloud.user.domain.vo.ConversationVO;
import com.petcloud.user.domain.vo.MessageCenterVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会话控制器
 *
 * @author luohao
 */
@Slf4j
@RestController
@RequestMapping("/api/conversation")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;
    private final UserContextHolderWeb userContextHolderWeb;

    /**
     * 获取消息中心聚合数据
     */
    @GetMapping("/center")
    public Response<MessageCenterVO> getMessageCenter(HttpServletRequest request) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        log.info("获取消息中心数据, userId: {}", userId);
        MessageCenterVO center = conversationService.getMessageCenter(userId);
        return Response.succeed(center);
    }

    /**
     * 获取会话列表
     */
    @GetMapping("/list")
    public Response<List<ConversationVO>> getConversationList(HttpServletRequest request) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        log.info("获取会话列表, userId: {}", userId);
        List<ConversationVO> conversations = conversationService.getConversationList(userId);
        return Response.succeed(conversations);
    }

    /**
     * 获取或创建AI会话
     */
    @PostMapping("/ai")
    public Response<ConversationVO> getOrCreateAiConversation(HttpServletRequest request) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        log.info("获取或创建AI会话, userId: {}", userId);
        ConversationVO conversation = conversationService.getOrCreateAiConversation(userId);
        return Response.succeed(conversation);
    }

    /**
     * 获取会话详情
     */
    @GetMapping("/{id}")
    public Response<ConversationVO> getConversationDetail(HttpServletRequest request,
                                                          @PathVariable Long id) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        log.info("获取会话详情, userId: {}, conversationId: {}", userId, id);
        ConversationVO conversation = conversationService.getConversationDetail(userId, id);
        return Response.succeed(conversation);
    }

    /**
     * 标记会话已读
     */
    @PutMapping("/{id}/read")
    public Response<Void> markAsRead(HttpServletRequest request,
                                     @PathVariable Long id) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        log.info("标记会话已读, userId: {}, conversationId: {}", userId, id);
        conversationService.markAsRead(userId, id);
        return Response.succeed();
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/{id}")
    public Response<Void> deleteConversation(HttpServletRequest request,
                                             @PathVariable Long id) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        log.info("删除会话, userId: {}, conversationId: {}", userId, id);
        conversationService.deleteConversation(userId, id);
        return Response.succeed();
    }

    /**
     * 置顶/取消置顶会话
     */
    @PutMapping("/{id}/pin")
    public Response<Void> togglePin(HttpServletRequest request,
                                    @PathVariable Long id,
                                    @RequestParam Boolean pinned) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        log.info("会话置顶状态变更, userId: {}, conversationId: {}, pinned: {}", userId, id, pinned);
        conversationService.togglePin(userId, id, pinned);
        return Response.succeed();
    }

    /**
     * 获取AI聊天历史
     */
    @GetMapping("/{id}/history")
    public Response<List<AiChatMessageVO>> getAiChatHistory(HttpServletRequest request,
                                                            @PathVariable Long id) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        log.info("获取AI聊天历史, userId: {}, conversationId: {}", userId, id);
        List<AiChatMessageVO> history = conversationService.getAiChatHistory(userId, id);
        return Response.succeed(history);
    }
}
