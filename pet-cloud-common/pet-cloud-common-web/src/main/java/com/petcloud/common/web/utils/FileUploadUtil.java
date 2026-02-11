package com.petcloud.common.web.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传工具类
 *
 * @author luohao
 */
@Slf4j
@Component
public class FileUploadUtil {

    @Value("${file.upload.path:/tmp/pet-cloud/uploads}")
    private String uploadBasePath;

    @Value("${file.upload.url-prefix:http://localhost:8080/uploads}")
    private String urlPrefix;

    /**
     * 允许的图片格式
     */
    private static final List<String> ALLOWED_IMAGE_TYPES = List.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    /**
     * 最大文件大小（5MB）
     */
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    /**
     * 上传单个文件
     *
     * @param file 文件
     * @return 文件访问URL
     * @throws IOException 上传失败
     */
    public String uploadFile(MultipartFile file) throws IOException {
        validateFile(file);

        // 生成文件路径: uploads/2024/01/15/uuid.jpg
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String filename = generateFilename(file.getOriginalFilename());
        Path uploadPath = Paths.get(uploadBasePath, datePath);
        Files.createDirectories(uploadPath);

        Path targetPath = uploadPath.resolve(filename);

        // 保存文件
        file.transferTo(targetPath);

        log.info("文件上传成功: {}", targetPath);

        // 返回访问URL
        return urlPrefix + "/" + datePath + "/" + filename;
    }

    /**
     * 上传多个文件
     *
     * @param files 文件列表
     * @return 文件访问URL列表
     * @throws IOException 上传失败
     */
    public List<String> uploadFiles(List<MultipartFile> files) throws IOException {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                String url = uploadFile(file);
                urls.add(url);
            }
        }
        return urls;
    }

    /**
     * 上传多个文件（数组形式）
     *
     * @param files 文件数组
     * @return 文件访问URL列表
     * @throws IOException 上传失败
     */
    public List<String> uploadFiles(MultipartFile[] files) throws IOException {
        List<String> urls = new ArrayList<>();
        if (files != null) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    String url = uploadFile(file);
                    urls.add(url);
                }
            }
        }
        return urls;
    }

    /**
     * 验证文件
     *
     * @param file 文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("不支持的文件类型: " + contentType);
        }

        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小超过限制（最大5MB）");
        }
    }

    /**
     * 生成文件名（保留原扩展名）
     *
     * @param originalFilename 原始文件名
     * @return 新文件名
     */
    private String generateFilename(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }

    /**
     * 删除文件
     *
     * @param fileUrl 文件URL
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            // 从URL中提取相对路径
            String relativePath = fileUrl.replace(urlPrefix, "");
            Path filePath = Paths.get(uploadBasePath, relativePath);

            Files.deleteIfExists(filePath);
            log.info("文件删除成功: {}", filePath);
        } catch (IOException e) {
            log.warn("文件删除失败: {}", fileUrl, e);
        }
    }
}
