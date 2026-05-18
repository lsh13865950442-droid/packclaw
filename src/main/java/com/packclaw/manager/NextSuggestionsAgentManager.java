package com.packclaw.manager;

import com.alibaba.fastjson2.JSON;
import com.packclaw.config.PgsqlSession;
import com.packclaw.prompt.PromptConstants;
import com.packclaw.utils.PromptFactory;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.memory.Memory;
import io.agentscope.core.memory.autocontext.AutoContextConfig;
import io.agentscope.core.memory.autocontext.AutoContextMemory;
import io.agentscope.core.memory.autocontext.ContextOffloadTool;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.ChatModelBase;
import io.agentscope.core.model.ExecutionConfig;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.ToolkitConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class NextSuggestionsAgentManager {

    @Resource
    private ChatModelBase chatModelBase;

    @Resource
    private PgsqlSession session;

    public List<String> call(Memory memory) {
        String systemPrompt = PromptFactory.loadPrompt(PromptConstants.NEXT_SUGGESTIONS);

        // 创建新的Memory副本，避免修改原始Memory
        AutoContextMemory copiedMemory = copyMemory((AutoContextMemory) memory);

        ReActAgent agent = createAgent(systemPrompt, copiedMemory);

        Msg msg = Msg.builder().textContent("请基于之前的上下文生成3个可能的后续问题或指令").build();

        Msg block;
        try {
            block = agent.call(msg).block();
        } catch (Exception e) {
            log.warn("NextSuggestions agent 调用异常, sessionId可能包含name为null的历史消息, 返回空建议列表", e);
            return List.of();
        }

        if (block != null) {
            String textContent = block.getTextContent();
            if (textContent != null && !textContent.isEmpty()) {
                try {
                    // 尝试解析 JSON 数组
                    String[] data = JSON.parseObject(textContent, String[].class);
                    if (data != null && data.length > 0) {
                        return Arrays.asList(data);
                    }
                } catch (Exception e) {
                    log.warn("解析结构化数据失败, textContent: {}", textContent, e);
                }
            }
        }

        return List.of();
    }

    public List<String> call(String sessionId) {
        String systemPrompt = PromptFactory.loadPrompt(PromptConstants.NEXT_SUGGESTIONS);

        ReActAgent agent = createAgent(systemPrompt);

        agent.loadIfExists(session, sessionId);

        Msg msg = Msg.builder().textContent("请基于之前的上下文生成3个可能的后续问题或指令").build();

        Msg block;
        try {
            block = agent.call(msg).block();
        } catch (Exception e) {
            log.warn("NextSuggestions agent 调用异常, sessionId可能包含name为null的历史消息, 返回空建议列表");
            return List.of();
        }

        if (block != null) {
            String textContent = block.getTextContent();
            if (textContent != null && !textContent.isEmpty()) {
                try {
                    // 尝试解析 JSON 数组
                    String[] data = JSON.parseObject(textContent, String[].class);
                    if (data != null && data.length > 0) {
                        return Arrays.asList(data);
                    }
                } catch (Exception e) {
                    log.warn("解析结构化数据失败, textContent: {}", textContent, e);
                }
            }
        }

        return List.of();
    }


    /**
     * 创建Memory的副本，避免修改原始Memory
     *
     * @param originalMemory 原始Memory
     * @return 新的Memory副本
     */
    private AutoContextMemory copyMemory(AutoContextMemory originalMemory) {
        AutoContextConfig config = AutoContextConfig.builder()
                .msgThreshold(30)
                .lastKeep(10)
                .tokenRatio(0.3)
                .build();

        AutoContextMemory newMemory = new AutoContextMemory(config, chatModelBase);

        List<Msg> messages = originalMemory.getMessages();
        if (messages != null) {
            for (Msg msg : messages) {
                newMemory.addMessage(msg);
            }
        }

        return newMemory;
    }

    private ReActAgent createAgent(String systemPrompt, AutoContextMemory memory) {
        Toolkit toolkit = new Toolkit(ToolkitConfig.builder()
                .executionConfig(ExecutionConfig.builder()
                        .timeout(Duration.ofSeconds(10 * 60))
                        .build())
                .build());
        toolkit.registerTool(new ContextOffloadTool(memory));

        return ReActAgent.builder()
                .sysPrompt(systemPrompt)
                .model(chatModelBase)
                .toolkit(toolkit)
                .memory(memory)
                .build();
    }

    private ReActAgent createAgent(String systemPrompt) {
        AutoContextConfig config = AutoContextConfig.builder()
                .msgThreshold(30)
                .lastKeep(10)
                .tokenRatio(0.3)
                .build();

        AutoContextMemory memory = new AutoContextMemory(config, chatModelBase);

        Toolkit toolkit = new Toolkit(ToolkitConfig.builder()
                .executionConfig(ExecutionConfig.builder()
                        .timeout(Duration.ofSeconds(10 * 60))
                        .build())
                .build());
        toolkit.registerTool(new ContextOffloadTool(memory));

        return ReActAgent.builder()
                .sysPrompt(systemPrompt)
                .model(chatModelBase)
                .toolkit(toolkit)
                .memory(memory)
                .build();
    }
}
