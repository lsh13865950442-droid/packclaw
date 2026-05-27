package com.packclaw.service;

import com.packclaw.config.QiniuConfig;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 千牛云对象存储上传服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QiniuUploadService {

    private final QiniuConfig qiniuConfig;
    private final Auth auth;
    private final UploadManager uploadManager;

    /**
     * 上传文件到千牛云对象存储
     *
     * @param file 上传的文件
     * @return 文件的完整访问URL
     */
    public String uploadFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // 生成唯一文件名，保留原始扩展名
        String originalName = file.getOriginalFilename();
        String ext = (originalName != null && originalName.contains("."))
                ? originalName.substring(originalName.lastIndexOf('.'))
                : "";
        String uniqueName = UUID.randomUUID().toString().replace("-", "") + ext;

        // 生成上传凭证
        String token = auth.uploadToken(qiniuConfig.getBucket());

        try {
            // 上传文件
            Response response = uploadManager.put(file.getInputStream(), uniqueName, token, null, null);
            
            // 解析上传结果
            if (response.isOK()) {
                String fileUrl = qiniuConfig.getDomain() + "/" + uniqueName;
                log.info("File uploaded to Qiniu: {} -> {}", originalName, fileUrl);
                return fileUrl;
            } else {
                log.error("Failed to upload file to Qiniu: {}", response);
                throw new IOException("Failed to upload file to Qiniu: " + response);
            }
        } catch (QiniuException e) {
            log.error("Qiniu upload exception", e);
            throw new IOException("Qiniu upload failed", e);
        }
    }
}
