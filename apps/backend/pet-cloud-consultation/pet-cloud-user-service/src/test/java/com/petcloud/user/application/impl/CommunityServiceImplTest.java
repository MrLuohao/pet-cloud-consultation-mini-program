package com.petcloud.user.application.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petcloud.common.core.response.PageVO;
import com.petcloud.common.core.response.Response;
import com.petcloud.user.domain.dto.CommunityPostCreateDTO;
import com.petcloud.user.domain.entity.CommunityPost;
import com.petcloud.user.domain.entity.UserPet;
import com.petcloud.user.domain.entity.WxUser;
import com.petcloud.user.domain.vo.CommunityPostVO;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommunityServiceImplTest {

    @Mock
    private CommunityPostMapper communityPostMapper;
    @Mock
    private CommunityPostLikeMapper communityPostLikeMapper;
    @Mock
    private CommunityPostCollectMapper communityPostCollectMapper;
    @Mock
    private CommunityPostShareMapper communityPostShareMapper;
    @Mock
    private CommunityPostReportMapper communityPostReportMapper;
    @Mock
    private CommunityCommentMapper communityCommentMapper;
    @Mock
    private CommunityCommentLikeMapper communityCommentLikeMapper;
    @Mock
    private CommunityTopicMapper communityTopicMapper;
    @Mock
    private WxUserMapper wxUserMapper;
    @Mock
    private UserPetMapper userPetMapper;
    @Mock
    private UserFollowMapper userFollowMapper;
    @Mock
    private AiServiceClient aiServiceClient;
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private CommunityServiceImpl communityService;

    @Test
    void shouldCreatePostWithModeratedAssetIds() {
        CommunityPostCreateDTO dto = CommunityPostCreateDTO.builder()
                .content("今天状态不错")
                .mediaAssetIds(List.of(11L, 12L))
                .petId(101L)
                .build();

        MediaAssetVO first = new MediaAssetVO();
        first.setAssetId(11L);
        first.setUrl("https://img/1.jpg");
        first.setMediaType("image");
        first.setAvailableForSubmit(true);
        MediaAssetVO second = new MediaAssetVO();
        second.setAssetId(12L);
        second.setUrl("https://img/2.jpg");
        second.setMediaType("image");
        second.setAvailableForSubmit(true);

        when(aiServiceClient.getMediaAssets(any())).thenReturn(Response.succeed(List.of(first, second)));
        doAnswer(invocation -> {
            CommunityPost post = invocation.getArgument(0);
            post.setId(9001L);
            return 1;
        }).when(communityPostMapper).insert(any(CommunityPost.class));

        Long postId = communityService.createPost(1L, dto);

        ArgumentCaptor<CommunityPost> captor = ArgumentCaptor.forClass(CommunityPost.class);
        verify(communityPostMapper).insert(captor.capture());
        CommunityPost saved = captor.getValue();
        assertEquals(9001L, postId);
        assertEquals("image", saved.getMediaType());
        assertEquals("[\"https://img/1.jpg\",\"https://img/2.jpg\"]", saved.getMediaUrls());
    }

    @Test
    void shouldRejectCreatePostWhenAssetUnavailable() {
        CommunityPostCreateDTO dto = CommunityPostCreateDTO.builder()
                .content("今天状态不错")
                .mediaAssetIds(List.of(11L))
                .build();

        MediaAssetVO asset = new MediaAssetVO();
        asset.setAssetId(11L);
        asset.setAvailableForSubmit(false);
        asset.setReason("素材审核未通过");
        when(aiServiceClient.getMediaAssets(any())).thenReturn(Response.succeed(List.of(asset)));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> communityService.createPost(1L, dto));

        assertEquals("存在不可用的社区媒体: 素材审核未通过", exception.getMessage());
    }

    @Test
    void shouldBuildPetFirstFeedCard() {
        CommunityPost post = new CommunityPost();
        post.setId(9001L);
        post.setUserId(1L);
        post.setPetId(101L);
        post.setContent("今天终于把粮吃完了");
        post.setMediaUrls("[\"https://img/1.jpg\"]");
        post.setMediaType("image");
        post.setLikeCount(5);
        post.setCommentCount(3);
        post.setShareCount(1);
        post.setCollectCount(2);
        post.setIsDeleted(0);
        post.setCreateTime(java.sql.Timestamp.valueOf(LocalDateTime.now()));

        IPage<CommunityPost> page = new Page<>(1, 20);
        page.setRecords(List.of(post));
        page.setTotal(1L);

        WxUser user = new WxUser();
        user.setId(1L);
        user.setNickname("宠物主人");
        user.setAvatarUrl("https://avatar/user.png");

        UserPet pet = new UserPet();
        pet.setId(101L);
        pet.setName("团子");
        pet.setBreed("英短蓝猫");
        pet.setAvatarUrl("https://avatar/pet.png");
        pet.setBirthday(LocalDate.now().minusYears(2));
        pet.setMotto("慢热小猫");

        when(communityPostMapper.selectPage(any(), any())).thenReturn(page);
        when(wxUserMapper.selectBatchIds(any())).thenReturn(List.of(user));
        when(userPetMapper.selectBatchIds(any())).thenReturn(List.of(pet));

        PageVO<CommunityPostVO> result = communityService.getPosts(1, 20, null);

        assertEquals(1, result.getList().size());
        CommunityPostVO card = result.getList().get(0);
        assertEquals("pet_post", card.getPostType());
        assertNotNull(card.getAuthor());
        assertEquals("宠物主人", card.getAuthor().getDisplayName());
        assertNotNull(card.getPet());
        assertEquals("团子", card.getPet().getName());
        assertEquals("慢热小猫", card.getPet().getSignature());
    }
}
