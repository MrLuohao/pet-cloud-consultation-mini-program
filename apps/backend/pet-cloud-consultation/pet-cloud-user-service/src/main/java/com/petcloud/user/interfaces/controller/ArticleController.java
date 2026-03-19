package com.petcloud.user.interfaces.controller;

import com.petcloud.common.core.response.PageVO;
import com.petcloud.common.core.response.Response;
import com.petcloud.common.web.utils.UserContextHolderWeb;
import com.petcloud.user.domain.dto.ArticleCreateDTO;
import com.petcloud.user.domain.dto.CommentCreateDTO;
import com.petcloud.user.domain.entity.WxUser;
import com.petcloud.user.domain.service.ArticleService;
import com.petcloud.user.domain.vo.ArticleCommentVO;
import com.petcloud.user.domain.vo.ArticleVO;
import com.petcloud.user.infrastructure.persistence.mapper.WxUserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文章控制器
 *
 * @author luohao
 */
@Slf4j
@RestController
@RequestMapping("/api/article")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;
    private final UserContextHolderWeb userContextHolder;
    private final WxUserMapper wxUserMapper;

    /**
     * 获取文章列表（分页）- BE-0.2
     *
     * @param tag      标签筛选（可选）
     * @param page     页码，默认1
     * @param pageSize 每页数量，默认10
     */
    @GetMapping("/list")
    public Response<PageVO<ArticleVO>> getArticleList(
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        log.info("获取文章列表, tag: {}, page: {}, pageSize: {}", tag, page, pageSize);
        PageVO<ArticleVO> result = articleService.getArticlePage(tag, page, pageSize);
        return Response.succeed(result);
    }

    /**
     * 新增文章
     *
     * @param createDTO 新增文章DTO
     * @param request HttpServletRequest
     * @return 文章ID
     */
    @PostMapping("/create")
    public Response<Long> createArticle(
            @Valid @RequestBody ArticleCreateDTO createDTO,
            HttpServletRequest request) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("新增文章，userId: {}, title: {}", userId, createDTO.getTitle());
        Long articleId = articleService.createArticle(createDTO, userId);
        return Response.succeed(articleId);
    }

    /**
     * 获取文章详情（公开接口，登录后显示点赞/收藏状态）
     *
     * @param id 文章ID
     * @param request HttpServletRequest
     * @return 文章详情
     */
    @GetMapping("/{id}")
    public Response<ArticleVO> getArticleDetail(
            @PathVariable Long id,
            HttpServletRequest request) {
        log.info("获取文章详情，articleId: {}", id);
        // 公开接口，使用 getCurrentUserId 获取可选的用户ID
        Long userId = userContextHolder.getCurrentUserId(request);
        log.info("获取文章详情，userId: {}", userId);
        ArticleVO article = articleService.getArticleDetail(id, userId);
        return Response.succeed(article);
    }

    /**
     * 点赞文章
     *
     * @param id 文章ID
     * @param request HttpServletRequest
     * @return 操作结果
     */
    @PostMapping("/{id}/like")
    public Response<Void> likeArticle(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("点赞文章，articleId: {}, userId: {}", id, userId);
        articleService.likeArticle(id, userId);
        return Response.succeed();
    }

    /**
     * 取消点赞
     *
     * @param id 文章ID
     * @param request HttpServletRequest
     * @return 操作结果
     */
    @DeleteMapping("/{id}/like")
    public Response<Void> unlikeArticle(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("取消点赞，articleId: {}, userId: {}", id, userId);
        articleService.unlikeArticle(id, userId);
        return Response.succeed();
    }

    /**
     * 收藏文章
     *
     * @param id 文章ID
     * @param request HttpServletRequest
     * @return 操作结果
     */
    @PostMapping("/{id}/collect")
    public Response<Void> collectArticle(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("收藏文章，articleId: {}, userId: {}", id, userId);
        articleService.collectArticle(id, userId);
        return Response.succeed();
    }

    /**
     * 取消收藏
     *
     * @param id 文章ID
     * @param request HttpServletRequest
     * @return 操作结果
     */
    @DeleteMapping("/{id}/collect")
    public Response<Void> uncollectArticle(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("取消收藏，articleId: {}, userId: {}", id, userId);
        articleService.uncollectArticle(id, userId);
        return Response.succeed();
    }

    /**
     * 获取文章评论列表（公开接口）
     *
     * @param id 文章ID
     * @param request HttpServletRequest
     * @return 评论列表
     */
    @GetMapping("/{id}/comments")
    public Response<List<ArticleCommentVO>> getCommentList(
            @PathVariable Long id,
            HttpServletRequest request) {
        // 公开接口，使用 getCurrentUserId 获取可选的用户ID
        Long userId = userContextHolder.getCurrentUserId(request);
        log.info("获取文章评论，articleId: {}, userId: {}", id, userId);
        List<ArticleCommentVO> comments = articleService.getCommentList(id, userId);
        return Response.succeed(comments);
    }

    /**
     * 发表评论
     *
     * @param createDTO 评论内容
     * @param request HttpServletRequest
     * @return 评论ID
     */
    @PostMapping("/comment")
    public Response<Long> createComment(
            @Valid @RequestBody CommentCreateDTO createDTO,
            HttpServletRequest request) {
        Long userId = userContextHolder.getRequiredUserId(request);

        // 获取用户真实信息
        String userNickname = "匿名用户";
        String userAvatar = "";
        WxUser wxUser = wxUserMapper.selectById(userId);
        if (wxUser != null) {
            userNickname = wxUser.getNickname() != null ? wxUser.getNickname() : "匿名用户";
            userAvatar = wxUser.getAvatarUrl() != null ? wxUser.getAvatarUrl() : "";
        }

        log.info("发表评论，articleId: {}, userId: {}, nickname: {}", createDTO.getArticleId(), userId, userNickname);
        Long commentId = articleService.createComment(createDTO, userId, userNickname, userAvatar);
        return Response.succeed(commentId);
    }

    /**
     * 删除评论
     *
     * @param commentId 评论ID
     * @param request HttpServletRequest
     * @return 操作结果
     */
    @DeleteMapping("/comment/{commentId}")
    public Response<Void> deleteComment(
            @PathVariable Long commentId,
            HttpServletRequest request) {
        Long userId = userContextHolder.getRequiredUserId(request);
        log.info("删除评论，commentId: {}, userId: {}", commentId, userId);
        articleService.deleteComment(commentId, userId);
        return Response.succeed();
    }
}
