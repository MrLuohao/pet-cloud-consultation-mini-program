package com.petcloud.user.application.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petcloud.common.core.exception.BusinessException;
import com.petcloud.common.core.response.PageVO;
import com.petcloud.common.core.utils.DateUtils;
import com.petcloud.user.domain.dto.CommunityPostCreateDTO;
import com.petcloud.user.domain.dto.CommunityPostUpdateDTO;
import com.petcloud.user.domain.dto.PostReportDTO;
import com.petcloud.user.domain.enums.CommunityShareType;
import com.petcloud.user.domain.enums.UserRespType;
import com.petcloud.user.domain.entity.CommunityComment;
import com.petcloud.user.domain.entity.CommunityCommentLike;
import com.petcloud.user.domain.entity.CommunityPost;
import com.petcloud.user.domain.entity.CommunityPostCollect;
import com.petcloud.user.domain.entity.UserFollow;
import com.petcloud.user.domain.entity.CommunityPostLike;
import com.petcloud.user.domain.entity.CommunityPostReport;
import com.petcloud.user.domain.entity.CommunityPostShare;
import com.petcloud.user.domain.entity.CommunityTopic;
import com.petcloud.user.domain.entity.UserPet;
import com.petcloud.user.domain.entity.WxUser;
import com.petcloud.user.domain.service.CommunityService;
import com.petcloud.user.domain.vo.CommunityCommentVO;
import com.petcloud.user.domain.vo.CommunityPostVO;
import com.petcloud.user.domain.vo.CommunityTopicVO;
import com.petcloud.user.domain.dto.MediaAssetQueryRequest;
import com.petcloud.user.infrastructure.feign.AiServiceClient;
import com.petcloud.user.infrastructure.feign.dto.MediaAssetVO;
import com.petcloud.user.infrastructure.persistence.mapper.CommunityCommentLikeMapper;
import com.petcloud.user.infrastructure.persistence.mapper.CommunityCommentMapper;
import com.petcloud.user.infrastructure.persistence.mapper.CommunityPostCollectMapper;
import com.petcloud.user.infrastructure.persistence.mapper.CommunityPostLikeMapper;
import com.petcloud.user.infrastructure.persistence.mapper.CommunityPostMapper;
import com.petcloud.user.infrastructure.persistence.mapper.CommunityPostReportMapper;
import com.petcloud.user.infrastructure.persistence.mapper.CommunityPostShareMapper;
import com.petcloud.user.infrastructure.persistence.mapper.CommunityTopicMapper;
import com.petcloud.user.infrastructure.persistence.mapper.UserFollowMapper;
import com.petcloud.user.infrastructure.persistence.mapper.UserPetMapper;
import com.petcloud.user.infrastructure.persistence.mapper.WxUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.Period;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 社区服务实现类
 *
 * @author luohao
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityServiceImpl implements CommunityService {

    private final CommunityPostMapper communityPostMapper;
    private final CommunityPostLikeMapper communityPostLikeMapper;
    private final CommunityPostCollectMapper communityPostCollectMapper;
    private final CommunityPostShareMapper communityPostShareMapper;
    private final CommunityPostReportMapper communityPostReportMapper;
    private final CommunityCommentMapper communityCommentMapper;
    private final CommunityCommentLikeMapper communityCommentLikeMapper;
    private final CommunityTopicMapper communityTopicMapper;
    private final WxUserMapper wxUserMapper;
    private final UserPetMapper userPetMapper;
    private final UserFollowMapper userFollowMapper;
    private final AiServiceClient aiServiceClient;
    private final ObjectMapper objectMapper;

    // ========================= 帖子CRUD =========================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPost(Long userId, CommunityPostCreateDTO dto) {
        log.info("createPost 收到参数 - userId: {}, dto.mediaUrls: {}, dto.mediaType: {}",
                userId, dto.getMediaUrls(), dto.getMediaType());

        CommunityPost post = new CommunityPost();
        post.setUserId(userId);
        post.setContent(dto.getContent());
        post.setPetId(dto.getPetId());
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setShareCount(0);
        post.setCollectCount(0);
        post.setIsDeleted(0);
        post.setIsPinned(0);
        post.setIsHot(0);

        // 设置话题ID（取第一个话题）
        if (dto.getTopicIds() != null && !dto.getTopicIds().isEmpty()) {
            post.setTopicId(dto.getTopicIds().get(0));
        }

        CommunityMediaPayload mediaPayload = resolveMediaPayload(dto.getMediaAssetIds(), dto.getMediaUrls(), dto.getMediaType());
        post.setMediaType(mediaPayload.mediaType());
        post.setMediaUrls(writeMediaUrls(mediaPayload.mediaUrls(), userId));

        communityPostMapper.insert(post);
        log.info("用户发布社区动态成功，userId: {}, postId: {}, 最终mediaUrls: {}",
                userId, post.getId(), post.getMediaUrls());
        return post.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePost(Long postId, Long userId, CommunityPostUpdateDTO dto) {
        CommunityPost post = communityPostMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(UserRespType.COMMUNITY_POST_NOT_FOUND);
        }
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException(UserRespType.COMMUNITY_POST_UPDATE_FORBIDDEN);
        }

        // 更新字段
        if (dto.getContent() != null) {
            post.setContent(dto.getContent());
        }
        if (dto.getMediaUrls() != null || dto.getMediaAssetIds() != null || dto.getMediaType() != null) {
            CommunityMediaPayload mediaPayload = resolveMediaPayload(dto.getMediaAssetIds(), dto.getMediaUrls(), dto.getMediaType());
            post.setMediaType(mediaPayload.mediaType());
            post.setMediaUrls(writeMediaUrls(mediaPayload.mediaUrls(), userId));
        }
        if (dto.getPetId() != null) {
            post.setPetId(dto.getPetId());
        }
        if (dto.getTopicId() != null) {
            post.setTopicId(dto.getTopicId());
        }
        // 更新可见性设置
        if (dto.getVisibility() != null) {
            post.setVisibility(dto.getVisibility());
        }
        if (dto.getVisibleUserIds() != null) {
            try {
                post.setVisibleUserIds(objectMapper.writeValueAsString(dto.getVisibleUserIds()));
            } catch (JsonProcessingException e) {
                log.warn("序列化 visibleUserIds 失败", e);
            }
        }

        communityPostMapper.updateById(post);
        log.info("用户更新社区动态，userId: {}, postId: {}", userId, postId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePost(Long postId, Long userId) {
        CommunityPost post = communityPostMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(UserRespType.COMMUNITY_POST_NOT_FOUND);
        }
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException(UserRespType.COMMUNITY_POST_DELETE_FORBIDDEN);
        }

        post.setIsDeleted(1);
        communityPostMapper.updateById(post);
        log.info("用户删除社区动态，userId: {}, postId: {}", userId, postId);
    }

    // ========================= 帖子列表查询 =========================

    @Override
    public PageVO<CommunityPostVO> getPosts(int page, int pageSize, Long currentUserId) {
        IPage<CommunityPost> pageObj = new Page<>(page, pageSize);
        LambdaQueryWrapper<CommunityPost> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommunityPost::getIsDeleted, 0)
                .orderByDesc(CommunityPost::getIsPinned)
                .orderByDesc(CommunityPost::getCreateTime);

        IPage<CommunityPost> result = communityPostMapper.selectPage(pageObj, queryWrapper);
        return convertPostPageToVO(result, currentUserId);
    }

    @Override
    public PageVO<CommunityPostVO> getHotPosts(int page, int pageSize, Long currentUserId) {
        IPage<CommunityPost> pageObj = new Page<>(page, pageSize);
        LambdaQueryWrapper<CommunityPost> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommunityPost::getIsDeleted, 0)
                .eq(CommunityPost::getIsHot, 1)
                .orderByDesc(CommunityPost::getCreateTime);

        IPage<CommunityPost> result = communityPostMapper.selectPage(pageObj, queryWrapper);
        return convertPostPageToVO(result, currentUserId);
    }

    @Override
    public PageVO<CommunityPostVO> getFollowingPosts(Long userId, int page, int pageSize) {
        // 获取关注用户列表
        LambdaQueryWrapper<UserFollow> followQuery = new LambdaQueryWrapper<>();
        followQuery.eq(UserFollow::getFollowerId, userId);
        List<UserFollow> follows = userFollowMapper.selectList(followQuery);

        if (follows == null || follows.isEmpty()) {
            return PageVO.of(Collections.emptyList(), 0L, page, pageSize);
        }

        Set<Long> followingIds = follows.stream()
                .map(UserFollow::getFollowingId)
                .collect(Collectors.toSet());

        IPage<CommunityPost> pageObj = new Page<>(page, pageSize);
        LambdaQueryWrapper<CommunityPost> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommunityPost::getIsDeleted, 0)
                .in(CommunityPost::getUserId, followingIds)
                .orderByDesc(CommunityPost::getCreateTime);

        IPage<CommunityPost> result = communityPostMapper.selectPage(pageObj, queryWrapper);
        return convertPostPageToVO(result, userId);
    }

    @Override
    public PageVO<CommunityPostVO> searchPosts(String keyword, int page, int pageSize, Long currentUserId) {
        if (!StringUtils.hasText(keyword)) {
            return PageVO.of(Collections.emptyList(), 0L, page, pageSize);
        }

        IPage<CommunityPost> pageObj = new Page<>(page, pageSize);
        LambdaQueryWrapper<CommunityPost> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommunityPost::getIsDeleted, 0)
                .like(CommunityPost::getContent, keyword)
                .orderByDesc(CommunityPost::getCreateTime);

        IPage<CommunityPost> result = communityPostMapper.selectPage(pageObj, queryWrapper);
        return convertPostPageToVO(result, currentUserId);
    }

    @Override
    public PageVO<CommunityPostVO> getPostsByTopic(Long topicId, int page, int pageSize, Long currentUserId) {
        IPage<CommunityPost> pageObj = new Page<>(page, pageSize);
        LambdaQueryWrapper<CommunityPost> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommunityPost::getIsDeleted, 0)
                .eq(CommunityPost::getTopicId, topicId)
                .orderByDesc(CommunityPost::getCreateTime);

        IPage<CommunityPost> result = communityPostMapper.selectPage(pageObj, queryWrapper);
        return convertPostPageToVO(result, currentUserId);
    }

    @Override
    public PageVO<CommunityPostVO> getPostsByUser(Long userId, int page, int pageSize, Long currentUserId) {
        IPage<CommunityPost> pageObj = new Page<>(page, pageSize);
        LambdaQueryWrapper<CommunityPost> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommunityPost::getIsDeleted, 0)
                .eq(CommunityPost::getUserId, userId)
                .orderByDesc(CommunityPost::getCreateTime);

        IPage<CommunityPost> result = communityPostMapper.selectPage(pageObj, queryWrapper);
        return convertPostPageToVO(result, currentUserId);
    }

    @Override
    public CommunityPostVO getPostDetail(Long postId, Long currentUserId) {
        CommunityPost post = communityPostMapper.selectById(postId);
        if (post == null || post.getIsDeleted() == 1) {
            throw new BusinessException(UserRespType.COMMUNITY_POST_NOT_FOUND);
        }

        // 查询用户信息
        WxUser user = wxUserMapper.selectById(post.getUserId());

        // 查询宠物信息
        UserPet pet = null;
        if (post.getPetId() != null) {
            pet = userPetMapper.selectById(post.getPetId());
        }

        // 查询话题信息
        CommunityTopic topic = null;
        if (post.getTopicId() != null) {
            topic = communityTopicMapper.selectById(post.getTopicId());
        }

        // 查询点赞和收藏状态
        boolean isLiked = false;
        boolean isCollected = false;
        boolean isSelf = false;
        if (currentUserId != null) {
            LambdaQueryWrapper<CommunityPostLike> likeQuery = new LambdaQueryWrapper<>();
            likeQuery.eq(CommunityPostLike::getPostId, postId)
                    .eq(CommunityPostLike::getUserId, currentUserId);
            Long likeCount = communityPostLikeMapper.selectCount(likeQuery);
            isLiked = likeCount != null && likeCount > 0;

            LambdaQueryWrapper<CommunityPostCollect> collectQuery = new LambdaQueryWrapper<>();
            collectQuery.eq(CommunityPostCollect::getPostId, postId)
                    .eq(CommunityPostCollect::getUserId, currentUserId);
            Long collectCount = communityPostCollectMapper.selectCount(collectQuery);
            isCollected = collectCount != null && collectCount > 0;

            // 判断是否是自己的帖子
            isSelf = currentUserId.equals(post.getUserId());
        }

        return convertPostToDetailVO(post, user, pet, topic, isLiked, isCollected, isSelf);
    }

    // ========================= 互动功能 =========================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void likePost(Long postId, Long userId) {
        // 检查帖子是否存在
        CommunityPost post = communityPostMapper.selectById(postId);
        if (post == null || post.getIsDeleted() == 1) {
            throw new BusinessException(UserRespType.COMMUNITY_POST_NOT_FOUND);
        }

        // 检查是否已点赞
        LambdaQueryWrapper<CommunityPostLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommunityPostLike::getPostId, postId)
                .eq(CommunityPostLike::getUserId, userId);
        Long existCount = communityPostLikeMapper.selectCount(queryWrapper);
        if (existCount != null && existCount > 0) {
            log.info("用户已点赞该动态，忽略重复请求，userId: {}, postId: {}", userId, postId);
            return;
        }

        // 添加点赞记录
        CommunityPostLike like = new CommunityPostLike();
        like.setPostId(postId);
        like.setUserId(userId);
        communityPostLikeMapper.insert(like);

        // 增加动态点赞数
        LambdaUpdateWrapper<CommunityPost> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CommunityPost::getId, postId)
                .setSql("like_count = like_count + 1");
        communityPostMapper.update(null, updateWrapper);

        log.info("用户点赞动态，userId: {}, postId: {}", userId, postId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlikePost(Long postId, Long userId) {
        // 物理删除点赞记录
        int deleted = communityPostLikeMapper.physicalDelete(postId, userId);

        if (deleted > 0) {
            // 减少动态点赞数
            LambdaUpdateWrapper<CommunityPost> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(CommunityPost::getId, postId)
                    .gt(CommunityPost::getLikeCount, 0)
                    .setSql("like_count = like_count - 1");
            communityPostMapper.update(null, updateWrapper);
        }

        log.info("用户取消点赞动态，userId: {}, postId: {}, deleted: {}", userId, postId, deleted);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void collectPost(Long postId, Long userId) {
        // 检查帖子是否存在
        CommunityPost post = communityPostMapper.selectById(postId);
        if (post == null || post.getIsDeleted() == 1) {
            throw new BusinessException(UserRespType.COMMUNITY_POST_NOT_FOUND);
        }

        // 检查是否已收藏
        LambdaQueryWrapper<CommunityPostCollect> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommunityPostCollect::getPostId, postId)
                .eq(CommunityPostCollect::getUserId, userId);
        Long existCount = communityPostCollectMapper.selectCount(queryWrapper);
        if (existCount != null && existCount > 0) {
            log.info("用户已收藏该动态，忽略重复请求，userId: {}, postId: {}", userId, postId);
            return;
        }

        // 添加收藏记录
        CommunityPostCollect collect = new CommunityPostCollect();
        collect.setPostId(postId);
        collect.setUserId(userId);
        communityPostCollectMapper.insert(collect);

        // 增加动态收藏数
        LambdaUpdateWrapper<CommunityPost> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CommunityPost::getId, postId)
                .setSql("collect_count = collect_count + 1");
        communityPostMapper.update(null, updateWrapper);

        log.info("用户收藏动态，userId: {}, postId: {}", userId, postId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uncollectPost(Long postId, Long userId) {
        // 物理删除收藏记录
        int deleted = communityPostCollectMapper.physicalDelete(postId, userId);

        if (deleted > 0) {
            // 减少动态收藏数
            LambdaUpdateWrapper<CommunityPost> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(CommunityPost::getId, postId)
                    .gt(CommunityPost::getCollectCount, 0)
                    .setSql("collect_count = collect_count - 1");
            communityPostMapper.update(null, updateWrapper);
        }

        log.info("用户取消收藏动态，userId: {}, postId: {}, deleted: {}", userId, postId, deleted);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sharePost(Long postId, Long userId, String shareType) {
        // 检查帖子是否存在
        CommunityPost post = communityPostMapper.selectById(postId);
        if (post == null || post.getIsDeleted() == 1) {
            throw new BusinessException(UserRespType.COMMUNITY_POST_NOT_FOUND);
        }

        // 添加分享记录
        CommunityPostShare share = new CommunityPostShare();
        share.setPostId(postId);
        share.setUserId(userId);
        share.setShareType(CommunityShareType.normalize(shareType));
        communityPostShareMapper.insert(share);

        // 增加动态分享数
        LambdaUpdateWrapper<CommunityPost> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CommunityPost::getId, postId)
                .setSql("share_count = share_count + 1");
        communityPostMapper.update(null, updateWrapper);

        log.info("用户分享动态，userId: {}, postId: {}, shareType: {}", userId, postId, shareType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reportPost(Long postId, Long userId, PostReportDTO dto) {
        // 检查帖子是否存在
        CommunityPost post = communityPostMapper.selectById(postId);
        if (post == null || post.getIsDeleted() == 1) {
            throw new BusinessException(UserRespType.COMMUNITY_POST_NOT_FOUND);
        }

        // 检查是否已举报
        if (communityPostReportMapper.hasReported(postId, userId)) {
            throw new BusinessException(UserRespType.COMMUNITY_POST_ALREADY_REPORTED);
        }

        // 添加举报记录
        CommunityPostReport report = new CommunityPostReport();
        report.setPostId(postId);
        report.setUserId(userId);
        report.setReason(dto.getReason());
        report.setReasonType(dto.getReasonType());
        report.setStatus(0);
        communityPostReportMapper.insert(report);

        log.info("用户举报动态，userId: {}, postId: {}, reason: {}", userId, postId, dto.getReason());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pinPost(Long postId) {
        CommunityPost post = communityPostMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(UserRespType.COMMUNITY_POST_NOT_FOUND);
        }
        post.setIsPinned(1);
        communityPostMapper.updateById(post);
        log.info("置顶动态，postId: {}", postId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unpinPost(Long postId) {
        CommunityPost post = communityPostMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(UserRespType.COMMUNITY_POST_NOT_FOUND);
        }
        post.setIsPinned(0);
        communityPostMapper.updateById(post);
        log.info("取消置顶动态，postId: {}", postId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setHot(Long postId, boolean isHot) {
        CommunityPost post = communityPostMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(UserRespType.COMMUNITY_POST_NOT_FOUND);
        }
        post.setIsHot(isHot ? 1 : 0);
        communityPostMapper.updateById(post);
        log.info("设置动态热门状态，postId: {}, isHot: {}", postId, isHot);
    }

    // ========================= 评论功能 =========================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addComment(Long postId, Long userId, String content) {
        return replyComment(postId, userId, content, null, null, Collections.emptyList(), null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addComment(Long postId, Long userId, String content, List<String> mediaUrls, String mediaType) {
        return replyComment(postId, userId, content, null, null, mediaUrls, mediaType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long replyComment(Long postId, Long userId, String content, Long replyToId, Long replyToUserId) {
        return replyComment(postId, userId, content, replyToId, replyToUserId, Collections.emptyList(), null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long replyComment(Long postId, Long userId, String content, Long replyToId, Long replyToUserId, List<String> mediaUrls, String mediaType) {
        // 检查帖子是否存在
        CommunityPost post = communityPostMapper.selectById(postId);
        if (post == null || post.getIsDeleted() == 1) {
            throw new BusinessException(UserRespType.COMMUNITY_POST_NOT_FOUND);
        }

        boolean hasText = StringUtils.hasText(content);
        boolean hasMedia = mediaUrls != null && !mediaUrls.isEmpty();
        if (!hasText && !hasMedia) {
            throw new BusinessException(UserRespType.COMMUNITY_COMMENT_CONTENT_REQUIRED);
        }

        CommunityComment comment = new CommunityComment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(hasText ? content : "");
        comment.setReplyToId(replyToId);
        comment.setReplyToUserId(replyToUserId);
        comment.setMediaType(hasMedia ? mediaType : null);
        if (hasMedia) {
            try {
                comment.setMediaUrls(objectMapper.writeValueAsString(mediaUrls));
            } catch (JsonProcessingException e) {
                log.warn("评论媒体URLs序列化失败，postId: {}, userId: {}", postId, userId, e);
                comment.setMediaUrls("[]");
            }
        } else {
            comment.setMediaUrls("[]");
        }
        comment.setLikeCount(0);
        comment.setIsDeleted(0);
        communityCommentMapper.insert(comment);

        // 增加动态评论数
        LambdaUpdateWrapper<CommunityPost> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CommunityPost::getId, postId)
                .setSql("comment_count = comment_count + 1");
        communityPostMapper.update(null, updateWrapper);

        log.info("用户发表评论，userId: {}, postId: {}, commentId: {}", userId, postId, comment.getId());
        return comment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId, Long userId) {
        CommunityComment comment = communityCommentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(UserRespType.COMMUNITY_COMMENT_NOT_FOUND);
        }
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(UserRespType.COMMUNITY_COMMENT_DELETE_FORBIDDEN);
        }

        comment.setIsDeleted(1);
        communityCommentMapper.updateById(comment);

        // 减少动态评论数
        LambdaUpdateWrapper<CommunityPost> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CommunityPost::getId, comment.getPostId())
                .gt(CommunityPost::getCommentCount, 0)
                .setSql("comment_count = comment_count - 1");
        communityPostMapper.update(null, updateWrapper);

        log.info("用户删除评论，userId: {}, commentId: {}", userId, commentId);
    }

    @Override
    public List<CommunityCommentVO> getComments(Long postId, Long currentUserId) {
        LambdaQueryWrapper<CommunityComment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommunityComment::getPostId, postId)
                .eq(CommunityComment::getIsDeleted, 0)
                .orderByAsc(CommunityComment::getCreateTime);
        List<CommunityComment> comments = communityCommentMapper.selectList(queryWrapper);

        if (comments.isEmpty()) {
            return Collections.emptyList();
        }

        // 批量查询用户信息
        Set<Long> userIds = new HashSet<>();
        comments.forEach(c -> {
            userIds.add(c.getUserId());
            if (c.getReplyToUserId() != null) {
                userIds.add(c.getReplyToUserId());
            }
        });
        Map<Long, WxUser> userMap = wxUserMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(WxUser::getId, u -> u, (a, b) -> a));

        // 查询点赞状态
        Set<Long> likedCommentIds = new HashSet<>();
        if (currentUserId != null) {
            Set<Long> commentIds = comments.stream().map(CommunityComment::getId).collect(Collectors.toSet());
            LambdaQueryWrapper<CommunityCommentLike> likeQuery = new LambdaQueryWrapper<>();
            likeQuery.eq(CommunityCommentLike::getUserId, currentUserId)
                    .in(CommunityCommentLike::getCommentId, commentIds);
            communityCommentLikeMapper.selectList(likeQuery).forEach(like -> likedCommentIds.add(like.getCommentId()));
        }

        return comments.stream()
                .map(comment -> convertCommentToVO(comment, userMap, likedCommentIds, currentUserId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void likeComment(Long commentId, Long userId) {
        // 检查评论是否存在
        CommunityComment comment = communityCommentMapper.selectById(commentId);
        if (comment == null || comment.getIsDeleted() == 1) {
            throw new BusinessException(UserRespType.COMMUNITY_COMMENT_NOT_FOUND);
        }

        // 检查是否已点赞
        LambdaQueryWrapper<CommunityCommentLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommunityCommentLike::getCommentId, commentId)
                .eq(CommunityCommentLike::getUserId, userId);
        Long existCount = communityCommentLikeMapper.selectCount(queryWrapper);
        if (existCount != null && existCount > 0) {
            return;
        }

        // 添加点赞记录
        CommunityCommentLike like = new CommunityCommentLike();
        like.setCommentId(commentId);
        like.setUserId(userId);
        communityCommentLikeMapper.insert(like);

        // 增加评论点赞数
        LambdaUpdateWrapper<CommunityComment> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CommunityComment::getId, commentId)
                .setSql("like_count = like_count + 1");
        communityCommentMapper.update(null, updateWrapper);

        log.info("用户点赞评论，userId: {}, commentId: {}", userId, commentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlikeComment(Long commentId, Long userId) {
        int deleted = communityCommentLikeMapper.physicalDelete(commentId, userId);

        if (deleted > 0) {
            LambdaUpdateWrapper<CommunityComment> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(CommunityComment::getId, commentId)
                    .gt(CommunityComment::getLikeCount, 0)
                    .setSql("like_count = like_count - 1");
            communityCommentMapper.update(null, updateWrapper);
        }

        log.info("用户取消点赞评论，userId: {}, commentId: {}", userId, commentId);
    }

    // ========================= 话题功能 =========================

    @Override
    public List<CommunityTopicVO> getHotTopics() {
        LambdaQueryWrapper<CommunityTopic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommunityTopic::getIsHot, 1)
                .orderByDesc(CommunityTopic::getPostCount)
                .last("LIMIT 20");
        List<CommunityTopic> topics = communityTopicMapper.selectList(queryWrapper);
        return topics.stream().map(this::convertTopicToVO).collect(Collectors.toList());
    }

    @Override
    public List<CommunityTopicVO> searchTopics(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<CommunityTopic> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(CommunityTopic::getName, keyword)
                .orderByDesc(CommunityTopic::getPostCount)
                .last("LIMIT 20");
        List<CommunityTopic> topics = communityTopicMapper.selectList(queryWrapper);
        return topics.stream().map(this::convertTopicToVO).collect(Collectors.toList());
    }

    @Override
    public CommunityTopicVO getTopicDetail(Long topicId) {
        CommunityTopic topic = communityTopicMapper.selectById(topicId);
        if (topic == null) {
            throw new BusinessException(UserRespType.COMMUNITY_TOPIC_NOT_FOUND);
        }
        return convertTopicToVO(topic);
    }

    // ========================= 私有辅助方法 =========================

    private PageVO<CommunityPostVO> convertPostPageToVO(IPage<CommunityPost> result, Long currentUserId) {
        List<CommunityPost> posts = result.getRecords();
        if (posts.isEmpty()) {
            return PageVO.of(Collections.emptyList(), 0L, (int) result.getCurrent(), (int) result.getSize());
        }

        // 可见性过滤
        List<CommunityPost> visiblePosts = posts.stream()
                .filter(post -> isPostVisibleToUser(post, currentUserId))
                .collect(Collectors.toList());

        if (visiblePosts.isEmpty()) {
            return PageVO.of(Collections.emptyList(), 0L, (int) result.getCurrent(), (int) result.getSize());
        }

        // 批量查询用户信息
        Set<Long> userIds = visiblePosts.stream().map(CommunityPost::getUserId).collect(Collectors.toSet());
        Map<Long, WxUser> userMap = wxUserMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(WxUser::getId, u -> u, (a, b) -> a));

        // 批量查询宠物信息
        Set<Long> petIds = visiblePosts.stream()
                .filter(p -> p.getPetId() != null)
                .map(CommunityPost::getPetId)
                .collect(Collectors.toSet());
        Map<Long, UserPet> petMap = petIds.isEmpty()
                ? Collections.emptyMap()
                : userPetMapper.selectBatchIds(petIds).stream()
                        .collect(Collectors.toMap(UserPet::getId, p -> p, (a, b) -> a));

        // 批量查询话题信息
        Set<Long> topicIds = visiblePosts.stream()
                .filter(p -> p.getTopicId() != null)
                .map(CommunityPost::getTopicId)
                .collect(Collectors.toSet());
        Map<Long, CommunityTopic> topicMap = topicIds.isEmpty()
                ? Collections.emptyMap()
                : communityTopicMapper.selectBatchIds(topicIds).stream()
                        .collect(Collectors.toMap(CommunityTopic::getId, t -> t, (a, b) -> a));

        // 查询点赞和收藏状态
        Set<Long> likedPostIds = new HashSet<>();
        Set<Long> collectedPostIds = new HashSet<>();
        if (currentUserId != null) {
            Set<Long> postIds = visiblePosts.stream().map(CommunityPost::getId).collect(Collectors.toSet());

            LambdaQueryWrapper<CommunityPostLike> likeQuery = new LambdaQueryWrapper<>();
            likeQuery.eq(CommunityPostLike::getUserId, currentUserId)
                    .in(CommunityPostLike::getPostId, postIds);
            communityPostLikeMapper.selectList(likeQuery).forEach(like -> likedPostIds.add(like.getPostId()));

            LambdaQueryWrapper<CommunityPostCollect> collectQuery = new LambdaQueryWrapper<>();
            collectQuery.eq(CommunityPostCollect::getUserId, currentUserId)
                    .in(CommunityPostCollect::getPostId, postIds);
            communityPostCollectMapper.selectList(collectQuery).forEach(collect -> collectedPostIds.add(collect.getPostId()));
        }

        List<CommunityPostVO> voList = visiblePosts.stream()
                .map(post -> convertPostToVO(post, userMap, petMap, topicMap, likedPostIds, collectedPostIds, currentUserId))
                .collect(Collectors.toList());

        return PageVO.of(voList, result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
    }

    /**
     * 判断帖子是否对当前用户可见
     */
    private boolean isPostVisibleToUser(CommunityPost post, Long currentUserId) {
        Integer visibility = post.getVisibility();
        if (visibility == null || visibility == 0) {
            // 公开，所有人可见
            return true;
        }

        if (visibility == 2) {
            // 仅自己可见
            return currentUserId != null && currentUserId.equals(post.getUserId());
        }

        if (visibility == 1) {
            // 指定人可见
            // 自己发的帖子自己肯定可见
            if (currentUserId != null && currentUserId.equals(post.getUserId())) {
                return true;
            }
            // 检查是否在可见列表中
            List<Long> visibleUserIds = parseVisibleUserIds(post.getVisibleUserIds());
            return currentUserId != null && visibleUserIds.contains(currentUserId);
        }

        return true;
    }

    private CommunityPostVO convertPostToVO(CommunityPost post,
                                            Map<Long, WxUser> userMap,
                                            Map<Long, UserPet> petMap,
                                            Map<Long, CommunityTopic> topicMap,
                                            Set<Long> likedPostIds,
                                            Set<Long> collectedPostIds,
                                            Long currentUserId) {
        WxUser user = userMap.get(post.getUserId());
        String nickname = user != null && StringUtils.hasText(user.getNickname())
                ? user.getNickname() : "匿名用户";
        String avatarUrl = user != null && StringUtils.hasText(user.getAvatarUrl())
                ? user.getAvatarUrl() : "";

        String petName = null;
        if (post.getPetId() != null) {
            UserPet pet = petMap.get(post.getPetId());
            if (pet != null) {
                petName = pet.getName();
            }
        }

        String topicName = null;
        if (post.getTopicId() != null) {
            CommunityTopic topic = topicMap.get(post.getTopicId());
            if (topic != null) {
                topicName = topic.getName();
            }
        }

        List<String> mediaUrlList = parseMediaUrls(post.getMediaUrls());

        String createTimeStr = DateUtils.format(post.getCreateTime());

        // 判断是否是自己的帖子
        boolean isSelf = currentUserId != null && currentUserId.equals(post.getUserId());

        return CommunityPostVO.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .postType(resolvePostType(petName))
                .nickname(nickname)
                .avatarUrl(avatarUrl)
                .author(buildAuthor(post.getUserId(), nickname, avatarUrl))
                .content(post.getContent())
                .mediaUrls(mediaUrlList)
                .mediaType(post.getMediaType())
                .petName(petName)
                .pet(buildPetIdentity(post.getPetId(), petMap.get(post.getPetId())))
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .shareCount(post.getShareCount())
                .collectCount(post.getCollectCount())
                .isLiked(likedPostIds.contains(post.getId()))
                .isCollected(collectedPostIds.contains(post.getId()))
                .isSelf(isSelf)
                .visibility(post.getVisibility() != null ? post.getVisibility() : 0)
                .visibleUserIds(parseVisibleUserIds(post.getVisibleUserIds()))
                .topicId(post.getTopicId())
                .topicName(topicName)
                .isPinned(Integer.valueOf(1).equals(post.getIsPinned()))
                .isHot(Integer.valueOf(1).equals(post.getIsHot()))
                .createTime(createTimeStr)
                .build();
    }

    private CommunityPostVO convertPostToDetailVO(CommunityPost post, WxUser user, UserPet pet,
                                                   CommunityTopic topic, boolean isLiked, boolean isCollected, boolean isSelf) {
        String nickname = user != null && StringUtils.hasText(user.getNickname())
                ? user.getNickname() : "匿名用户";
        String avatarUrl = user != null && StringUtils.hasText(user.getAvatarUrl())
                ? user.getAvatarUrl() : "";

        String petName = pet != null ? pet.getName() : null;
        String topicName = topic != null ? topic.getName() : null;

        List<String> mediaUrlList = parseMediaUrls(post.getMediaUrls());

        // 解析可见用户ID并获取昵称
        List<Long> visibleUserIds = parseVisibleUserIds(post.getVisibleUserIds());
        List<String> visibleUserNames = null;
        if (visibleUserIds != null && !visibleUserIds.isEmpty()) {
            List<WxUser> visibleUsers = wxUserMapper.selectBatchIds(visibleUserIds);
            visibleUserNames = visibleUsers.stream()
                    .map(u -> u.getNickname() != null ? u.getNickname() : "用户")
                    .collect(Collectors.toList());
        }

        return CommunityPostVO.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .postType(resolvePostType(petName))
                .nickname(nickname)
                .avatarUrl(avatarUrl)
                .author(buildAuthor(post.getUserId(), nickname, avatarUrl))
                .content(post.getContent())
                .mediaUrls(mediaUrlList)
                .mediaType(post.getMediaType())
                .petName(petName)
                .pet(buildPetIdentity(post.getPetId(), pet))
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .shareCount(post.getShareCount())
                .collectCount(post.getCollectCount())
                .isLiked(isLiked)
                .isCollected(isCollected)
                .isSelf(isSelf)
                .visibility(post.getVisibility() != null ? post.getVisibility() : 0)
                .visibleUserIds(visibleUserIds)
                .visibleUserNames(visibleUserNames)
                .topicId(post.getTopicId())
                .topicName(topicName)
                .isPinned(Integer.valueOf(1).equals(post.getIsPinned()))
                .isHot(Integer.valueOf(1).equals(post.getIsHot()))
                .createTime(DateUtils.format(post.getCreateTime()))
                .build();
    }

    private CommunityCommentVO convertCommentToVO(CommunityComment comment,
                                                   Map<Long, WxUser> userMap,
                                                   Set<Long> likedCommentIds,
                                                   Long currentUserId) {
        WxUser user = userMap.get(comment.getUserId());
        String nickname = user != null && StringUtils.hasText(user.getNickname())
                ? user.getNickname() : "匿名用户";
        String avatarUrl = user != null && StringUtils.hasText(user.getAvatarUrl())
                ? user.getAvatarUrl() : "";

        String replyToNickname = null;
        if (comment.getReplyToUserId() != null) {
            WxUser replyToUser = userMap.get(comment.getReplyToUserId());
            if (replyToUser != null && StringUtils.hasText(replyToUser.getNickname())) {
                replyToNickname = replyToUser.getNickname();
            }
        }

        // 判断是否是自己的评论
        boolean isSelf = currentUserId != null && currentUserId.equals(comment.getUserId());

        return CommunityCommentVO.builder()
                .id(comment.getId())
                .userId(comment.getUserId())
                .nickname(nickname)
                .avatarUrl(avatarUrl)
                .replyToId(comment.getReplyToId())
                .replyToUserId(comment.getReplyToUserId())
                .replyToNickname(replyToNickname)
                .content(comment.getContent())
                .mediaUrls(parseMediaUrls(comment.getMediaUrls()))
                .mediaType(comment.getMediaType())
                .likeCount(comment.getLikeCount())
                .isLiked(likedCommentIds.contains(comment.getId()))
                .isSelf(isSelf)
                .createTime(DateUtils.format(comment.getCreateTime()))
                .build();
    }

    private CommunityTopicVO convertTopicToVO(CommunityTopic topic) {
        return CommunityTopicVO.builder()
                .id(topic.getId())
                .name(topic.getName())
                .icon(topic.getIcon())
                .description(topic.getDescription())
                .coverUrl(topic.getCoverUrl())
                .postCount(topic.getPostCount())
                .isHot(topic.getIsHot() == 1)
                .build();
    }

    private List<String> parseMediaUrls(String mediaUrlsJson) {
        if (!StringUtils.hasText(mediaUrlsJson)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(mediaUrlsJson, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.warn("解析 mediaUrls JSON 失败: {}", mediaUrlsJson, e);
            return Collections.emptyList();
        }
    }

    /**
     * 解析可见用户ID列表
     */
    private List<Long> parseVisibleUserIds(String visibleUserIdsJson) {
        if (!StringUtils.hasText(visibleUserIdsJson)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(visibleUserIdsJson, new TypeReference<List<Long>>() {});
        } catch (JsonProcessingException e) {
            log.warn("解析 visibleUserIds JSON 失败: {}", visibleUserIdsJson, e);
            return Collections.emptyList();
        }
    }

    private String writeMediaUrls(List<String> mediaUrls, Long userId) {
        if (mediaUrls == null || mediaUrls.isEmpty()) {
            return "[]";
        }
        try {
            String mediaUrlsJson = objectMapper.writeValueAsString(mediaUrls);
            log.info("序列化 mediaUrls 成功: {}", mediaUrlsJson);
            return mediaUrlsJson;
        } catch (JsonProcessingException e) {
            log.warn("序列化 mediaUrls 失败，userId: {}", userId, e);
            return "[]";
        }
    }

    private CommunityMediaPayload resolveMediaPayload(List<Long> mediaAssetIds, List<String> fallbackMediaUrls, String fallbackMediaType) {
        if (mediaAssetIds == null || mediaAssetIds.isEmpty()) {
            return new CommunityMediaPayload(
                    fallbackMediaUrls == null ? Collections.emptyList() : fallbackMediaUrls,
                    fallbackMediaType
            );
        }

        MediaAssetQueryRequest request = new MediaAssetQueryRequest();
        request.setAssetIds(mediaAssetIds);
        List<MediaAssetVO> assets = aiServiceClient.getMediaAssets(request).getData();
        if (assets == null || assets.size() != mediaAssetIds.size()) {
            throw new BusinessException(UserRespType.COMMUNITY_MEDIA_ASSET_INVALID, "素材不存在或已失效");
        }

        Map<Long, MediaAssetVO> assetMap = assets.stream()
                .collect(Collectors.toMap(MediaAssetVO::getAssetId, item -> item, (a, b) -> a));

        List<MediaAssetVO> orderedAssets = mediaAssetIds.stream()
                .map(assetMap::get)
                .toList();

        String invalidReason = orderedAssets.stream()
                .filter(asset -> asset == null || !Boolean.TRUE.equals(asset.getAvailableForSubmit()))
                .map(asset -> asset == null ? "素材不存在或已失效" : (StringUtils.hasText(asset.getReason()) ? asset.getReason() : "素材审核未通过"))
                .findFirst()
                .orElse(null);
        if (invalidReason != null) {
            throw new BusinessException(UserRespType.COMMUNITY_MEDIA_ASSET_INVALID, invalidReason);
        }

        List<String> mediaUrls = orderedAssets.stream()
                .map(MediaAssetVO::getUrl)
                .filter(StringUtils::hasText)
                .toList();
        if (fallbackMediaUrls != null && !fallbackMediaUrls.isEmpty()) {
            mediaUrls = java.util.stream.Stream.concat(
                    fallbackMediaUrls.stream().filter(StringUtils::hasText),
                    mediaUrls.stream()
            ).distinct().toList();
        }
        String mediaType = orderedAssets.stream()
                .map(MediaAssetVO::getMediaType)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse(fallbackMediaType);

        return new CommunityMediaPayload(mediaUrls, mediaType);
    }

    private String resolvePostType(String petName) {
        return StringUtils.hasText(petName) ? "pet_post" : "author_post";
    }

    private CommunityPostVO.AuthorVO buildAuthor(Long userId, String nickname, String avatarUrl) {
        return CommunityPostVO.AuthorVO.builder()
                .userId(userId)
                .displayName(nickname)
                .avatarUrl(avatarUrl)
                .role("user")
                .build();
    }

    private CommunityPostVO.PetIdentityVO buildPetIdentity(Long petId, UserPet pet) {
        if (pet == null) {
            return null;
        }
        return CommunityPostVO.PetIdentityVO.builder()
                .petId(petId)
                .name(pet.getName())
                .avatarUrl(pet.getAvatarUrl())
                .breed(pet.getBreed())
                .ageText(buildAgeText(pet))
                .signature(buildPetSignature(pet))
                .build();
    }

    private String buildAgeText(UserPet pet) {
        LocalDate birthday = pet.getBirthday();
        if (birthday == null) {
            return "年龄待补充";
        }
        Period period = Period.between(birthday, LocalDate.now());
        if (period.getYears() > 0) {
            return period.getYears() + "岁" + Math.max(period.getMonths(), 0) + "个月";
        }
        if (period.getMonths() > 0) {
            return period.getMonths() + "个月";
        }
        return "幼年期";
    }

    private String buildPetSignature(UserPet pet) {
        if (StringUtils.hasText(pet.getMotto())) {
            return pet.getMotto();
        }
        if (StringUtils.hasText(pet.getPersonality())) {
            return pet.getPersonality();
        }
        return "记录日常状态，慢慢认识彼此。";
    }

    private record CommunityMediaPayload(List<String> mediaUrls, String mediaType) {
    }
}
