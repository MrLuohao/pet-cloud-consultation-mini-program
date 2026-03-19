package com.petcloud.user.interfaces.controller;

import com.petcloud.common.core.response.Response;
import com.petcloud.common.web.utils.UserContextHolderWeb;
import com.petcloud.user.domain.service.MessageService;
import com.petcloud.user.domain.vo.MessageVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * 消息通知别名控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/messages/notifications")
@RequiredArgsConstructor
public class MessageNotificationController {

    private final MessageService messageService;
    private final UserContextHolderWeb userContextHolder;

    @GetMapping
    public Response<List<MessageVO>> getMessageList(@RequestParam(required = false) String type,
                                                    HttpServletRequest request) {
        Long userId = userContextHolder.getCurrentUserId(request);
        log.info("获取消息通知列表, userId: {}, type: {}", userId, type);
        if (userId == null) {
            return Response.succeed(Collections.emptyList());
        }
        return Response.succeed(messageService.getMessageList(userId, type));
    }

    @PutMapping("/{id}/read")
    public Response<Void> markAsRead(@PathVariable Long id, HttpServletRequest request) {
        Long userId = userContextHolder.getCurrentUserId(request);
        log.info("标记消息通知已读, userId: {}, messageId: {}", userId, id);
        if (userId == null) {
            return Response.succeed();
        }
        messageService.markAsRead(id, userId);
        return Response.succeed();
    }

    @PutMapping("/read-all")
    public Response<Void> markAllAsRead(HttpServletRequest request) {
        Long userId = userContextHolder.getCurrentUserId(request);
        log.info("标记消息通知全部已读, userId: {}", userId);
        if (userId == null) {
            return Response.succeed();
        }
        messageService.markAllAsRead(userId);
        return Response.succeed();
    }

    @GetMapping("/unread-count")
    public Response<Long> getUnreadCount(HttpServletRequest request) {
        Long userId = userContextHolder.getCurrentUserId(request);
        log.info("获取消息通知未读数, userId: {}", userId);
        if (userId == null) {
            return Response.succeed(0L);
        }
        return Response.succeed(messageService.getUnreadCount(userId));
    }
}
