package com.petcloud.user.interfaces.controller.media;

import com.petcloud.common.core.exception.RespType;
import com.petcloud.common.core.response.Response;
import com.petcloud.common.web.utils.FileUploadUtil;
import com.petcloud.common.web.utils.UserContextHolderWeb;
import com.petcloud.user.domain.enums.MediaOwnerType;
import com.petcloud.user.domain.enums.MediaType;
import com.petcloud.user.domain.enums.UserRespType;
import com.petcloud.user.domain.vo.MediaUploadVO;
import com.petcloud.user.infrastructure.feign.MediaServiceClient;
import com.petcloud.user.infrastructure.feign.dto.MediaAssetVO;
import com.petcloud.user.infrastructure.feign.dto.RegisterMediaAssetRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
/**
 * 文件上传控制器
 *
 * @author luohao
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class UploadController {

    private final FileUploadUtil fileUploadUtil;
    private final UserContextHolderWeb userContextHolderWeb;
    private final MediaServiceClient mediaServiceClient;

    @PostMapping("/api/media/upload")
    public Response<MediaUploadVO> uploadMedia(@RequestParam("file") MultipartFile file,
                                               @RequestParam(defaultValue = MediaOwnerType.DEFAULT_CODE) String ownerType,
                                               HttpServletRequest request) {
        try {
            Long userId = userContextHolderWeb.getCurrentUserId(request);
            String fileUrl = fileUploadUtil.uploadMediaFile(file);

            RegisterMediaAssetRequest registerRequest = new RegisterMediaAssetRequest();
            registerRequest.setUserId(userId);
            registerRequest.setOwnerType(resolveOwnerType(ownerType));
            registerRequest.setMediaType(MediaType.fromMimeType(file.getContentType()).getCode());
            registerRequest.setUrl(fileUrl);
            registerRequest.setMimeType(file.getContentType());
            registerRequest.setFileSize(file.getSize());
            registerRequest.setOriginalFilename(file.getOriginalFilename());

            MediaAssetVO assetVO = mediaServiceClient.registerMediaAsset(registerRequest).getData();
            return Response.succeed(MediaUploadVO.builder()
                    .assetId(assetVO.getAssetId())
                    .assetNo(assetVO.getAssetNo())
                    .url(assetVO.getUrl())
                    .mediaType(assetVO.getMediaType())
                    .uploadStatus(assetVO.getUploadStatus())
                    .moderationStatus(assetVO.getModerationStatus())
                    .availableForSubmit(assetVO.getAvailableForSubmit())
                    .riskTags(assetVO.getRiskTags())
                    .reason(assetVO.getReason())
                    .build());
        } catch (IOException e) {
            log.error("统一媒体上传失败", e);
            return Response.error(UserRespType.MEDIA_UPLOAD_FAILED, e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("统一媒体上传参数错误: {}", e.getMessage());
            return Response.of(RespType.PARAMETER_ERROR, e.getMessage());
        }
    }

    private String resolveOwnerType(String ownerType) {
        return MediaOwnerType.normalize(ownerType);
    }
}
