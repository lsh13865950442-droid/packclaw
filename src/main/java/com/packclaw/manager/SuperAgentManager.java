package com.packclaw.manager;

import com.packclaw.config.PgsqlSession;
import com.packclaw.model.bo.AgentContext;
import com.packclaw.prompt.PromptConstants;
import com.packclaw.tools.GetCurrentTimeTool;
import com.packclaw.utils.PromptFactory;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.Event;
import io.agentscope.core.memory.Memory;
import io.agentscope.core.memory.autocontext.AutoContextConfig;
import io.agentscope.core.memory.autocontext.AutoContextHook;
import io.agentscope.core.memory.autocontext.AutoContextMemory;
import io.agentscope.core.memory.autocontext.ContextOffloadTool;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.model.ChatModelBase;
import io.agentscope.core.model.ExecutionConfig;
import io.agentscope.core.tool.ToolExecutionContext;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.ToolkitConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SuperAgentManager {

    @Resource
    private ChatModelBase chatModelBase;

    @Resource
    private GetCurrentTimeTool getCurrentTimeTool;

    @Resource
    private PgsqlSession pgsqlSession;

    /**
     * 存储会话的agent
     */
    private final ConcurrentHashMap<String, ReActAgent> concurrentAgents = new ConcurrentHashMap<>();


    public Flux<Event> stream(String query, String sessionId) {

        ReActAgent agent = createAgent(sessionId);

        concurrentAgents.put(sessionId, agent);

        agent.loadIfExists(pgsqlSession, sessionId);

        Msg msg = Msg.builder()
                .role(MsgRole.USER)
                .textContent(query)
                .build();

        return agent.stream(msg)
                .doFinally(
                        signalType -> {
                            log.info(
                                    "Stream terminated with signal: {}, saving session: {}",
                                    signalType,
                                    sessionId);
                            agent.saveTo(pgsqlSession, sessionId);
                        });
    }

    public ReActAgent createAgent(String sessionId) {
        // 创建内存
        AutoContextConfig config = AutoContextConfig.builder()
                .msgThreshold(10)
                .lastKeep(5)
                .tokenRatio(0.3)
                .minCompressionTokenThreshold(500)
                .build();
        AutoContextMemory memory = new AutoContextMemory(config, chatModelBase);

        // 创建工具箱
        Toolkit toolkit = new Toolkit(ToolkitConfig.builder()
                .executionConfig(ExecutionConfig.builder()
                        .timeout(Duration.ofMinutes(20))
                        .build())
                .build());
        toolkit.registerTool(new ContextOffloadTool(memory));

        // 加载 prompt
        String systemPrompt = PromptFactory.loadPrompt(PromptConstants.AGENTIC_LLM);

        ToolExecutionContext context = ToolExecutionContext.builder()
                .register(new AgentContext(sessionId))
                .build();

        // 创建智能体
        return ReActAgent.builder()
                .name("PackClawAgent")
                .sysPrompt(systemPrompt)
                .model(chatModelBase)
                .memory(memory)
                .toolkit(toolkit)
                .toolExecutionContext(context)
                .hooks(List.of(
                        new AutoContextHook()
                ))
                .build();
    }


    /**
     * 删除指定会话的代理
     *
     * @param sessionId 会话ID
     */
    public void deleteAgent(String sessionId) {
        concurrentAgents.remove(sessionId);
    }

    /**
     * 中断指定会话的代理
     *
     * @param sessionId 会话ID
     */
    public void interruptAgent(String sessionId) {
        log.info("Interrupted agent for session: {}", sessionId);
        ReActAgent agent = concurrentAgents.get(sessionId);
        if (agent != null) {
            agent.interrupt();
            log.info("Interrupted agent for session: {}", sessionId);
        }
    }

    public Memory getMemory(String sessionId) {
        ReActAgent agent = concurrentAgents.get(sessionId);
        if (agent != null) {
            return agent.getMemory();
        }
        return null;
    }
}
