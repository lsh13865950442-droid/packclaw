package com.packclaw.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * 工作区初始化器
 * 应用启动时将 classpath:workspace/ 下的文件复制到运行时目录
 */
@Slf4j
@Component
public class WorkspaceInitializer {

    @Value("${packclaw.workspace.path:./data/workspace}")
    private String workspacePath;

    @PostConstruct
    public void init() {
        try {
            Path workspaceDir = Path.of(workspacePath);
            Files.createDirectories(workspaceDir);

            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:workspace/**/*");

            for (Resource resource : resources) {
                if (!resource.exists() || !resource.isReadable()) {
                    continue;
                }

                String urlPath = resource.getURL().getPath();
                // 提取 workspace 后的相对路径
                int workspaceIndex = urlPath.indexOf("workspace/");
                if (workspaceIndex == -1) {
                    continue;
                }
                String relativePath = urlPath.substring(workspaceIndex + "workspace/".length());
                if (relativePath.isEmpty()) {
                    continue;
                }

                Path targetPath = workspaceDir.resolve(relativePath);

                // 如果目标文件已存在，跳过（不覆盖 MEMORY.md 等运行时文件）
                if (Files.exists(targetPath)) {
                    log.debug("Workspace file already exists, skipping: {}", targetPath);
                    continue;
                }

                // 创建父目录
                Files.createDirectories(targetPath.getParent());

                // 复制文件
                try (InputStream is = resource.getInputStream()) {
                    Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    log.info("Initialized workspace file: {}", relativePath);
                }
            }

            log.info("Workspace initialized at: {}", workspaceDir.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to initialize workspace", e);
            throw new RuntimeException("Failed to initialize workspace", e);
        }
    }
}
