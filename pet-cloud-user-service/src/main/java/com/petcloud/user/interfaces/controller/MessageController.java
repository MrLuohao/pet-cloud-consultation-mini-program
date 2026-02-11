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
 * 消息控制器
 *
 * @author luohao
 */
@Slf4j
@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final UserContextHolderWeb userContextHolder;

    /**
     * 获取消息列表（支持类型过滤）- BE-0.3
     *
     * @param type    消息类型：system/order/consultation/activity（可选，不传返回全部）
     * @param request HttpServletRequest
     */
    @GetMapping("/list")
    public Response<List<MessageVO>> getMessageList(
            @RequestParam(required = false) String type,
            HttpServletRequest request) {
        Long userId = userContextHolder.getCurrentUserId(request);
        log.info("获取消息列表，userId: {}, type: {}", userId, type);

        if (userId == null) {
            return Response.succeed(Collections.emptyList());
        }

        List<MessageVO> messages = messageService.getMessageList(userId, type);
        return Response.succeed(messages);
    }

    /**
     * 标记消息已读
     *
     * @param id 消息ID
     * @param request HttpServletRequest
     * @return 操作结果
     */
    @PutMapping("/{id}/read")
    public Response<Void> markAsRead(@PathVariable Long id, HttpServletRequest request) {
        Long userId = userContextHolder.getCurrentUserId(request);
        log.info("标记消息已读，messageId: {}, userId: {}", id, userId);

        // 未登录用户直接返回成功
        if (userId == null) {
            return Response.succeed();
        }

        messageService.markAsRead(id, userId);
        return Response.succeed();
    }

    /**
     * 全部标记已读
     *
     * @param request HttpServletRequest
     * @return 操作结果
     */
    @PutMapping("/read-all")
    public Response<Void> markAllAsRead(HttpServletRequest request) {
        Long userId = userContextHolder.getCurrentUserId(request);
        log.info("标记所有消息已读，userId: {}", userId);

        // 未登录用户直接返回成功
        if (userId == null) {
            return Response.succeed();
        }

        messageService.markAllAsRead(userId);
        return Response.succeed();
    }

    /**
     * 获取未读消息数量
     *
     * @param request HttpServletRequest
     * @return 未读数量
     */
    @GetMapping("/unread-count")
    public Response<Long> getUnreadCount(HttpServletRequest request) {
        Long userId = userContextHolder.getCurrentUserId(request);

        // 未登录用户返回0
        if (userId == null) {
            return Response.succeed(0L);
        }

        Long count = messageService.getUnreadCount(userId);
        return Response.succeed(count);
    }
}
