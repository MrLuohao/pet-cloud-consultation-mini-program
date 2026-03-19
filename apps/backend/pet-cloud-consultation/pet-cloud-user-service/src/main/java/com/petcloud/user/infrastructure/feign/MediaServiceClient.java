package com.petcloud.user.infrastructure.feign;

import com.petcloud.common.core.response.Response;
import com.petcloud.user.domain.dto.MediaAssetQueryRequest;
import com.petcloud.user.infrastructure.feign.dto.MediaAssetVO;
import com.petcloud.user.infrastructure.feign.dto.RegisterMediaAssetRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "media-service", url = "${media-service.url:http://localhost:8121}")
public interface MediaServiceClient {

    @PostMapping("/internal/media/assets/register")
    Response<MediaAssetVO> registerMediaAsset(@RequestBody RegisterMediaAssetRequest request);

    @PostMapping("/internal/media/assets/query")
    Response<List<MediaAssetVO>> getMediaAssets(@RequestBody MediaAssetQueryRequest request);
}
