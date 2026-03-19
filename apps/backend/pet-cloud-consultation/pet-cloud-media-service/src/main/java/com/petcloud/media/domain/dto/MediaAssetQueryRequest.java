package com.petcloud.media.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class MediaAssetQueryRequest {
    private List<Long> assetIds;
}
