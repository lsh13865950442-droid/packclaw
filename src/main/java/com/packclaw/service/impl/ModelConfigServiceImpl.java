package com.packclaw.service.impl;

import com.packclaw.mapper.ModelConfigMapper;
import com.packclaw.model.po.ModelConfig;
import com.packclaw.service.ModelConfigService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 模型配置服务实现
 */
@Slf4j
@Service
public class ModelConfigServiceImpl implements ModelConfigService {

    @Resource
    private ModelConfigMapper modelConfigMapper;

    @Override
    public List<ModelConfig> getList() {
        return modelConfigMapper.selectAll();
    }

    @Override
    public ModelConfig getActiveConfig() {
        ModelConfig config = modelConfigMapper.selectActive();
        if (config == null) {
            log.warn("No active model config found");
        }
        return config;
    }

    @Override
    @Transactional
    public ModelConfig addConfig(ModelConfig config) {
        config.setCreatedAt(new Date());
        config.setUpdatedAt(new Date());
        modelConfigMapper.insert(config);
        log.info("Added new model config: provider={}, model={}", config.getProvider(), config.getModelId());
        return config;
    }

    @Override
    @Transactional
    public ModelConfig updateConfig(ModelConfig config) {
        config.setUpdatedAt(new Date());
        modelConfigMapper.update(config);
        log.info("Updated model config: id={}, provider={}, model={}", 
                config.getId(), config.getProvider(), config.getModelId());
        return modelConfigMapper.selectById(config.getId());
    }

    @Override
    @Transactional
    public void deleteConfig(Integer id) {
        modelConfigMapper.deleteById(id);
        log.info("Deleted model config: id={}", id);
    }

    @Override
    @Transactional
    public void activateConfig(Integer id) {
        // 先将所有模型的 is_active 设为 false
        List<ModelConfig> allConfigs = modelConfigMapper.selectAll();
        for (ModelConfig config : allConfigs) {
            if (!config.getId().equals(id)) {
                config.setIsActive(false);
                modelConfigMapper.update(config);
            }
        }
        
        // 将指定模型的 is_active 设为 true
        ModelConfig config = modelConfigMapper.selectById(id);
        if (config == null) {
            log.warn("Model config not found: id={}", id);
            return;
        }
        
        config.setIsActive(true);
        modelConfigMapper.update(config);
        
        log.info("Activated model config: id={}", id);
    }

    @Override
    public void validateConfig(ModelConfig config) throws Exception {
        log.info("Validating model config: provider={}, model={}", config.getProvider(), config.getModelId());
            
        // 根据不同的provider构建验证请求
        String baseUrl = config.getApiUrl();
        String apiKey = config.getApiKey();
        String modelId = config.getModelId();
            
        // 如果baseUrl为空,使用默认值
        if (baseUrl == null || baseUrl.isEmpty()) {
            switch (config.getProvider()) {
                case "DASHSCOPE":
                    baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";
                    break;
                case "OPENAI":
                    baseUrl = "https://api.openai.com/v1";
                    break;
                case "ANTHROPIC":
                    baseUrl = "https://api.anthropic.com/v1";
                    break;
                default:
                    throw new Exception("不支持的模型提供商: " + config.getProvider());
            }
        }
            
        // 去除baseUrl末尾的斜杠,避免拼接错误
        baseUrl = baseUrl.replaceAll("/+$", "");
            
        // 根据不同的provider拼接完整URL
        String apiUrl;
        if ("ANTHROPIC".equals(config.getProvider())) {
            apiUrl = baseUrl + "/messages";
        } else {
            // DashScope和OpenAI使用OpenAI兼容格式
            apiUrl = baseUrl + "/chat/completions";
        }
        
        // 构建验证请求
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        
        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        if ("ANTHROPIC".equals(config.getProvider())) {
            // Anthropic使用不同的格式
            requestBody.put("model", modelId);
            requestBody.put("max_tokens", 10);
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", "hi");
            requestBody.put("messages", List.of(message));
        } else {
            // DashScope和OpenAI使用OpenAI兼容格式
            requestBody.put("model", modelId);
            requestBody.put("max_tokens", 10);
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", "hi");
            requestBody.put("messages", List.of(message));
        }
        
        String jsonBody = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(requestBody);
        
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(new URI(apiUrl))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json");
        
        // 设置Authorization
        if ("ANTHROPIC".equals(config.getProvider())) {
            requestBuilder.header("x-api-key", apiKey);
            requestBuilder.header("anthropic-version", "2023-06-01");
        } else {
            requestBuilder.header("Authorization", "Bearer " + apiKey);
        }
        
        requestBuilder.POST(HttpRequest.BodyPublishers.ofString(jsonBody));
        
        HttpRequest request = requestBuilder.build();
        
        // 发送请求
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        // 检查响应状态
        if (response.statusCode() != 200) {
            log.error("Validation failed: status={}, body={}", response.statusCode(), response.body());
            throw new Exception("模型配置验证失败: HTTP " + response.statusCode() + " - " + response.body());
        }
        
        log.info("Model config validation successful: provider={}, model={}", config.getProvider(), config.getModelId());
    }
}
