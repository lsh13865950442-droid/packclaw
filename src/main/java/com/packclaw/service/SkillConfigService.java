package com.packclaw.service;

import com.packclaw.model.po.SkillConfig;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 技能配置服务接口
 */
public interface SkillConfigService {

    /**
     * 获取所有技能配置列表
     */
    List<SkillConfig> getList();

    /**
     * 根据ID获取技能配置
     */
    SkillConfig getById(Integer id);

    /**
     * 添加技能配置
     */
    SkillConfig addSkill(SkillConfig skillConfig);

    /**
     * 更新技能配置
     */
    SkillConfig updateSkill(SkillConfig skillConfig);

    /**
     * 删除技能配置（包括数据库记录和文件）
     */
    void deleteSkill(Integer id);

    /**
     * 切换技能启用状态（包括数据库更新和文件同步）
     */
    void toggleEnabled(Integer id, Boolean isEnabled);

    /**
     * 验证技能路径是否有效
     */
    void validateSkillPath(String skillPath) throws Exception;

    /**
     * 上传技能文件（zip或SKILL.md）
     * @return 解压/保存后的目录路径
     */
    String uploadSkillFile(MultipartFile file) throws Exception;

    /**
     * 读取SKILL.md内容
     */
    String readSkillContent(String path) throws Exception;
}
