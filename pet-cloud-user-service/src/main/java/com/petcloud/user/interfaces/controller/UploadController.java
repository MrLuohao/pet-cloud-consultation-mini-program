package com.petcloud.user.interfaces.controller;

import com.petcloud.common.core.exception.RespType;
import com.petcloud.common.core.response.Response;
import com.petcloud.common.web.utils.FileUploadUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 文件上传控制器
 *
 * @author luohao
 */
@Slf4j
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final FileUploadUtil fileUploadUtil;

    /**
     * 上传单张图片
     *
     * @param file 图片文件
     * @return 图片URL
     */
    @PostMapping("/image")
    public Response<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            log.info("上传图片，文件名: {}, 大小: {} bytes", file.getOriginalFilename(), file.getSize());
            String imageUrl = fileUploadUtil.uploadFile(file);
            log.info("图片上传成功: {}", imageUrl);
            return Response.succeed(imageUrl);
        } catch (IOException e) {
            log.error("图片上传失败", e);
            return Response.error(RespType.PARAMETER_ERROR, "图片上传失败: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("图片上传参数错误: {}", e.getMessage());
            return Response.error(RespType.PARAMETER_ERROR, e.getMessage());
        }
    }

    /**
     * 批量上传图片
     *
     * @param files 图片文件数组
     * @return 图片URL列表
     */
    @PostMapping("/images")
    public Response<List<String>> uploadImages(@RequestParam("files") MultipartFile[] files) {
        try {
            log.info("批量上传图片，数量: {}", files.length);
            List<String> imageUrls = fileUploadUtil.uploadFiles(files);
            log.info("批量图片上传成功，数量: {}", imageUrls.size());
            return Response.succeed(imageUrls);
        } catch (IOException e) {
            log.error("批量图片上传失败", e);
            return Response.error(RespType.PARAMETER_ERROR, "图片上传失败: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("图片上传参数错误: {}", e.getMessage());
            return Response.error(RespType.PARAMETER_ERROR, e.getMessage());
        }
    }
}
