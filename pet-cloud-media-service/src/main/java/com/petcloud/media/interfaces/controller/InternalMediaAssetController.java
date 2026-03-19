package com.petcloud.media.interfaces.controller;

import com.petcloud.common.core.response.Response;
import com.petcloud.media.domain.dto.MediaAssetQueryRequest;
import com.petcloud.media.domain.dto.RegisterMediaAssetRequest;
import com.petcloud.media.domain.service.MediaAssetService;
import com.petcloud.media.domain.vo.MediaAssetVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/media")
public class InternalMediaAssetController {

    private final MediaAssetService mediaAssetService;

    @PostMapping("/assets/register")
    public Response<MediaAssetVO> registerMediaAsset(@RequestBody RegisterMediaAssetRequest request) {
        return Response.succeed(mediaAssetService.registerMediaAsset(request));
    }

    @PostMapping("/assets/query")
    public Response<List<MediaAssetVO>> getMediaAssets(@RequestBody MediaAssetQueryRequest request) {
        return Response.succeed(mediaAssetService.getMediaAssets(request));
    }
}
