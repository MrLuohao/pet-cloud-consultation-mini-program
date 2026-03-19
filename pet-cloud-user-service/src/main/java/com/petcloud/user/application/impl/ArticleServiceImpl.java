package com.petcloud.user.application.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.petcloud.common.core.exception.BusinessException;
import com.petcloud.common.core.exception.RespType;
import com.petcloud.common.core.response.PageVO;
import com.petcloud.user.domain.dto.ArticleCreateDTO;
import com.petcloud.user.domain.dto.CommentCreateDTO;
import com.petcloud.user.domain.entity.Article;
import com.petcloud.user.domain.entity.ArticleCollect;
import com.petcloud.user.domain.entity.ArticleComment;
import com.petcloud.user.domain.entity.ArticleLike;
import com.petcloud.user.domain.entity.WxUser;
import com.petcloud.user.domain.service.ArticleService;
import com.petcloud.user.domain.vo.ArticleCommentVO;
import com.petcloud.user.domain.vo.ArticleVO;
import com.petcloud.user.infrastructure.persistence.mapper.ArticleCollectMapper;
import com.petcloud.user.infrastructure.persistence.mapper.ArticleCommentMapper;
import com.petcloud.user.infrastructure.persistence.mapper.ArticleLikeMapper;
import com.petcloud.user.infrastructure.persistence.mapper.ArticleMapper;
import com.petcloud.user.infrastructure.persistence.mapper.WxUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 文章服务实现类
 *
 * @author luohao
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;
    private final ArticleLikeMapper articleLikeMapper;
    private final ArticleCollectMapper articleCollectMapper;
    private final ArticleCommentMapper articleCommentMapper;
    private final WxUserMapper wxUserMapper;

    @Override
    public List<ArticleVO> getArticleList() {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Article::getStatus, Article.Status.PUBLISHED.getCode())
                .orderByDesc(Article::getPublishTime);
        List<Article> articles = articleMapper.selectList(queryWrapper);
        return articles.stream()
                .map(article -> convertToVO(article, null))
                .collect(Collectors.toList());
    }

    @Override
    public List<ArticleVO> getArticleListByTag(String tag) {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Article::getStatus, Article.Status.PUBLISHED.getCode())
                .like(Article::getTag, tag)
                .orderByDesc(Article::getPublishTime);
        List<Article> articles = articleMapper.selectList(queryWrapper);
        return articles.stream()
                .map(article -> convertToVO(article, null))
                .collect(Collectors.toList());
    }

    @Override
    public PageVO<ArticleVO> getArticlePage(String tag, int page, int pageSize) {
        IPage<Article> pageObj = new Page<>(page, pageSize);
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Article::getStatus, Article.Status.PUBLISHED.getCode());
        if (tag != null && !tag.isEmpty()) {
            queryWrapper.like(Article::getTag, tag);
        }
        queryWrapper.orderByDesc(Article::getPublishTime);

        IPage<Article> result = articleMapper.selectPage(pageObj, queryWrapper);
        List<ArticleVO> list = result.getRecords().stream()
                .map(article -> convertToVO(article, null))
                .collect(Collectors.toList());

        log.debug("文章分页查询，tag: {}, page: {}, pageSize: {}, total: {}", tag, page, pageSize, result.getTotal());
        return PageVO.of(list, result.getTotal(), page, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createArticle(ArticleCreateDTO createDTO, Long userId) {
        // 创建文章实体
        Article article = new Article();
        article.setTitle(createDTO.getTitle());
        article.setCoverUrl(createDTO.getCoverUrl());
        article.setSummary(createDTO.getSummary());
        article.setContent(createDTO.getContent());
        article.setTag(createDTO.getTag());

        // 设置状态，默认为草稿
        Integer status = createDTO.getStatus();
        if (status == null) {
            status = Article.Status.DRAFT.getCode();
        }
        article.setStatus(status);

        // 设置初始统计值
        article.setViewCount(0);
        article.setLikeCount(0);
        article.setCollectCount(0);
        article.setCommentCount(0);

        // 如果是发布状态，设置发布时间
        if (status.equals(Article.Status.PUBLISHED.getCode())) {
            article.setPublishTime(new Date());
        }

        // 插入数据库（BaseEntity 的 creatorId 和 createTime 会自动填充）
        articleMapper.insert(article);

        log.info("创建文章成功，articleId: {}, userId: {}, title: {}",
                article.getId(), userId, createDTO.getTitle());

        return article.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleVO getArticleDetail(Long articleId, Long userId) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException(RespType.ARTICLE_NOT_FOUND);
        }

        // 增加浏览量
        article.setViewCount(article.getViewCount() + 1);
        articleMapper.updateById(article);

        return convertToVO(article, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void likeArticle(Long articleId, Long userId) {
        // 检查是否已点赞
        LambdaQueryWrapper<ArticleLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ArticleLike::getArticleId, articleId)
                .eq(ArticleLike::getUserId, userId);
        ArticleLike existLike = articleLikeMapper.selectOne(queryWrapper);

        if (existLike != null) {
            log.info("用户已点赞该文章，忽略重复请求，userId: {}, articleId: {}", userId, articleId);
            return;
        }

        // 添加点赞记录
        ArticleLike articleLike = new ArticleLike();
        articleLike.setArticleId(articleId);
        articleLike.setUserId(userId);
        articleLikeMapper.insert(articleLike);

        // 增加点赞数
        Article article = articleMapper.selectById(articleId);
        article.setLikeCount(article.getLikeCount() + 1);
        articleMapper.updateById(article);

        log.info("用户点赞文章，userId: {}, articleId: {}", userId, articleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void collectArticle(Long articleId, Long userId) {
        // 检查是否已收藏
        LambdaQueryWrapper<ArticleCollect> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ArticleCollect::getArticleId, articleId)
                .eq(ArticleCollect::getUserId, userId);
        ArticleCollect existCollect = articleCollectMapper.selectOne(queryWrapper);

        if (existCollect != null) {
            log.info("用户已收藏该文章，忽略重复请求，userId: {}, articleId: {}", userId, articleId);
            return;
        }

        // 添加收藏记录
        ArticleCollect articleCollect = new ArticleCollect();
        articleCollect.setArticleId(articleId);
        articleCollect.setUserId(userId);
        articleCollectMapper.insert(articleCollect);

        // 增加收藏数
        Article article = articleMapper.selectById(articleId);
        article.setCollectCount(article.getCollectCount() + 1);
        articleMapper.updateById(article);

        log.info("用户收藏文章，userId: {}, articleId: {}", userId, articleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlikeArticle(Long articleId, Long userId) {
        // 使用物理删除，绕过逻辑删除拦截器
        int deleted = articleLikeMapper.physicalDelete(articleId, userId);

        // 如果确实删除了记录，减少点赞数
        if (deleted > 0) {
            Article article = articleMapper.selectById(articleId);
            if (article != null && article.getLikeCount() > 0) {
                article.setLikeCount(article.getLikeCount() - 1);
                articleMapper.updateById(article);
            }
        }

        log.info("用户取消点赞，userId: {}, articleId: {}, deleted: {}", userId, articleId, deleted);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uncollectArticle(Long articleId, Long userId) {
        // 使用物理删除，绕过逻辑删除拦截器
        int deleted = articleCollectMapper.physicalDelete(articleId, userId);

        // 如果确实删除了记录，减少收藏数
        if (deleted > 0) {
            Article article = articleMapper.selectById(articleId);
            if (article != null && article.getCollectCount() > 0) {
                article.setCollectCount(article.getCollectCount() - 1);
                articleMapper.updateById(article);
            }
        }

        log.info("用户取消收藏，userId: {}, articleId: {}, deleted: {}", userId, articleId, deleted);
    }

    @Override
    public List<ArticleCommentVO> getCommentList(Long articleId, Long userId) {
        // 查询该文章的所有评论
        LambdaQueryWrapper<ArticleComment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ArticleComment::getArticleId, articleId)
                .orderByAsc(ArticleComment::getCreateTime);
        List<ArticleComment> allComments = articleCommentMapper.selectList(queryWrapper);
        Map<Long, WxUser> userMap = buildCommentUserMap(allComments);

        // 分离顶级评论和回复
        List<ArticleComment> topComments = new ArrayList<>();
        Map<Long, List<ArticleComment>> repliesMap = allComments.stream()
                .filter(c -> c.getParentId() != null)
                .collect(Collectors.groupingBy(ArticleComment::getParentId));

        for (ArticleComment comment : allComments) {
            if (comment.getParentId() == null) {
                topComments.add(comment);
            }
        }

        // 构建评论树
        return topComments.stream()
                .map(comment -> {
                    ArticleCommentVO vo = convertCommentToVO(comment, userId, userMap);
                    List<ArticleComment> replies = repliesMap.get(comment.getId());
                    if (replies != null && !replies.isEmpty()) {
                        vo.setReplies(replies.stream()
                                .map(r -> convertCommentToVO(r, userId, userMap))
                                .collect(Collectors.toList()));
                    }
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createComment(CommentCreateDTO createDTO, Long userId, String userNickname, String userAvatar) {
        // 检查文章是否存在
        Article article = articleMapper.selectById(createDTO.getArticleId());
        if (article == null) {
            throw new BusinessException(RespType.ARTICLE_NOT_FOUND);
        }

        // 创建评论实体
        ArticleComment comment = new ArticleComment();
        comment.setArticleId(createDTO.getArticleId());
        comment.setUserId(userId);
        comment.setUserNickname(userNickname);
        comment.setUserAvatar(userAvatar);
        comment.setContent(createDTO.getContent());
        comment.setParentId(createDTO.getParentId());
        comment.setReplyToUserId(createDTO.getReplyToUserId());
        comment.setLikeCount(0);

        // 如果是回复，查找被回复者的昵称
        if (createDTO.getReplyToUserId() != null) {
            LambdaQueryWrapper<ArticleComment> query = new LambdaQueryWrapper<>();
            query.eq(ArticleComment::getUserId, createDTO.getReplyToUserId())
                    .last("LIMIT 1");
            ArticleComment replyToComment = articleCommentMapper.selectOne(query);
            if (replyToComment != null) {
                comment.setReplyToNickname(replyToComment.getUserNickname());
            }
        }

        articleCommentMapper.insert(comment);

        // 增加文章评论数
        article.setCommentCount(article.getCommentCount() + 1);
        articleMapper.updateById(article);

        log.info("用户发表评论，userId: {}, articleId: {}, commentId: {}",
                userId, createDTO.getArticleId(), comment.getId());

        return comment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId, Long userId) {
        // 查找评论
        ArticleComment comment = articleCommentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(RespType.COMMENT_NOT_FOUND);
        }

        // 检查是否是评论作者
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(RespType.COMMENT_DELETE_FORBIDDEN);
        }

        // 删除评论
        articleCommentMapper.deleteById(commentId);

        // 减少文章评论数
        Article article = articleMapper.selectById(comment.getArticleId());
        if (article != null && article.getCommentCount() > 0) {
            article.setCommentCount(article.getCommentCount() - 1);
            articleMapper.updateById(article);
        }

        log.info("用户删除评论，userId: {}, commentId: {}", userId, commentId);
    }

    private ArticleCommentVO convertCommentToVO(ArticleComment comment, Long userId, Map<Long, WxUser> userMap) {
        WxUser user = comment.getUserId() != null ? userMap.get(comment.getUserId()) : null;
        String nickname = user != null && user.getNickname() != null ? user.getNickname() : comment.getUserNickname();
        String avatar = user != null && user.getAvatarUrl() != null ? user.getAvatarUrl() : comment.getUserAvatar();

        String replyToNickname = comment.getReplyToNickname();
        if (comment.getReplyToUserId() != null) {
            WxUser replyToUser = userMap.get(comment.getReplyToUserId());
            if (replyToUser != null && replyToUser.getNickname() != null) {
                replyToNickname = replyToUser.getNickname();
            }
        }

        return ArticleCommentVO.builder()
                .id(comment.getId())
                .articleId(comment.getArticleId())
                .userId(comment.getUserId())
                .userNickname(nickname)
                .userAvatar(avatar)
                .content(comment.getContent())
                .parentId(comment.getParentId())
                .replyToUserId(comment.getReplyToUserId())
                .replyToNickname(replyToNickname)
                .likeCount(comment.getLikeCount())
                .createTime(comment.getCreateTime())
                .isOwner(userId != null && userId.equals(comment.getUserId()))
                .build();
    }

    private Map<Long, WxUser> buildCommentUserMap(List<ArticleComment> comments) {
        if (comments == null || comments.isEmpty()) {
            return Map.of();
        }

        Set<Long> userIds = new HashSet<>();
        for (ArticleComment comment : comments) {
            if (comment.getUserId() != null) {
                userIds.add(comment.getUserId());
            }
            if (comment.getReplyToUserId() != null) {
                userIds.add(comment.getReplyToUserId());
            }
        }
        if (userIds.isEmpty()) {
            return Map.of();
        }

        return wxUserMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(WxUser::getId, u -> u, (a, b) -> a));
    }

    private ArticleVO convertToVO(Article article, Long userId) {
        // 检查是否已点赞
        boolean isLiked = false;
        boolean isCollected = false;

        if (userId != null) {
            LambdaQueryWrapper<ArticleLike> likeQuery = new LambdaQueryWrapper<>();
            likeQuery.eq(ArticleLike::getArticleId, article.getId())
                    .eq(ArticleLike::getUserId, userId);
            Long likeCount = articleLikeMapper.selectCount(likeQuery);
            // 防止 NPE：如果 selectCount 返回 null，视为 0
            isLiked = likeCount != null && likeCount > 0;

            LambdaQueryWrapper<ArticleCollect> collectQuery = new LambdaQueryWrapper<>();
            collectQuery.eq(ArticleCollect::getArticleId, article.getId())
                    .eq(ArticleCollect::getUserId, userId);
            Long collectCount = articleCollectMapper.selectCount(collectQuery);
            // 防止 NPE：如果 selectCount 返回 null，视为 0
            isCollected = collectCount != null && collectCount > 0;
        }

        return ArticleVO.builder()
                .id(article.getId())
                .title(article.getTitle())
                .coverUrl(article.getCoverUrl())
                .summary(article.getSummary())
                .content(article.getContent())
                .tag(article.getTag())
                .viewCount(article.getViewCount())
                .likeCount(article.getLikeCount())
                .collectCount(article.getCollectCount())
                .commentCount(article.getCommentCount())
                .publishTime(article.getPublishTime())
                .isLiked(isLiked)
                .isCollected(isCollected)
                .build();
    }
}
