package com.packclaw.config;

import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.model.ChatModelBase;
import io.agentscope.core.model.transport.OkHttpTransport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Chat model configuration
 */
@Configuration
public class ChatModelConfig {

    @Value("${packclaw.dashscope.apiKey}")
    private String apiKey;

    @Value("${packclaw.dashscope.model}")
    private String model;

    @Bean
    @Primary
    public ChatModelBase chatModelBase() {
        return DashScopeChatModel.builder()
                .apiKey(apiKey)
                .modelName(model)
                .enableThinking(true)
                .enableSearch(true)
                .stream(true)
                .httpTransport(OkHttpTransport.builder().build())
                .build();
    }
}
