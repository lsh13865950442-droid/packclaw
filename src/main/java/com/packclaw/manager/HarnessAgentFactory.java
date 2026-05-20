package com.packclaw.manager;

import io.agentscope.core.agent.Event;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
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
     * @param query     用户查询
     * @return SSE 事件流
     */
    public Flux<Event> stream(String sessionId, String query) {
        // 每次调用创建新的 HarnessAgent 实例，避免并发执行冲突
        HarnessAgent agent = createAgent();
        activeAgents.put(sessionId, agent);

        RuntimeContext ctx = RuntimeContext.builder()
                .sessionId(sessionId)
                .userId("local_user")
                .build();

        Msg msg = Msg.builder()
                .role(MsgRole.USER)
                .textContent(query)
                .build();

        return agent.stream(msg, ctx)
                .doFinally(signalType -> {
                    log.info("Stream terminated with signal: {}, session: {}", signalType, sessionId);
                    activeAgents.remove(sessionId);
                });
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
