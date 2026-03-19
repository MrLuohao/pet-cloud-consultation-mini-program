package com.petcloud.user.interfaces.controller;

import com.petcloud.common.core.response.Response;
import com.petcloud.common.web.utils.UserContextHolderWeb;
import com.petcloud.user.domain.service.ConversationService;
import com.petcloud.user.domain.vo.ConversationVO;
import com.petcloud.user.domain.vo.MessageCenterVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 消息中心聚合控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageCenterController {

    private final ConversationService conversationService;
    private final UserContextHolderWeb userContextHolderWeb;

    @GetMapping("/center")
    public Response<MessageCenterVO> getCenter(HttpServletRequest request) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        log.info("获取消息中心聚合数据, userId: {}", userId);
        return Response.succeed(conversationService.getMessageCenter(userId));
    }

    @GetMapping("/conversations")
    public Response<List<ConversationVO>> getConversations(HttpServletRequest request) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        log.info("获取消息中心会话列表, userId: {}", userId);
        return Response.succeed(conversationService.getConversationList(userId));
    }

    @GetMapping("/unread-summary")
    public Response<MessageCenterVO.UnreadSummaryVO> getUnreadSummary(HttpServletRequest request) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        log.info("获取消息中心未读汇总, userId: {}", userId);
        return Response.succeed(conversationService.getMessageCenter(userId).getUnreadSummary());
    }

    @GetMapping("/notification-counts")
    public Response<MessageCenterVO.NotificationCounts> getNotificationCounts(HttpServletRequest request) {
        Long userId = userContextHolderWeb.getRequiredUserId(request);
        log.info("获取消息中心通知计数, userId: {}", userId);
        return Response.succeed(conversationService.getMessageCenter(userId).getNotificationCounts());
    }
}
