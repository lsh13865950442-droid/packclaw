package com.packclaw.mapper;

import com.packclaw.model.po.ModelConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 模型配置 Mapper 接口
 */
@Mapper
public interface ModelConfigMapper {

    /**
     * 查询所有模型配置列表
     *
     * @return 模型配置列表
     */
    List<ModelConfig> selectAll();

    /**
     * 根据ID查询模型配置
     *
     * @param id 配置ID
     * @return 模型配置
     */
    ModelConfig selectById(@Param("id") Integer id);

    /**
     * 查询当前激活的模型配置
     *
     * @return 激活的模型配置
     */
    ModelConfig selectActive();

    /**
     * 插入模型配置
     *
     * @param config 模型配置
     * @return 影响行数
     */
    int insert(ModelConfig config);

    /**
     * 更新模型配置
     *
     * @param config 模型配置
     * @return 影响行数
     */
    int update(ModelConfig config);

    /**
     * 删除模型配置
     *
     * @param id 配置ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Integer id);
}
