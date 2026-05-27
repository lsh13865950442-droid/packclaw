package com.packclaw.manager;

import com.packclaw.config.HarnessAgentConfig;
import com.packclaw.model.po.SkillConfig;
import com.packclaw.model.req.ChatRequest;
import io.agentscope.core.agent.Event;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.*;
import io.agentscope.core.model.ChatModelBase;
import io.agentscope.core.session.JsonSession;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.memory.compaction.CompactionConfig;
import io.agentscope.harness.agent.memory.compaction.ToolResultEvictionConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import jakarta.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HarnessAgent 工厂
 * 每次调用创建新的 HarnessAgent 实例，避免并发执行冲突
 * 替代原有的 SuperAgentManager
 * 
 * 技能加载说明:
 * HarnessAgent 会自动从 workspace 的 skills 目录下加载技能
 * 我们只需要确保已启用的技能被同步到 workspace 的 skills 目录下
 */
@Slf4j
@Component
public class HarnessAgentFactory {

    @Resource
    private HarnessAgentConfig harnessAgentConfig;

    @Value("${packclaw.workspace.path:./data/workspace}")
    private String workspacePath;

    @Value("${packclaw.skill.repository.path:./data/skill-repository}")
    private String skillRepositoryPath;

    /**
     * 存储活跃会话的 agent
     */
    private final ConcurrentHashMap<String, HarnessAgent> activeAgents = new ConcurrentHashMap<>();

    /**
     * 流式对话
     *
     * @param sessionId 会话ID
     * @param request   聊天请求（包含文本和可选的多媒体内容）
     * @return SSE 事件流
     */
    public Flux<Event> stream(String sessionId, ChatRequest request) {
        // 每次调用创建新的 HarnessAgent 实例，避免并发执行冲突
        HarnessAgent agent = createAgent();
        activeAgents.put(sessionId, agent);

        RuntimeContext ctx = RuntimeContext.builder()
                .sessionId(sessionId)
                .build();

        Msg msg = buildMsg(request);

        return agent.stream(msg, ctx)
                .doFinally(signalType -> {
                    log.info("Stream terminated with signal: {}, session: {}", signalType, sessionId);
                    activeAgents.remove(sessionId);
                });
    }

    /**
     * 根据请求构建 Msg，支持纯文本和多模态
     * 媒体处理策略：
     *   - 图片：从服务器本地路径读取，转 Base64 传输（DashScope 图片 Base64 限制 10MB）
     *   - 音频/视频：使用 URLSource，需公网可访问的 URL
     */
    private Msg buildMsg(ChatRequest request) {
        List<ChatRequest.MediaItem> mediaItems = request.getMediaItems();

        // 无媒体文件：直接构建纯文本消息
        if (mediaItems == null || mediaItems.isEmpty()) {
            return Msg.builder()
                    .role(MsgRole.USER)
                    .textContent(request.getQuery())
                    .build();
        }

        // 有媒体文件：构建多模态消息
        List<ContentBlock> blocks = new ArrayList<>();
        blocks.add(TextBlock.builder().text(request.getQuery()).build());

        for (ChatRequest.MediaItem item : mediaItems) {
            if (item.getMediaType() == null || item.getFilePath() == null) continue;

            String prefix = item.getMediaType().split("/")[0].toLowerCase();
            switch (prefix) {
                case "image" -> {
                    blocks.add(ImageBlock.builder()
                            .source(URLSource.builder()
                                    .url(item.getFilePath())
                                    .build())
                            .build());
                }
                case "audio" -> {
                    // 音频：使用 URLSource
                    log.info("Audio as URL: {}", item.getFilePath());
                    blocks.add(AudioBlock.builder()
                            .source(URLSource.builder()
                                    .url(item.getFilePath())
                                    .build())
                            .build());
                }
                case "video" -> {
                    // 视频：使用 URLSource（DashScope 视频不支持 Base64，只支持 URL）
                    log.info("Video as URL: {}", item.getFilePath());
                    blocks.add(VideoBlock.builder()
                            .source(URLSource.builder()
                                    .url(item.getFilePath())
                                    .build())
                            .build());
                }
                default -> log.warn("Unsupported media type prefix: {}", prefix);
            }
        }

        return Msg.builder()
                .role(MsgRole.USER)
                .content(blocks)
                .build();
    }

