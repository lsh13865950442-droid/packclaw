package com.packclaw.config;

import com.qiniu.storage.BucketManager;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 千牛云对象存储配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "packclaw.qiniu")
public class QiniuConfig {

    /**
     * 访问密钥 AK
     */
    private String accessKey;

    /**
     * 秘钥 SK
     */
    private String secretKey;

    /**
     * 存储空间名称
     */
    private String bucket;

    /**
     * 访问域名
     */
    private String domain;

    /**
     * 创建千牛云认证对象
     */
    @Bean
    public Auth auth() {
        return Auth.create(accessKey, secretKey);
    }

    /**
     * 创建千牛云上传管理器
     */
    @Bean
    public UploadManager uploadManager() {
        return new UploadManager(new com.qiniu.storage.Configuration());
    }

    /**
     * 创建千牛云空间管理器
     */
    @Bean
    public BucketManager bucketManager() {
        return new BucketManager(auth(), new com.qiniu.storage.Configuration());
    }
}
