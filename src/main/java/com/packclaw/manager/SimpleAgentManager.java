package com.packclaw.manager;

import com.packclaw.prompt.PromptConstants;
import com.packclaw.utils.PromptFactory;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.Event;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.ChatModelBase;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Slf4j
@Component
public class SimpleAgentManager {

    @Resource
    private ChatModelBase deepseekChatModel;

    public String call(String query, String systemPromptPath) {
        String systemPrompt = PromptFactory.loadPrompt(systemPromptPath);

        ReActAgent agent = createAgent(systemPrompt);

        // 发送消息
        Msg msg = Msg.builder()
                .textContent(query)
                .build();

        String content = null;
        try {
            Msg block = agent.call(msg).block();
            if (block != null) {
                content = block.getTextContent();
            }
        } catch (Exception e) {
            log.error("在调用智能体进行对话时出错,prompt地址: {}", systemPromptPath, e);
        }

        return content;
    }

    private ReActAgent createAgent(String systemPrompt) {
        // 创建智能体
        return ReActAgent.builder()
                .sysPrompt(systemPrompt)
                .model(deepseekChatModel)
                .build();
    }
}
