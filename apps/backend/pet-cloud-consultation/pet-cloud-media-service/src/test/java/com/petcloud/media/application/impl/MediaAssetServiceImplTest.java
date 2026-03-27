package com.petcloud.media.application.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petcloud.media.domain.dto.MediaAssetQueryRequest;
import com.petcloud.media.domain.dto.RegisterMediaAssetRequest;
import com.petcloud.media.domain.entity.MediaAsset;
import com.petcloud.media.domain.enums.MediaModerationStatus;
import com.petcloud.media.domain.vo.MediaAssetVO;
import com.petcloud.media.infrastructure.persistence.mapper.MediaAssetMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MediaAssetServiceImplTest {

    @Mock
    private MediaAssetMapper mediaAssetMapper;

    @InjectMocks
    private MediaAssetServiceImpl mediaAssetService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mediaAssetService, "objectMapper", new ObjectMapper());
    }

    @Test
    void shouldRegisterSafeMediaAsset() {
        doAnswer(invocation -> {
            MediaAsset asset = invocation.getArgument(0);
            asset.setId(101L);
            return 1;
        }).when(mediaAssetMapper).insert(any(MediaAsset.class));

        RegisterMediaAssetRequest request = new RegisterMediaAssetRequest();
        request.setMimeType("image/jpeg");
        request.setOriginalFilename("pet_photo.jpg");
        request.setUrl("https://cdn/pet_photo.jpg");

        MediaAssetVO result = mediaAssetService.registerMediaAsset(request);

        assertEquals(101L, result.getAssetId());
        assertEquals(MediaModerationStatus.PASS.getCode(), result.getModerationStatus());
        assertTrue(result.getAvailableForSubmit());
    }

    @Test
    void shouldReturnReviewStatusForUnclearMedia() {
        doAnswer(invocation -> {
            MediaAsset asset = invocation.getArgument(0);
            asset.setId(102L);
            return 1;
        }).when(mediaAssetMapper).insert(any(MediaAsset.class));

        RegisterMediaAssetRequest request = new RegisterMediaAssetRequest();
        request.setMimeType("image/jpeg");
        request.setOriginalFilename("unclear_scan.jpg");
        request.setUrl("https://cdn/unclear_scan.jpg");

        MediaAssetVO result = mediaAssetService.registerMediaAsset(request);

        assertEquals(MediaModerationStatus.REVIEW.getCode(), result.getModerationStatus());
        assertFalse(result.getAvailableForSubmit());
    }

    @Test
    void shouldQueryExistingAssets() {
        MediaAsset asset = new MediaAsset();
        asset.setId(201L);
        asset.setAssetNo("asset_201");
        asset.setUrl("https://cdn/asset_201.jpg");
        asset.setMediaType("image");
        asset.setUploadStatus("uploaded");
        asset.setModerationStatus("pass");
        asset.setRiskTagsJson("[]");
        asset.setReason("内容审核通过");

        when(mediaAssetMapper.selectList(any())).thenReturn(List.of(asset));

        MediaAssetQueryRequest request = new MediaAssetQueryRequest();
        request.setAssetIds(List.of(201L));

        List<MediaAssetVO> result = mediaAssetService.getMediaAssets(request);

        assertEquals(1, result.size());
        assertEquals("asset_201", result.get(0).getAssetNo());
    }
}
