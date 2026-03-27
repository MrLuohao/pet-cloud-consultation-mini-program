package com.petcloud.user.interfaces.controller.content;

import com.petcloud.common.core.exception.RespType;
import com.petcloud.common.core.response.PageVO;
import com.petcloud.common.core.response.Response;
import com.petcloud.common.web.utils.UserContextHolderWeb;
import com.petcloud.user.domain.dto.CommunityPostCreateDTO;
import com.petcloud.user.domain.dto.CommunityPostUpdateDTO;
import com.petcloud.user.domain.dto.PostReportDTO;
import com.petcloud.user.domain.enums.CommunityPostQueryType;
import com.petcloud.user.domain.enums.CommunityShareType;
import com.petcloud.user.domain.enums.UserRespType;
import com.petcloud.user.domain.service.CommunityService;
import com.petcloud.user.domain.vo.CommunityCommentVO;
import com.petcloud.user.domain.vo.CommunityPostVO;
import com.petcloud.user.domain.vo.CommunityTopicVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Collections;

/**
 * 社区控制器
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

    // ========================= 帖子列表 =========================

    /**
     * 获取社区动态列表（公开接口，登录后显示点赞状态）
     *
     * @param page     页码，默认1
     * @param pageSize 每页数量，默认20，最大50
     * @param type     列表类型: latest-最新, hot-热门, following-关注
     * @param request  HttpServletRequest
     * @return 分页动态列表
     */
    @GetMapping("/posts")
    public Response<PageVO<CommunityPostVO>> getPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = CommunityPostQueryType.DEFAULT_CODE) String type,
            HttpServletRequest request) {
        // 参数校验
        if (page < 1) page = 1;
        if (pageSize < 1 || pageSize > 50) pageSize = 20;

        Long currentUserId = userContextHolder.getCurrentUserId(request);
        log.debug("获取社区动态列表，page: {}, pageSize: {}, type: {}", page, pageSize, type);

        CommunityPostQueryType queryType = CommunityPostQueryType.fromCode(type);
        PageVO<CommunityPostVO> result;
        switch (queryType) {
            case HOT:
                result = communityService.getHotPosts(page, pageSize, currentUserId);
                break;
            case FOLLOWING:
                if (currentUserId == null) {
                    return Response.of(UserRespType.LOGIN_REQUIRED);
                }
                result = communityService.getFollowingPosts(currentUserId, page, pageSize);
                break;
            default:
                result = communityService.getPosts(page, pageSize, currentUserId);
        }
        return Response.succeed(result);
    }

    @GetMapping("/feed")
    public Response<PageVO<CommunityPostVO>> getFeed(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = CommunityPostQueryType.DEFAULT_CODE) String type,
            HttpServletRequest request) {
        return getPosts(page, pageSize, type, request);
    }

    /**
     * 获取热门帖子列表
     *
     * @param page     页码
     * @param pageSize 每页数量
     * @param request  HttpServletRequest
     * @return 分页动态列表
     */
    @GetMapping("/posts/hot")
    public Response<PageVO<CommunityPostVO>> getHotPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            HttpServletRequest request) {
        Long currentUserId = userContextHolder.getCurrentUserId(request);
        log.debug("获取热门帖子列表，page: {}, pageSize: {}", page, pageSize);
        PageVO<CommunityPostVO> result = communityService.getHotPosts(page, pageSize, currentUserId);
        return Response.succeed(result);
    }

    /**
     * 搜索帖子
     *
     * @param keyword  搜索关键词
     * @param page     页码
     * @param pageSize 每页数量
     * @param request  HttpServletRequest
     * @return 分页动态列表
     */
    @GetMapping("/posts/search")
    public Response<PageVO<CommunityPostVO>> searchPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            HttpServletRequest request) {
        Long currentUserId = userContextHolder.getCurrentUserId(request);
        log.debug("搜索帖子，keyword: {}, page: {}, pageSize: {}", keyword, page, pageSize);
        PageVO<CommunityPostVO> result = communityService.searchPosts(keyword, page, pageSize, currentUserId);
        return Response.succeed(result);
    }

    /**
     * 获取话题下的帖子
     *
     * @param topicId  话题ID
     * @param page     页码
     * @param pageSize 每页数量
     * @param request  HttpServletRequest
     * @return 分页动态列表
     */
    @GetMapping("/topic/{topicId}/posts")
    public Response<PageVO<CommunityPostVO>> getTopicPosts(
            @PathVariable Long topicId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            HttpServletRequest request) {
        Long currentUserId = userContextHolder.getCurrentUserId(request);
        log.debug("获取话题帖子列表，topicId: {}, page: {}", topicId, page);
        PageVO<CommunityPostVO> result = communityService.getPostsByTopic(topicId, page, pageSize, currentUserId);
        return Response.succeed(result);
    }

    /**
     * 获取用户的帖子
     *
     * @param userId   用户ID
     * @param page     页码
     * @param pageSize 每页数量
     * @param request  HttpServletRequest
     * @return 分页动态列表
     */
    @GetMapping("/user/{userId}/posts")
    public Response<PageVO<CommunityPostVO>> getUserPosts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            HttpServletRequest request) {
        Long currentUserId = userContextHolder.getCurrentUserId(request);
        log.debug("获取用户帖子列表，userId: {}, page: {}", userId, page);
        PageVO<CommunityPostVO> result = communityService.getPostsByUser(userId, page, pageSize, currentUserId);
        return Response.succeed(result);
    }

    // ========================= 帖子详情/CRUD =========================

    /**
     * 获取帖子详情
     *
     * @param id      帖子ID
     * @param request HttpServletRequest
     * @return 帖子详情
     */
    @GetMapping("/post/{id}")
    public Response<CommunityPostVO> getPostDetail(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long currentUserId = userContextHolder.getCurrentUserId(request);
        log.debug("获取帖子详情，postId: {}, currentUserId: {}", id, currentUserId);
        CommunityPostVO post = communityService.getPostDetail(id, currentUserId);
        return Response.succeed(post);
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
        log.info("用户发布社区动态，userId: {}, content: {}, mediaType: {}, mediaUrls: {}, mediaAssetIds: {}",
                userId, dto.getContent(), dto.getMediaType(), dto.getMediaUrls(), dto.getMediaAssetIds());
        Long postId = communityService.createPost(userId, dto);
        return Response.succeed(postId);
    }

    /**
     * 更新社区动态（需登录）
     *
     * @param id      动态ID
     * @param dto     更新DTO
     * @param request HttpServletRequest
     * @return 操作结果
     */
    @PutMapping("/post/{id}")
    public Response<Void> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody CommunityPostUpdateDTO dto,
            HttpServletRequest request) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("用户更新社区动态，userId: {}, postId: {}", userId, id);
        communityService.updatePost(id, userId, dto);
        return Response.succeed();
    }

    /**
     * 删除社区动态（需登录）
     *
     * @param id      动态ID
     * @param request HttpServletRequest
     * @return 操作结果
     */
    @DeleteMapping("/post/{id}")
    public Response<Void> deletePost(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("用户删除社区动态，userId: {}, postId: {}", userId, id);
        communityService.deletePost(id, userId);
        return Response.succeed();
    }

    // ========================= 帖子互动 =========================

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
     * 收藏动态（需登录）
     *
     * @param id      动态ID
     * @param request HttpServletRequest
     * @return 操作结果
     */
    @PostMapping("/post/{id}/collect")
    public Response<Void> collectPost(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("用户收藏动态，userId: {}, postId: {}", userId, id);
        communityService.collectPost(id, userId);
        return Response.succeed();
    }

    /**
     * 取消收藏（需登录）
     *
     * @param id      动态ID
     * @param request HttpServletRequest
     * @return 操作结果
     */
    @DeleteMapping("/post/{id}/collect")
    public Response<Void> uncollectPost(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("用户取消收藏动态，userId: {}, postId: {}", userId, id);
        communityService.uncollectPost(id, userId);
        return Response.succeed();
    }

    /**
     * 记录分享（需登录）
     *
     * @param id      动态ID
     * @param body    请求体，包含 shareType 字段
     * @param request HttpServletRequest
     * @return 操作结果
     */
    @PostMapping("/post/{id}/share")
    public Response<Void> sharePost(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            HttpServletRequest request) {
        Long userId = userContextHolder.getRequiredUserId(request);
        String shareType = CommunityShareType.normalize(body != null ? body.get("shareType") : CommunityShareType.DEFAULT_CODE);
        log.info("用户分享动态，userId: {}, postId: {}, shareType: {}", userId, id, shareType);
        communityService.sharePost(id, userId, shareType);
        return Response.succeed();
    }

    /**
     * 举报动态（需登录）
     *
     * @param id      动态ID
     * @param dto     举报DTO
     * @param request HttpServletRequest
     * @return 操作结果
     */
    @PostMapping("/post/{id}/report")
    public Response<Void> reportPost(
            @PathVariable Long id,
            @Valid @RequestBody PostReportDTO dto,
            HttpServletRequest request) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("用户举报动态，userId: {}, postId: {}", userId, id);
        communityService.reportPost(id, userId, dto);
        return Response.succeed();
    }

    // ========================= 评论功能 =========================

    /**
     * 发表评论（需登录）
     *
     * @param id      动态ID
     * @param body    请求体，包含 content, replyToId, replyToUserId 字段
     * @param request HttpServletRequest
     * @return 评论ID
     */
    @PostMapping("/post/{id}/comment")
    public Response<Long> addComment(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        Long userId = userContextHolder.getRequiredUserId(request);
        String content = body.get("content") != null ? body.get("content").toString() : "";
        Long replyToId = body.get("replyToId") != null ? Long.valueOf(body.get("replyToId").toString()) : null;
        Long replyToUserId = body.get("replyToUserId") != null ? Long.valueOf(body.get("replyToUserId").toString()) : null;
        String mediaType = body.get("mediaType") != null ? body.get("mediaType").toString() : null;
        List<String> mediaUrls = Collections.emptyList();
        if (body.get("mediaUrls") instanceof List<?> rawList) {
            mediaUrls = rawList.stream().map(String::valueOf).toList();
        }
        log.info("用户发表评论，userId: {}, postId: {}", userId, id);
        Long commentId = communityService.replyComment(id, userId, content, replyToId, replyToUserId, mediaUrls, mediaType);
        return Response.succeed(commentId);
    }

    /**
     * 删除评论（需登录）
     *
     * @param id        动态ID
     * @param commentId 评论ID
     * @param request   HttpServletRequest
     * @return 操作结果
     */
    @DeleteMapping("/post/{id}/comment/{commentId}")
    public Response<Void> deleteComment(
            @PathVariable Long id,
            @PathVariable Long commentId,
            HttpServletRequest request) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("用户删除评论，userId: {}, commentId: {}", userId, commentId);
        communityService.deleteComment(commentId, userId);
        return Response.succeed();
    }

    /**
     * 获取动态评论列表（公开接口）
     *
     * @param id      动态ID
     * @param request HttpServletRequest
     * @return 评论列表
     */
    @GetMapping("/post/{id}/comments")
    public Response<List<CommunityCommentVO>> getComments(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long currentUserId = userContextHolder.getCurrentUserId(request);
        log.debug("获取动态评论列表，postId: {}", id);
        List<CommunityCommentVO> comments = communityService.getComments(id, currentUserId);
        return Response.succeed(comments);
    }

    /**
     * 点赞评论（需登录）
     *
     * @param id        动态ID
     * @param commentId 评论ID
     * @param request   HttpServletRequest
     * @return 操作结果
     */
    @PostMapping("/post/{id}/comment/{commentId}/like")
    public Response<Void> likeComment(
            @PathVariable Long id,
            @PathVariable Long commentId,
            HttpServletRequest request) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("用户点赞评论，userId: {}, commentId: {}", userId, commentId);
        communityService.likeComment(commentId, userId);
        return Response.succeed();
    }

    /**
     * 取消点赞评论（需登录）
     *
     * @param id        动态ID
     * @param commentId 评论ID
     * @param request   HttpServletRequest
     * @return 操作结果
     */
    @DeleteMapping("/post/{id}/comment/{commentId}/like")
    public Response<Void> unlikeComment(
            @PathVariable Long id,
            @PathVariable Long commentId,
            HttpServletRequest request) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("用户取消点赞评论，userId: {}, commentId: {}", userId, commentId);
        communityService.unlikeComment(commentId, userId);
        return Response.succeed();
    }

    // ========================= 话题功能 =========================

    /**
     * 获取热门话题列表
     *
     * @return 话题列表
     */
    @GetMapping("/topics")
    public Response<List<CommunityTopicVO>> getHotTopics() {
        log.debug("获取热门话题列表");
        List<CommunityTopicVO> topics = communityService.getHotTopics();
        return Response.succeed(topics);
    }

    /**
     * 搜索话题
     *
     * @param keyword 搜索关键词
     * @return 话题列表
     */
    @GetMapping("/topics/search")
    public Response<List<CommunityTopicVO>> searchTopics(@RequestParam String keyword) {
        log.debug("搜索话题，keyword: {}", keyword);
        List<CommunityTopicVO> topics = communityService.searchTopics(keyword);
        return Response.succeed(topics);
    }

    /**
     * 获取话题详情
     *
     * @param topicId 话题ID
     * @return 话题详情
     */
    @GetMapping("/topic/{topicId}")
    public Response<CommunityTopicVO> getTopicDetail(@PathVariable Long topicId) {
        log.debug("获取话题详情，topicId: {}", topicId);
        CommunityTopicVO topic = communityService.getTopicDetail(topicId);
        return Response.succeed(topic);
    }

    // ========================= 管理员功能 =========================

    /**
     * 置顶帖子（管理员）
     *
     * @param id 帖子ID
     * @return 操作结果
     */
    @PostMapping("/admin/post/{id}/pin")
    public Response<Void> pinPost(@PathVariable Long id) {
        log.info("置顶帖子，postId: {}", id);
        communityService.pinPost(id);
        return Response.succeed();
    }

    /**
     * 取消置顶（管理员）
     *
     * @param id 帖子ID
     * @return 操作结果
     */
    @DeleteMapping("/admin/post/{id}/pin")
    public Response<Void> unpinPost(@PathVariable Long id) {
        log.info("取消置顶帖子，postId: {}", id);
        communityService.unpinPost(id);
        return Response.succeed();
    }

    /**
     * 设置热门（管理员）
     *
     * @param id    帖子ID
     * @param isHot 是否热门
     * @return 操作结果
     */
    @PostMapping("/admin/post/{id}/hot")
    public Response<Void> setHot(
            @PathVariable Long id,
            @RequestParam boolean isHot) {
        log.info("设置帖子热门状态，postId: {}, isHot: {}", id, isHot);
        communityService.setHot(id, isHot);
        return Response.succeed();
    }
}
