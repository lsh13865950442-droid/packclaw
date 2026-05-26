package com.packclaw.mapper;

import com.packclaw.model.po.SkillConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 技能配置 Mapper
 */
@Mapper
public interface SkillConfigMapper {

    /**
     * 查询所有技能配置
     */
    List<SkillConfig> selectAll();

    /**
     * 根据ID查询技能配置
     */
    SkillConfig selectById(@Param("id") Integer id);

    /**
     * 插入技能配置
     */
    int insert(SkillConfig skillConfig);

    /**
     * 更新技能配置
     */
    int update(SkillConfig skillConfig);

    /**
     * 删除技能配置
     */
    int deleteById(@Param("id") Integer id);

    /**
     * 更新技能启用状态
     */
    int updateEnabledStatus(@Param("id") Integer id, @Param("isEnabled") Boolean isEnabled);
}
