package com.petcloud.common.web.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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

    @Value("${file.upload.video-transcode.enabled:true}")
    private boolean videoTranscodeEnabled;

    @Value("${file.upload.video-transcode.ffmpeg:ffmpeg}")
    private String ffmpegCommand;

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
     * 允许的视频格式
     */
    private static final List<String> ALLOWED_VIDEO_TYPES = List.of(
            "video/mp4",
            "video/quicktime",
            "video/x-msvideo",
            "video/x-ms-wmv",
            "video/webm"
    );

    /**
     * 图片最大文件大小（5MB）
     */
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;

    /**
     * 视频最大文件大小（100MB）
     */
    private static final long MAX_VIDEO_SIZE = 100 * 1024 * 1024;

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
     * 上传媒体文件（支持图片和视频）
     *
     * @param file 文件
     * @return 文件访问URL
     * @throws IOException 上传失败
     */
    public String uploadMediaFile(MultipartFile file) throws IOException {
        validateMediaFile(file);

        // 生成文件路径: uploads/2024/01/15/uuid.jpg
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String filename = generateFilename(file.getOriginalFilename());
        Path uploadPath = Paths.get(uploadBasePath, datePath);
        Files.createDirectories(uploadPath);

        Path targetPath = uploadPath.resolve(filename);

        // 保存文件
        file.transferTo(targetPath);

        log.info("媒体文件上传成功: {}", targetPath);

        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        if (contentType.startsWith("video/") && videoTranscodeEnabled) {
            Path transcodedPath = transcodeVideoToCompatibleMp4(targetPath);
            if (transcodedPath != null) {
                log.info("视频转码完成，使用兼容文件: {}", transcodedPath);
            }
        }

        // 返回访问URL
        return urlPrefix + "/" + datePath + "/" + filename;
    }

    /**
     * 验证文件（仅图片）
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
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException("文件大小超过限制（最大5MB）");
        }
    }

    /**
     * 验证媒体文件（支持图片和视频）
     *
     * @param file 文件
     */
    private void validateMediaFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            throw new IllegalArgumentException("无法识别文件类型");
        }

        String lowerContentType = contentType.toLowerCase();
        boolean isImage = ALLOWED_IMAGE_TYPES.contains(lowerContentType);
        boolean isVideo = ALLOWED_VIDEO_TYPES.contains(lowerContentType);

        if (!isImage && !isVideo) {
            throw new IllegalArgumentException("不支持的文件类型: " + contentType + "，仅支持图片(jpg/png/gif/webp)和视频(mp4/mov/avi/wmv/webm)");
        }

        // 检查文件大小
        long maxSize = isVideo ? MAX_VIDEO_SIZE : MAX_IMAGE_SIZE;
        String maxSizeText = isVideo ? "100MB" : "5MB";
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("文件大小超过限制（" + maxSizeText + "）");
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
     * 转码为更高兼容性的 MP4（H.264 + AAC + faststart）
     */
    private Path transcodeVideoToCompatibleMp4(Path sourcePath) {
        String sourceFilename = sourcePath.getFileName().toString();
        Path outputPath = sourcePath.getParent().resolve(sourceFilename + ".transcoding.mp4");

        List<String> command = List.of(
                ffmpegCommand,
                "-y",
                "-i", sourcePath.toString(),
                "-c:v", "libx264",
                "-pix_fmt", "yuv420p",
                "-profile:v", "main",
                "-level", "4.1",
                "-c:a", "aac",
                "-b:a", "128k",
                "-movflags", "+faststart",
                outputPath.toString()
        );

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);

        try {
            Process process = pb.start();
            boolean finished = process.waitFor(180, TimeUnit.SECONDS);
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            if (!finished) {
                process.destroyForcibly();
                log.warn("视频转码超时，保留原视频: {}", sourcePath);
                return null;
            }

            if (process.exitValue() != 0 || !Files.exists(outputPath) || Files.size(outputPath) == 0) {
                log.warn("视频转码失败，保留原视频。exitCode={}, output={}", process.exitValue(), output);
                Files.deleteIfExists(outputPath);
                return null;
            }

            Files.move(outputPath, sourcePath, StandardCopyOption.REPLACE_EXISTING);
            return sourcePath;
        } catch (IOException e) {
            log.warn("未执行视频转码（ffmpeg不可用或执行失败），保留原视频: {}", sourcePath, e);
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("视频转码被中断，保留原视频: {}", sourcePath, e);
            return null;
        }
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
            while (relativePath.startsWith("/")) {
                relativePath = relativePath.substring(1);
            }

            Path basePath = Paths.get(uploadBasePath).toAbsolutePath().normalize();
            Path filePath = basePath.resolve(relativePath).normalize();
            if (!filePath.startsWith(basePath)) {
                log.warn("非法文件删除请求，fileUrl: {}", fileUrl);
                return;
            }

            Files.deleteIfExists(filePath);
            log.info("文件删除成功: {}", filePath);
        } catch (IOException e) {
            log.warn("文件删除失败: {}", fileUrl, e);
        }
    }
}
