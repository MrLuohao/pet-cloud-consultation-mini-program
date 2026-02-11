package com.petcloud.user.application.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petcloud.common.core.response.PageVO;
import com.petcloud.user.domain.dto.CommunityPostCreateDTO;
import com.petcloud.user.domain.entity.CommunityComment;
import com.petcloud.user.domain.entity.CommunityPost;
import com.petcloud.user.domain.entity.CommunityPostLike;
import com.petcloud.user.domain.entity.UserPet;
import com.petcloud.user.domain.entity.WxUser;
import com.petcloud.user.domain.service.CommunityService;
import com.petcloud.user.domain.vo.CommunityCommentVO;
import com.petcloud.user.domain.vo.CommunityPostVO;
import com.petcloud.user.infrastructure.persistence.mapper.CommunityCommentMapper;
import com.petcloud.user.infrastructure.persistence.mapper.CommunityPostLikeMapper;
import com.petcloud.user.infrastructure.persistence.mapper.CommunityPostMapper;
import com.petcloud.user.infrastructure.persistence.mapper.UserPetMapper;
import com.petcloud.user.infrastructure.persistence.mapper.WxUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
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

    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private final CommunityPostMapper communityPostMapper;
    private final CommunityPostLikeMapper communityPostLikeMapper;
    private final CommunityCommentMapper communityCommentMapper;
    private final WxUserMapper wxUserMapper;
    private final UserPetMapper userPetMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPost(Long userId, CommunityPostCreateDTO dto) {
        CommunityPost post = new CommunityPost();
        post.setUserId(userId);
        post.setContent(dto.getContent());
        post.setMediaType(dto.getMediaType());
        post.setPetId(dto.getPetId());
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setIsDeleted(0);

        // JSON 序列化 mediaUrls
        if (dto.getMediaUrls() != null && !dto.getMediaUrls().isEmpty()) {
            try {
                post.setMediaUrls(objectMapper.writeValueAsString(dto.getMediaUrls()));
            } catch (JsonProcessingException e) {
                log.warn("序列化 mediaUrls 失败，userId: {}", userId, e);
                post.setMediaUrls("[]");
            }
        } else {
            post.setMediaUrls("[]");
        }

        communityPostMapper.insert(post);
        log.info("用户发布社区动态成功，userId: {}, postId: {}", userId, post.getId());
        return post.getId();
    }

    @Override
    public PageVO<CommunityPostVO> getPosts(int page, int pageSize, Long currentUserId) {
        IPage<CommunityPost> pageObj = new Page<>(page, pageSize);
        LambdaQueryWrapper<CommunityPost> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommunityPost::getIsDeleted, 0)
                .orderByDesc(CommunityPost::getCreateTime);

        IPage<CommunityPost> result = communityPostMapper.selectPage(pageObj, queryWrapper);
        List<CommunityPost> posts = result.getRecords();

        if (posts.isEmpty()) {
            return PageVO.of(Collections.emptyList(), 0L, page, pageSize);
        }

        // 批量查询用户信息
        Set<Long> userIds = posts.stream().map(CommunityPost::getUserId).collect(Collectors.toSet());
        Map<Long, WxUser> userMap = wxUserMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(WxUser::getId, u -> u, (a, b) -> a));

        // 批量查询宠物信息
        Set<Long> petIds = posts.stream()
                .filter(p -> p.getPetId() != null)
                .map(CommunityPost::getPetId)
                .collect(Collectors.toSet());
        Map<Long, UserPet> petMap = petIds.isEmpty()
                ? Collections.emptyMap()
                : userPetMapper.selectBatchIds(petIds).stream()
                        .collect(Collectors.toMap(UserPet::getId, p -> p, (a, b) -> a));

        // 查询当前用户的点赞记录
        Set<Long> likedPostIds = new HashSet<>();
        if (currentUserId != null) {
            Set<Long> postIds = posts.stream().map(CommunityPost::getId).collect(Collectors.toSet());
            LambdaQueryWrapper<CommunityPostLike> likeQuery = new LambdaQueryWrapper<>();
            likeQuery.eq(CommunityPostLike::getUserId, currentUserId)
                    .in(CommunityPostLike::getPostId, postIds);
            communityPostLikeMapper.selectList(likeQuery).forEach(like -> likedPostIds.add(like.getPostId()));
        }

        List<CommunityPostVO> voList = posts.stream()
                .map(post -> convertPostToVO(post, userMap, petMap, likedPostIds))
                .collect(Collectors.toList());

        log.debug("分页获取社区动态，page: {}, pageSize: {}, total: {}", page, pageSize, result.getTotal());
        return PageVO.of(voList, result.getTotal(), page, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void likePost(Long postId, Long userId) {
        // 检查是否已点赞，重复则忽略
        LambdaQueryWrapper<CommunityPostLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommunityPostLike::getPostId, postId)
                .eq(CommunityPostLike::getUserId, userId);
        Long existCount = communityPostLikeMapper.selectCount(queryWrapper);
        if (existCount > 0) {
            log.info("用户已点赞该动态，忽略重复请求，userId: {}, postId: {}", userId, postId);
            return;
        }

        // 添加点赞记录
        CommunityPostLike like = new CommunityPostLike();
        like.setPostId(postId);
        like.setUserId(userId);
        communityPostLikeMapper.insert(like);

        // 增加动态点赞数
        CommunityPost post = communityPostMapper.selectById(postId);
        if (post != null) {
            post.setLikeCount(post.getLikeCount() + 1);
            communityPostMapper.updateById(post);
        }

        log.info("用户点赞动态，userId: {}, postId: {}", userId, postId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlikePost(Long postId, Long userId) {
        // 物理删除点赞记录，绕过逻辑删除拦截器
        int deleted = communityPostLikeMapper.physicalDelete(postId, userId);

        // 减少动态点赞数
        if (deleted > 0) {
            CommunityPost post = communityPostMapper.selectById(postId);
            if (post != null && post.getLikeCount() > 0) {
                post.setLikeCount(post.getLikeCount() - 1);
                communityPostMapper.updateById(post);
            }
        }

        log.info("用户取消点赞动态，userId: {}, postId: {}, deleted: {}", userId, postId, deleted);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addComment(Long postId, Long userId, String content) {
        CommunityComment comment = new CommunityComment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setIsDeleted(0);
        communityCommentMapper.insert(comment);

        // 增加动态评论数
        CommunityPost post = communityPostMapper.selectById(postId);
        if (post != null) {
            post.setCommentCount(post.getCommentCount() + 1);
            communityPostMapper.updateById(post);
        }

        log.info("用户发表评论，userId: {}, postId: {}, commentId: {}", userId, postId, comment.getId());
        return comment.getId();
    }

    @Override
    public List<CommunityCommentVO> getComments(Long postId) {
        LambdaQueryWrapper<CommunityComment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommunityComment::getPostId, postId)
                .eq(CommunityComment::getIsDeleted, 0)
                .orderByAsc(CommunityComment::getCreateTime);
        List<CommunityComment> comments = communityCommentMapper.selectList(queryWrapper);

        if (comments.isEmpty()) {
            return Collections.emptyList();
        }

        // 批量查询用户信息
        Set<Long> userIds = comments.stream().map(CommunityComment::getUserId).collect(Collectors.toSet());
        Map<Long, WxUser> userMap = wxUserMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(WxUser::getId, u -> u, (a, b) -> a));

        return comments.stream()
                .map(comment -> convertCommentToVO(comment, userMap))
                .collect(Collectors.toList());
    }

    // ----------------------------- 私有辅助方法 -----------------------------

    private CommunityPostVO convertPostToVO(CommunityPost post,
                                            Map<Long, WxUser> userMap,
                                            Map<Long, UserPet> petMap,
                                            Set<Long> likedPostIds) {
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

        List<String> mediaUrlList = parseMediaUrls(post.getMediaUrls());

        String createTimeStr = null;
        if (post.getCreateTime() != null) {
            createTimeStr = new SimpleDateFormat(DATE_PATTERN).format(post.getCreateTime());
        }

        return CommunityPostVO.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .nickname(nickname)
                .avatarUrl(avatarUrl)
                .content(post.getContent())
                .mediaUrls(mediaUrlList)
                .mediaType(post.getMediaType())
                .petName(petName)
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .isLiked(likedPostIds.contains(post.getId()))
                .createTime(createTimeStr)
                .build();
    }

    private CommunityCommentVO convertCommentToVO(CommunityComment comment, Map<Long, WxUser> userMap) {
        WxUser user = userMap.get(comment.getUserId());
        String nickname = user != null && StringUtils.hasText(user.getNickname())
                ? user.getNickname() : "匿名用户";
        String avatarUrl = user != null && StringUtils.hasText(user.getAvatarUrl())
                ? user.getAvatarUrl() : "";

        String createTimeStr = null;
        if (comment.getCreateTime() != null) {
            createTimeStr = new SimpleDateFormat(DATE_PATTERN).format(comment.getCreateTime());
        }

        return CommunityCommentVO.builder()
                .id(comment.getId())
                .userId(comment.getUserId())
                .nickname(nickname)
                .avatarUrl(avatarUrl)
                .content(comment.getContent())
                .createTime(createTimeStr)
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
}
