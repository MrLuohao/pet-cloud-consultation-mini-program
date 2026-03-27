package com.petcloud.media.application.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petcloud.media.domain.dto.MediaAssetQueryRequest;
import com.petcloud.media.domain.dto.RegisterMediaAssetRequest;
import com.petcloud.media.domain.entity.MediaAsset;
import com.petcloud.media.domain.enums.MediaModerationStatus;
import com.petcloud.media.domain.enums.MediaType;
import com.petcloud.media.domain.enums.MediaUploadStatus;
import com.petcloud.media.domain.service.MediaAssetService;
import com.petcloud.media.domain.vo.MediaAssetVO;
import com.petcloud.media.infrastructure.persistence.mapper.MediaAssetMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaAssetServiceImpl implements MediaAssetService {

    private final MediaAssetMapper mediaAssetMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MediaAssetVO registerMediaAsset(RegisterMediaAssetRequest request) {
        List<String> riskTags = new ArrayList<>();
        MediaType mediaType = MediaType.fromMimeType(request.getMimeType());
        MediaModerationStatus moderationStatus = MediaModerationStatus.PASS;
        String reason = "内容审核通过";

        String normalizedName = String.valueOf(request.getOriginalFilename()).toLowerCase();
        if (normalizedName.contains("blood") || normalizedName.contains("gore") || normalizedName.contains("violent")) {
            moderationStatus = MediaModerationStatus.REJECT;
            reason = "内容不符合发布规范，请更换素材后重试";
            riskTags.add("bloody");
            riskTags.add("violence");
        } else if (normalizedName.contains("scan") || normalizedName.contains("report") || normalizedName.contains("unclear")) {
            moderationStatus = MediaModerationStatus.REVIEW;
            reason = "素材已上传，审核中，请稍后再试";
            riskTags.add("manual_review");
        }

        MediaAsset asset = new MediaAsset();
        asset.setAssetNo("asset_" + UUID.randomUUID().toString().replace("-", ""));
        asset.setOwnerType(request.getOwnerType());
        asset.setOwnerId(request.getOwnerId());
        asset.setUserId(request.getUserId());
        asset.setMediaType(mediaType.getCode());
        asset.setUrl(request.getUrl());
        asset.setMimeType(request.getMimeType());
        asset.setFileSize(request.getFileSize());
        asset.setUploadStatus(MediaUploadStatus.UPLOADED.getCode());
        asset.setModerationStatus(moderationStatus.getCode());
        asset.setRiskTagsJson(writeJsonSafe(riskTags));
        asset.setReason(reason);
        mediaAssetMapper.insert(asset);
        return toVO(asset, riskTags, moderationStatus);
    }

    @Override
    public List<MediaAssetVO> getMediaAssets(MediaAssetQueryRequest request) {
        if (request == null || request.getAssetIds() == null || request.getAssetIds().isEmpty()) {
            return List.of();
        }
        List<Long> assetIds = request.getAssetIds().stream().filter(Objects::nonNull).distinct().toList();
        if (assetIds.isEmpty()) {
            return List.of();
        }
        return mediaAssetMapper.selectList(new LambdaQueryWrapper<MediaAsset>()
                        .in(MediaAsset::getId, assetIds))
                .stream()
                .map(this::toVO)
                .toList();
    }

    private MediaAssetVO toVO(MediaAsset asset) {
        MediaModerationStatus moderationStatus = MediaModerationStatus.fromCode(asset.getModerationStatus());
        return toVO(asset, readStringList(asset.getRiskTagsJson()), moderationStatus);
    }

    private MediaAssetVO toVO(MediaAsset asset, List<String> riskTags, MediaModerationStatus moderationStatus) {
        return MediaAssetVO.builder()
                .assetId(asset.getId())
                .assetNo(asset.getAssetNo())
                .url(asset.getUrl())
                .mediaType(asset.getMediaType())
                .uploadStatus(asset.getUploadStatus())
                .moderationStatus(moderationStatus.getCode())
                .availableForSubmit(moderationStatus.isAllowedForBizSubmission())
                .riskTags(riskTags)
                .reason(asset.getReason())
                .build();
    }

    private String writeJsonSafe(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("媒体风险标签序列化失败", e);
        }
    }

    private List<String> readStringList(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(raw, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }
}