    /**
     * 创建新的 HarnessAgent 实例
     * 每次从数据库获取最新的激活模型配置并创建 ChatModelBase
     * 
     * HarnessAgent 会自动从 workspace 的 skills 目录加载技能
     * 技能同步在启用/禁用时已完成，这里不需要重复同步
     */
    private HarnessAgent createAgent() {
        Path workspace = Path.of(workspacePath);
        
        // 动态获取最新的模型配置并创建 ChatModelBase
        ChatModelBase currentModel = harnessAgentConfig.createChatModel();
        if (currentModel == null) {
            throw new IllegalStateException("ChatModelBase is not initialized. Please check model configuration.");
        }

        // 创建 Toolkit
        // 技能已在启用/禁用时同步到 workspace，HarnessAgent 会自动加载
        Toolkit toolkit = new Toolkit();

        HarnessAgent agent = HarnessAgent.builder()
                .name("PackClawAgent")
                .model(currentModel)
                .workspace(workspace)
                .toolkit(toolkit)
                .compaction(CompactionConfig.builder().build())
                .toolResultEviction(ToolResultEvictionConfig.builder().build())
                .maxIters(200)  // 增加最大迭代次数，避免复杂任务提前终止
                .checkRunning(false)
                .enablePlan()
                .build();

        return agent;
    }

    /**
     * 删除技能目录（公开方法，供 Controller 调用）
     */
    public void deleteSkillDirectory(File dir) {
        if (!dir.exists()) return;
        
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteSkillDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        
        dir.delete();
        log.info("Deleted skill directory: {}", dir.getPath());
    }

    /**
     * 同步技能到 workspace（仅在技能启用/禁用时调用）
     * 
     * 技能存储架构:
     * - 技能仓库 (skillRepositoryPath): 存储所有已安装的技能（无论启用/禁用）
     * - 工作空间 (workspace/skills): 仅存储已启用的技能，供 HarnessAgent 加载
     * 
     * 优化策略:
     * - 不再每次对话时同步，改为在技能状态变更时调用
     * - 增量同步：只添加/删除变更的技能，不清空整个目录
     */
    public void syncSkillToWorkspace(SkillConfig skill) {
        try {
            Path workspace = Path.of(workspacePath);
            Path skillsDir = workspace.resolve("skills");
            if (!Files.exists(skillsDir)) {
                Files.createDirectories(skillsDir);
            }
            
            String skillName = skill.getSkillName();
            Path targetDir = skillsDir.resolve(skillName);
            
            if (Boolean.TRUE.equals(skill.getIsEnabled())) {
                // 启用：复制到 workspace
                File sourceDir = new File(skill.getSkillPath());
                
                if (!sourceDir.exists() || !sourceDir.isDirectory()) {
                    log.warn("Skill directory not found: {}", skill.getSkillPath());
                    return;
                }
                
                File skillMd = new File(sourceDir, "SKILL.md");
                if (!skillMd.exists()) {
                    log.warn("SKILL.md not found in: {}", skill.getSkillPath());
                    return;
                }
                
                // 复制技能到 workspace
                copyDirectoryContents(sourceDir, targetDir.toFile());
                log.info("Synced enabled skill to workspace: {} -> {}", skillName, targetDir);
                
            } else {
                // 禁用：从 workspace 删除
                if (Files.exists(targetDir)) {
                    deleteDirectory(targetDir.toFile());
                    log.info("Removed disabled skill from workspace: {}", targetDir);
                }
            }
        } catch (Exception e) {
            log.error("Failed to sync skill to workspace: {}", skill.getSkillName(), e);
        }
    }

    /**
     * 删除目录及其内容（私有方法，内部使用）
     */
    private void deleteDirectory(File dir) {
        deleteSkillDirectory(dir);  // 复用公开方法
    }

    /**
     * 复制目录内容
     */
    private void copyDirectoryContents(File source, File dest) throws IOException {
        if (!dest.exists()) {
            dest.mkdirs();
        }
        
        File[] files = source.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            File destFile = new File(dest, file.getName());
            if (file.isDirectory()) {
                copyDirectoryContents(file, destFile);
            } else {
                Files.copy(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    /**
     * 删除指定会话
     *
     * @param sessionId 会话ID
     */
    public void deleteAgent(String sessionId) {
        activeAgents.remove(sessionId);
        log.info("Deleted agent for session: {}", sessionId);
    }

    /**
     * 中断指定会话
     *
     * @param sessionId 会话ID
     */
    public void interruptAgent(String sessionId) {
        HarnessAgent agent = activeAgents.get(sessionId);
        
        if (agent != null) {
            // 1. 调用 HarnessAgent 自带的 interrupt() 方法
            // 该方法会优雅地中断流，添加恢复消息到 memory
            agent.interrupt();
            log.info("Called agent.interrupt() for session: {}", sessionId);
        } else {
            log.warn("No active agent found for session: {}", sessionId);
        }
    }
}
