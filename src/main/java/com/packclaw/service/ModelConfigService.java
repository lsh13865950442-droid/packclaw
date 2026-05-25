package com.packclaw.service;

import com.packclaw.model.po.ModelConfig;

import java.util.List;

/**
 * 模型配置服务接口
 */
public interface ModelConfigService {

    /**
     * 获取所有模型配置列表
     *
     * @return 模型配置列表
     */
    List<ModelConfig> getList();

    /**
     * 获取当前激活的模型配置
     *
     * @return 激活的模型配置
     */
    ModelConfig getActiveConfig();

    /**
     * 添加新模型配置
     *
     * @param config 模型配置
     * @return 保存后的配置
     */
    ModelConfig addConfig(ModelConfig config);

    /**
     * 更新模型配置
     *
     * @param config 模型配置
     * @return 更新后的配置
     */
    ModelConfig updateConfig(ModelConfig config);

    /**
     * 删除模型配置
     *
     * @param id 配置ID
     */
    void deleteConfig(Integer id);

    /**
     * 激活指定模型配置
     *
     * @param id 配置ID
     */
    void activateConfig(Integer id);

    /**
     * 验证模型配置是否可用
     *
     * @param config 模型配置
     * @throws Exception 验证失败时抛出异常
     */
    void validateConfig(ModelConfig config) throws Exception;
}
