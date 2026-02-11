package com.petcloud.user.interfaces.controller;

import com.petcloud.common.core.response.PageVO;
import com.petcloud.common.core.response.Response;
import com.petcloud.common.web.utils.UserContextHolderWeb;
import com.petcloud.user.domain.dto.CommunityPostCreateDTO;
import com.petcloud.user.domain.service.CommunityService;
import com.petcloud.user.domain.vo.CommunityCommentVO;
import com.petcloud.user.domain.vo.CommunityPostVO;
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

import java.util.List;
import java.util.Map;

/**
 * 社区控制器 - BE-5.3
 *
 * @author luohao
 */
@Slf4j
@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;
    private final UserContextHolderWeb userContextHolder;

    /**
     * 获取社区动态列表（公开接口，登录后显示点赞状态）
     *
     * @param page     页码，默认1
     * @param pageSize 每页数量，默认20
     * @param request  HttpServletRequest
     * @return 分页动态列表
     */
    @GetMapping("/posts")
    public Response<PageVO<CommunityPostVO>> getPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            HttpServletRequest request) {
        // 公开接口，使用 getCurrentUserId 获取可选的用户ID
        Long currentUserId = userContextHolder.getCurrentUserId(request);
        log.info("获取社区动态列表，page: {}, pageSize: {}, currentUserId: {}", page, pageSize, currentUserId);
        PageVO<CommunityPostVO> result = communityService.getPosts(page, pageSize, currentUserId);
        return Response.succeed(result);
    }

    /**
     * 发布社区动态（需登录）
     *
     * @param dto     创建动态DTO
     * @param request HttpServletRequest
     * @return 动态ID
     */
    @PostMapping("/post")
    public Response<Long> createPost(
            @Valid @RequestBody CommunityPostCreateDTO dto,
            HttpServletRequest request) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("用户发布社区动态，userId: {}", userId);
        Long postId = communityService.createPost(userId, dto);
        return Response.succeed(postId);
    }

    /**
     * 点赞动态（需登录）
     *
     * @param id      动态ID
     * @param request HttpServletRequest
     * @return 操作结果
     */
    @PostMapping("/post/{id}/like")
    public Response<Void> likePost(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("用户点赞动态，userId: {}, postId: {}", userId, id);
        communityService.likePost(id, userId);
        return Response.succeed();
    }

    /**
     * 取消点赞（需登录）
     *
     * @param id      动态ID
     * @param request HttpServletRequest
     * @return 操作结果
     */
    @DeleteMapping("/post/{id}/like")
    public Response<Void> unlikePost(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("用户取消点赞动态，userId: {}, postId: {}", userId, id);
        communityService.unlikePost(id, userId);
        return Response.succeed();
    }

    /**
     * 发表评论（需登录）
     *
     * @param id      动态ID
     * @param body    请求体，包含 content 字段
     * @param request HttpServletRequest
     * @return 评论ID
     */
    @PostMapping("/post/{id}/comment")
    public Response<Long> addComment(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        Long userId = userContextHolder.getRequiredUserId(request);
        String content = body.get("content");
        log.info("用户发表评论，userId: {}, postId: {}", userId, id);
        Long commentId = communityService.addComment(id, userId, content);
        return Response.succeed(commentId);
    }

    /**
     * 获取动态评论列表（公开接口）
     *
     * @param id 动态ID
     * @return 评论列表
     */
    @GetMapping("/post/{id}/comments")
    public Response<List<CommunityCommentVO>> getComments(@PathVariable Long id) {
        log.info("获取动态评论列表，postId: {}", id);
        List<CommunityCommentVO> comments = communityService.getComments(id);
        return Response.succeed(comments);
    }
}
