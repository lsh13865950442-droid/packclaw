package com.packclaw.manager;

import com.packclaw.prompt.PromptConstants;
import com.packclaw.tools.GetCurrentTimeTool;
import com.packclaw.utils.PromptFactory;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.a2a.agent.A2aAgent;
import io.agentscope.core.a2a.agent.card.WellKnownAgentCardResolver;
import io.agentscope.core.memory.autocontext.AutoContextConfig;
import io.agentscope.core.memory.autocontext.AutoContextMemory;
import io.agentscope.core.memory.autocontext.ContextOffloadTool;
import io.agentscope.core.model.ChatModelBase;
import io.agentscope.core.model.ExecutionConfig;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.ToolkitConfig;
import io.agentscope.core.tool.subagent.SubAgentConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Slf4j
@Configuration
public class AgentA2AManager {

    @Resource
    private ChatModelBase chatModelBase;

    @Resource
    private GetCurrentTimeTool getCurrentTimeTool;

    @Bean
    public ReActAgent.Builder reActAgent() {

        // 创建内存
        AutoContextConfig config = AutoContextConfig.builder()
                .msgThreshold(30)
                .lastKeep(10)
                .tokenRatio(0.3)
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

        // 创建智能体
        return ReActAgent.builder()
                .name("PackClawAgent")
                .sysPrompt(systemPrompt)
                .model(chatModelBase)
                .memory(memory)
                .toolkit(toolkit);
    }
}
