package com.packclaw.config;

import com.packclaw.model.po.ModelConfig;
import com.packclaw.service.ModelConfigService;
import io.agentscope.core.model.AnthropicChatModel;
import io.agentscope.core.model.ChatModelBase;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.model.OpenAIChatModel;
import io.agentscope.core.model.transport.OkHttpTransport;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * ChatModel 工厂
 * 从数据库动态加载模型配置，每次创建新的 ChatModelBase 实例
 */
@Slf4j
@Configuration
public class HarnessAgentConfig {

    @Resource
    private ModelConfigService modelConfigService;

    /**
     * 创建一个新的 ChatModelBase 实例
     * 每次调用都会从数据库查询当前激活的模型配置
     */
    public ChatModelBase createChatModel() {
        ModelConfig config = modelConfigService.getActiveConfig();
        if (config == null) {
            log.warn("No active model config found in database. Please configure a model via Settings.");
            return null;
        }

        log.info("Creating new ChatModel: provider={}, model={}", config.getProvider(), config.getModelId());
        return createModel(config);
    }

    /**
     * 根据配置创建对应的ChatModel实例
     */
    private ChatModelBase createModel(ModelConfig config) {
        String provider = config.getProvider().toUpperCase();
        
        return switch (provider) {
            case "DASHSCOPE" -> createDashScopeModel(config);
            case "OPENAI" -> createOpenAIModel(config);
            case "ANTHROPIC" -> createAnthropicModel(config);
            default -> throw new IllegalArgumentException("Unsupported model provider: " + provider);
        };
    }

    /**
     * 创建 DashScope 模型
     */
    private DashScopeChatModel createDashScopeModel(ModelConfig config) {
        var builder = DashScopeChatModel.builder()
                .apiKey(config.getApiKey())
                .modelName(config.getModelId())
                .enableThinking(true)  // 默认启用
                .enableSearch(true)     // 默认启用
                .stream(true)
                .httpTransport(OkHttpTransport.builder().build());

        if (config.getApiUrl() != null && !config.getApiUrl().isBlank()) {
            builder.baseUrl(config.getApiUrl());
        }

        return builder.build();
    }

    /**
     * 创建 OpenAI 模型（兼容 OpenAI API 格式）
     */
    private OpenAIChatModel createOpenAIModel(ModelConfig config) {
        var builder = OpenAIChatModel.builder()
                .apiKey(config.getApiKey())
                .modelName(config.getModelId())
                .stream(true)
                .httpTransport(OkHttpTransport.builder().build());

        if (config.getApiUrl() != null && !config.getApiUrl().isBlank()) {
            builder.baseUrl(config.getApiUrl());
        }

        return builder.build();
    }

    /**
     * 创建 Anthropic 模型
     */
    private AnthropicChatModel createAnthropicModel(ModelConfig config) {
        var builder = AnthropicChatModel.builder()
                .apiKey(config.getApiKey())
                .modelName(config.getModelId())
                .stream(true);

        if (config.getApiUrl() != null && !config.getApiUrl().isBlank()) {
            builder.baseUrl(config.getApiUrl());
        }

        return builder.build();
    }
}
