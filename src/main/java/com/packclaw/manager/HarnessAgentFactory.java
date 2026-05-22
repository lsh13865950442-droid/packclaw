package com.packclaw.manager;

import com.packclaw.model.req.ChatRequest;
import io.agentscope.core.agent.Event;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.*;
import io.agentscope.core.model.ChatModelBase;
import io.agentscope.core.session.JsonSession;
import io.agentscope.core.state.SimpleSessionKey;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.memory.compaction.CompactionConfig;
import io.agentscope.harness.agent.memory.compaction.ToolResultEvictionConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import jakarta.annotation.Resource;
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
 */
@Slf4j
@Component
public class HarnessAgentFactory {

    @Resource
    private ChatModelBase chatModelBase;

    @Value("${packclaw.workspace.path:./data/workspace}")
    private String workspacePath;

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
     *     TODO: 目前 filePath 是服务器本地路径，无法直接作为 URL 使用。
     *           后续需接入公网文件服务，将音频/视频上传至公网并返回可访问 URL，
     *           再由 FileUploadController 返回该 URL 而非本地路径。
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
            try {
                switch (prefix) {
                    case "image" -> {
                        // 图片：本地读取转 Base64（DashScope 支持，限制 10MB 内）
                        byte[] fileBytes = Files.readAllBytes(Path.of(item.getFilePath()));
                        String base64Data = java.util.Base64.getEncoder().encodeToString(fileBytes);
                        log.info("Image loaded as Base64: {}, size: {} bytes", item.getFilePath(), fileBytes.length);
                        blocks.add(ImageBlock.builder()
                                .source(Base64Source.builder()
                                        .data(base64Data)
                                        .mediaType(item.getMediaType())
                                        .build())
                                .build());
                    }
                    case "audio" -> {
                        // 音频：使用 URLSource
                        // TODO: filePath 当前为本地绝对路径，暂无法直接用于 URL。
                        //       后续接入公网文件服务后，改为返回公网 URL，此处直接使用 item.getFilePath()。
                        log.info("Audio as URL: {}", item.getFilePath());
                        blocks.add(AudioBlock.builder()
                                .source(URLSource.builder()
                                        .url(item.getFilePath())
                                        .build())
                                .build());
                    }
                    case "video" -> {
                        // 视频：使用 URLSource（DashScope 视频不支持 Base64，只支持 URL）
                        // TODO: filePath 当前为本地绝对路径，暂无法直接用于 URL。
                        //       后续接入公网文件服务后，改为返回公网 URL，此处直接使用 item.getFilePath()。
                        log.info("Video as URL: {}", item.getFilePath());
                        blocks.add(VideoBlock.builder()
                                .source(URLSource.builder()
                                        .url(item.getFilePath())
                                        .build())
                                .build());
                    }
                    default -> log.warn("Unsupported media type prefix: {}", prefix);
                }
            } catch (IOException e) {
                log.error("Failed to read media file: {}", item.getFilePath(), e);
            }
        }

        return Msg.builder()
                .role(MsgRole.USER)
                .content(blocks)
                .build();
    }

    /**
     * 创建新的 HarnessAgent 实例
     */
    private HarnessAgent createAgent() {
        Path workspace = Path.of(workspacePath);

        HarnessAgent agent = HarnessAgent.builder()
                .name("PackClawAgent")
                .model(chatModelBase)
                .workspace(workspace)
                .compaction(CompactionConfig.builder().build())
                .toolResultEviction(ToolResultEvictionConfig.builder().build())
                .build();

        return agent;
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
