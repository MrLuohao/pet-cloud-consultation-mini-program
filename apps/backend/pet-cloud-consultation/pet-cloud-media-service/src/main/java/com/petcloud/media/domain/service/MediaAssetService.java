package com.petcloud.media.domain.service;

import com.petcloud.media.domain.dto.MediaAssetQueryRequest;
import com.petcloud.media.domain.dto.RegisterMediaAssetRequest;
import com.petcloud.media.domain.vo.MediaAssetVO;

import java.util.List;

public interface MediaAssetService {
    MediaAssetVO registerMediaAsset(RegisterMediaAssetRequest request);

    List<MediaAssetVO> getMediaAssets(MediaAssetQueryRequest request);
}
