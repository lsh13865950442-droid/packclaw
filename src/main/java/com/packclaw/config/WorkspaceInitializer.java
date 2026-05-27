package com.packclaw.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 工作区初始化器
 * 应用启动时创建必要的工作区目录和默认文件
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
            
            // 1. 创建基础目录结构
            createDirectory(workspaceDir, "基础工作区目录");
            createDirectory(workspaceDir.resolve("knowledge"), "知识目录");
            createDirectory(workspaceDir.resolve("memory"), "记忆目录");
            createDirectory(workspaceDir.resolve("skills"), "技能目录");
            createDirectory(workspaceDir.resolve("sessions"), "会话目录");
            createDirectory(workspaceDir.resolve("tasks"), "任务目录");
            createDirectory(workspaceDir.resolve("uploads"), "上传目录");
            
            // 2. 创建 AGENTS.md（如果不存在）
            Path agentsMd = workspaceDir.resolve("AGENTS.md");
            createFileIfNotExists(agentsMd, getDefaultAgentsMdContent(), "AGENTS.md");
            
            // 3. 创建 KNOWLEDGE.md（如果不存在）
            Path knowledgeMd = workspaceDir.resolve("knowledge").resolve("KNOWLEDGE.md");
            createFileIfNotExists(knowledgeMd, getDefaultKnowledgeMdContent(), "KNOWLEDGE.md");
            
            // 4. 创建 MEMORY.md（如果不存在）
            Path memoryMd = workspaceDir.resolve("MEMORY.md");
            createFileIfNotExists(memoryMd, getDefaultMemoryMdContent(), "MEMORY.md");

            log.info("✓ Workspace initialized at: {}", workspaceDir.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to initialize workspace", e);
            throw new RuntimeException("Failed to initialize workspace", e);
        }
    }
    
    /**
     * 创建目录
     */
    private void createDirectory(Path path, String description) throws IOException {
        if (!Files.exists(path)) {
            Files.createDirectories(path);
            log.info("  创建{}: {}", description, path);
        } else {
            log.debug("  {}已存在: {}", description, path);
        }
    }
    
    /**
     * 创建文件（如果不存在）
     */
    private void createFileIfNotExists(Path path, String content, String description) throws IOException {
        if (!Files.exists(path)) {
            Files.writeString(path, content);
            log.info("  创建{}: {}", description, path);
        } else {
            log.debug("  {}已存在，跳过: {}", description, path);
        }
    }
    
    /**
     * 默认的 AGENTS.md 内容
     */
    private String getDefaultAgentsMdContent() {
        return """
# PackClaw Agent

你是一个有用的 AI 助手。

## 能力
- 直接进入正题 少说「好的」「很乐意」，直接开始做事。
- 先想再做 接到任务后先理清思路，想清楚步骤再执行，避免盲目行动。
- 办事严谨 做事牢靠是获得信任的关键，不要让用户后悔使用你。
- 尽量不拒绝 尽量满足用户的合理请求。只有在涉及违法、有害、侵犯他人权益或超出你能力范围时，才明确说明原因并拒绝。
- 隐私守护 永远守护用户的隐私，除非用户明确要求分享。
- 遇到无法解决的问题时，需要向用户反馈出现的问题，并尝试提供可能的解决方案。

## 行为准则
- 像真正的人类一样回复，语气可爱、有温度：
- 对用户的问题给出适当建议并说明理由，常用「我建议」「我觉得」等表达。
- 表达观点 对事情给出个人看法，常用「就我个人而言」「这个东西不对」。
- 使用表情 在合适处使用表情，让回复更有生气，但不过度。
- 不废话、贴心 做一个不废话的助手，永远贴心、永远热情。不要过度谄媚，也不要过度高冷。
- 不确定的信息要说明
- 使用中文回复（除非用户用其他语言）
""";
    }
    
    /**
     * 默认的 KNOWLEDGE.md 内容
     */
    private String getDefaultKnowledgeMdContent() {
        return """
# Knowledge

<!-- 领域知识存放目录 -->
<!-- 可以在此处添加与业务相关的知识文件 -->
""";
    }
    
    /**
     * 默认的 MEMORY.md 内容
     */
    private String getDefaultMemoryMdContent() {
        return """
# Memory

<!-- 系统记忆文件，由 AI 自动维护 -->
<!-- 不要手动编辑此文件 -->
""";
    }
}
