package com.packclaw.controller;

import com.packclaw.common.domain.Response;
import com.packclaw.model.po.ModelConfig;
import com.packclaw.service.ModelConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 模型配置控制器
 */
@Slf4j
@RestController
@RequestMapping("/model-config")
@Tag(name = "Model Config", description = "模型配置管理")
public class ModelConfigController {

    @Resource
    private ModelConfigService modelConfigService;

    /**
     * 获取所有模型配置列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取模型配置列表", description = "获取所有已配置的模型列表")
    public Response<List<ModelConfig>> getList() {
        log.info("Getting model config list");
        return Response.ok(modelConfigService.getList());
    }

    /**
     * 获取当前激活的模型配置
     */
    @GetMapping("/active")
    @Operation(summary = "获取激活的模型配置", description = "获取当前正在使用的模型配置")
    public Response<ModelConfig> getActiveConfig() {
        log.info("Getting active model config");
        return Response.ok(modelConfigService.getActiveConfig());
    }

    /**
     * 添加新模型配置
     */
    @PostMapping
    @Operation(summary = "添加模型配置", description = "添加新的模型配置")
    public Response<ModelConfig> addConfig(@RequestBody ModelConfig config) {
        log.info("Adding model config: provider={}, model={}", config.getProvider(), config.getModelId());
        
        // 验证配置是否可用
        try {
            modelConfigService.validateConfig(config);
        } catch (Exception e) {
            log.error("Model config validation failed: {}", e.getMessage());
            return Response.error("模型配置验证失败: " + e.getMessage());
        }
        
        // 如果是第一个模型配置，设置为激活状态
        List<ModelConfig> allConfigs = modelConfigService.getList();
        if (allConfigs.isEmpty()) {
            config.setIsActive(true);
            log.info("First model config added, auto-activating: id={}", config.getId());
        } else {
            config.setIsActive(false);
        }
        
        return Response.ok(modelConfigService.addConfig(config));
    }

    /**
     * 更新模型配置
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新模型配置", description = "更新指定的模型配置")
    public Response<ModelConfig> updateConfig(@PathVariable Integer id, @RequestBody ModelConfig config) {
        config.setId(id);
        log.info("Updating model config: id={}, provider={}, model={}", id, config.getProvider(), config.getModelId());
        
        // 验证配置是否可用
        try {
            modelConfigService.validateConfig(config);
        } catch (Exception e) {
            log.error("Model config validation failed: {}", e.getMessage());
            return Response.error("模型配置验证失败: " + e.getMessage());
        }
        
        // 保留原有的 is_active 状态
        ModelConfig existingConfig = modelConfigService.getList().stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null);
        if (existingConfig != null) {
            config.setIsActive(existingConfig.getIsActive());
        }
        
        return Response.ok(modelConfigService.updateConfig(config));
    }

    /**
     * 删除模型配置
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除模型配置", description = "删除指定的模型配置")
    public Response<Void> deleteConfig(@PathVariable Integer id) {
        log.info("Deleting model config: id={}", id);
        modelConfigService.deleteConfig(id);
        return Response.ok();
    }

    /**
     * 激活模型配置
     */
    @PostMapping("/{id}/activate")
    @Operation(summary = "激活模型配置", description = "激活指定的模型配置,激活后新会话将使用该模型")
    public Response<Void> activateConfig(@PathVariable Integer id) {
        log.info("Activating model config: id={}", id);
        modelConfigService.activateConfig(id);
        return Response.ok();
    }
}
