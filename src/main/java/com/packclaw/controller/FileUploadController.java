package com.packclaw.controller;

import com.packclaw.common.domain.Response;
import com.packclaw.service.QiniuUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 文件上传接口
 *
 * 当前策略：使用千牛云对象存储
 *   - 所有媒体文件（图片/音频/视频）上传至千牛云
 *   - 返回公网可访问 URL，模型可直接处理
 */
@Slf4j
@RestController
@RequestMapping("/file")
@Tag(name = "File Upload", description = "Upload media files for multimodal chat")
@RequiredArgsConstructor
public class FileUploadController {

    private final QiniuUploadService qiniuUploadService;

    /**
     * 上传任意类型文件
     * 文件上传到千牛云对象存储，返回公网访问URL
     */
    @PostMapping("/upload")
    @Operation(summary = "Upload file", description = "Upload any type file to Qiniu cloud storage, return public URL")
    public Response<String> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return Response.fail("File is empty");
        }

        // 上传到千牛云（支持任意文件类型）
        String fileUrl = qiniuUploadService.uploadFile(file);

        log.info("File uploaded: {} -> {}", file.getOriginalFilename(), fileUrl);
        return Response.ok(fileUrl);
    }
}
