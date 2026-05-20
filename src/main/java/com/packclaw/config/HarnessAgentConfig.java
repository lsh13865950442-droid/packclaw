package com.packclaw.config;

import io.agentscope.core.model.ChatModelBase;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.model.transport.OkHttpTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ChatModel 配置
 * 提供共享的模型实例，由 HarnessAgentFactory 按需创建 Agent
 */
@Slf4j
@Configuration
public class HarnessAgentConfig {

    @Value("${packclaw.dashscope.apiKey}")
    private String apiKey;

    @Value("${packclaw.dashscope.model}")
    private String modelName;

    @Bean
    public ChatModelBase chatModelBase() {
        log.info("Initializing DashScopeChatModel with model: {}", modelName);
        return DashScopeChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .enableThinking(true)
                .enableSearch(true)
                .stream(true)
                .httpTransport(OkHttpTransport.builder().build())
                .build();
    }
}
