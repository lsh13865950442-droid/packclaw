package com.packclaw.controller;

import com.packclaw.common.domain.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * 文件上传接口
 *
 * 当前策略：单机版本地存储，返回服务器本地绝对路径
 *   - 图片：后端按路径读取文件，转 Base64 传给模型
 *   - 音频/视频：目前返回本地路径，模型尚无法处理（需公网 URL）
 *
 * TODO: 后续接入公网文件服务（OSS、S3 或其他）后，对音频/视频：
 *       1. 将文件上传至公网存储服务
 *       2. 返回公网可访问 URL 而非本地路径
 *       3. HarnessAgentFactory.buildMsg 中的 URLSource 将直接使用该 URL
 */
@Slf4j
@RestController
@RequestMapping("/file")
@Tag(name = "File Upload", description = "Upload media files for multimodal chat")
public class FileUploadController {

    @Value("${packclaw.workspace.path:./data/workspace}")
    private String workspacePath;

    /**
     * 上传媒体文件（图像/音频/视频）
     * 文件保存到 workspace/uploads 目录，返回绝对路径
     */
    @PostMapping("/upload")
    @Operation(summary = "Upload media file", description = "Upload image/audio/video, save to local uploads dir, return absolute path")
    public Response<String> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return Response.fail("File is empty");
        }

        // 校验媒体类型
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/")
                && !contentType.startsWith("audio/")
                && !contentType.startsWith("video/"))) {
            return Response.fail("Unsupported file type: " + contentType);
        }

        // 确保上传目录存在
        Path uploadDir = Path.of(workspacePath).resolve("uploads");
        Files.createDirectories(uploadDir);

        // 生成唯一文件名，保留原始扩展名
        String originalName = file.getOriginalFilename();
        String ext = (originalName != null && originalName.contains("."))
                ? originalName.substring(originalName.lastIndexOf('.'))
                : "";
        String uniqueName = UUID.randomUUID().toString().replace("-", "") + ext;
        Path target = uploadDir.resolve(uniqueName);

        // 保存文件
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        String filePath = target.toAbsolutePath().toString();

        log.info("File uploaded: {} -> {}", originalName, filePath);
        return Response.ok(filePath);
    }
}
