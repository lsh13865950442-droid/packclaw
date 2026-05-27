package com.packclaw.service.impl;

import com.packclaw.manager.HarnessAgentFactory;
import com.packclaw.mapper.SkillConfigMapper;
import com.packclaw.model.po.SkillConfig;
import com.packclaw.service.SkillConfigService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 技能配置服务实现
 */
@Slf4j
@Service
public class SkillConfigServiceImpl implements SkillConfigService {

    @Resource
    private SkillConfigMapper skillConfigMapper;

    @Resource
    private HarnessAgentFactory harnessAgentFactory;

    @Value("${packclaw.workspace.path:./data/workspace}")
    private String workspacePath;

    @Value("${packclaw.skill.repository.path:./data/skill-repository}")
    private String skillRepositoryPath;

    @Override
    public List<SkillConfig> getList() {
        return skillConfigMapper.selectAll();
    }

    @Override
    public SkillConfig getById(Integer id) {
        return skillConfigMapper.selectById(id);
    }

    @Override
    public SkillConfig addSkill(SkillConfig skillConfig) {
        // 验证技能路径
        try {
            validateSkillPath(skillConfig.getSkillPath());
        } catch (Exception e) {
            throw new RuntimeException("技能路径验证失败: " + e.getMessage());
        }

        // 默认启用
        if (skillConfig.getIsEnabled() == null) {
            skillConfig.setIsEnabled(true);
        }

        skillConfigMapper.insert(skillConfig);
        log.info("Added skill config: id={}, name={}, path={}", 
                skillConfig.getId(), skillConfig.getSkillName(), skillConfig.getSkillPath());
        return skillConfig;
    }

    @Override
    public SkillConfig updateSkill(SkillConfig skillConfig) {
        // 验证技能路径
        try {
            validateSkillPath(skillConfig.getSkillPath());
        } catch (Exception e) {
            throw new RuntimeException("技能路径验证失败: " + e.getMessage());
        }

        skillConfigMapper.update(skillConfig);
        log.info("Updated skill config: id={}, name={}, path={}", 
                skillConfig.getId(), skillConfig.getSkillName(), skillConfig.getSkillPath());
        return skillConfig;
    }

    @Override
    public void deleteSkill(Integer id) {
        // 先获取技能信息（用于删除文件）
        SkillConfig skill = skillConfigMapper.selectById(id);
        
        // 删除数据库记录
        skillConfigMapper.deleteById(id);
        
        // 删除技能文件
        if (skill != null) {
            try {
                // 1. 删除技能仓库中的文件
                String skillPath = skill.getSkillPath();
                if (skillPath != null) {
                    File skillDir = new File(skillPath);
                    if (skillDir.exists()) {
                        harnessAgentFactory.deleteSkillDirectory(skillDir);
                        log.info("Deleted skill from repository: {}", skillPath);
                    }
                }
                
                // 2. 如果在 workspace 中也有（已启用的），也删除
                Path workspaceSkillDir = Path.of(workspacePath, "skills", skill.getSkillName());
                if (Files.exists(workspaceSkillDir)) {
                    harnessAgentFactory.deleteSkillDirectory(workspaceSkillDir.toFile());
                    log.info("Deleted skill from workspace: {}", workspaceSkillDir);
                }
            } catch (Exception e) {
                log.error("Failed to delete skill files", e);
                // 不抛出异常，避免影响主流程
            }
        }
        
        log.info("Deleted skill config: id={}", id);
    }

    @Override
    public void toggleEnabled(Integer id, Boolean isEnabled) {
        // 更新数据库状态
        skillConfigMapper.updateEnabledStatus(id, isEnabled);
        
        // 同步到 workspace（启用则添加，禁用则删除）
        try {
            SkillConfig skill = skillConfigMapper.selectById(id);
            if (skill != null) {
                harnessAgentFactory.syncSkillToWorkspace(skill);
            }
        } catch (Exception e) {
            log.error("Failed to sync skill to workspace after toggle", e);
            // 不抛出异常，避免影响主流程
        }
        
        log.info("Toggled skill enabled status: id={}, enabled={}", id, isEnabled);
    }

    @Override
    public void validateSkillPath(String skillPath) throws Exception {
        if (skillPath == null || skillPath.trim().isEmpty()) {
            throw new Exception("技能路径不能为空");
        }

        File skillDir = new File(skillPath);
        if (!skillDir.exists()) {
            throw new Exception("技能路径不存在: " + skillPath);
        }

        if (!skillDir.isDirectory()) {
            throw new Exception("技能路径必须是目录: " + skillPath);
        }

        // 检查是否包含 SKILL.md 文件
        File skillMd = new File(skillDir, "SKILL.md");
        if (!skillMd.exists()) {
            throw new Exception("技能目录中必须包含 SKILL.md 文件: " + skillPath);
        }

        log.info("Skill path validated successfully: {}", skillPath);
    }

    @Override
    public String uploadSkillFile(MultipartFile file) throws Exception {
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new Exception("文件名为空");
        }
        
        String lowerName = fileName.toLowerCase();
        
        if (lowerName.endsWith(".zip")) {
            // 解压zip文件
            return extractZipFile(file);
        } else if (lowerName.equals("skill.md")) {
            // 直接保存SKILL.md
            return saveSkillMdFile(file);
        } else {
            throw new Exception("只支持.zip或SKILL.md文件");
        }
    }

    @Override
    public String readSkillContent(String path) throws Exception {
        File skillDir = new File(path);
        
        // 先尝试在根目录找 SKILL.md
        File skillMd = new File(skillDir, "SKILL.md");
        
        // 如果根目录没有，递归查找
        if (!skillMd.exists()) {
            skillMd = findSkillMdFile(skillDir);
        }
        
        if (skillMd == null || !skillMd.exists()) {
            throw new Exception("SKILL.md文件不存在");
        }
        
        return new String(Files.readAllBytes(skillMd.toPath()));
    }

    /**
     * 递归查找 SKILL.md 文件（优先查找当前目录，再递归子目录）
     */
    private File findSkillMdFile(File dir) {
        File[] files = dir.listFiles();
        if (files == null) return null;
        
        // 第一轮：先查找当前目录下的文件
        for (File file : files) {
            if (!file.isDirectory() && "SKILL.md".equalsIgnoreCase(file.getName())) {
                return file;
            }
        }
        
        // 第二轮：再递归查找子目录
        for (File file : files) {
            if (file.isDirectory()) {
                File found = findSkillMdFile(file);
                if (found != null) {
                    return found;
                }
            }
        }
        
        return null;
    }

    /**
     * 解压zip文件
     */
    private String extractZipFile(MultipartFile file) throws Exception {
        // 创建技能仓库目录（所有技能的存储位置）
        Path skillsDir = Paths.get(skillRepositoryPath);
        if (!Files.exists(skillsDir)) {
            Files.createDirectories(skillsDir);
        }
        
        // 生成唯一目录名
        String skillName = file.getOriginalFilename().replace(".zip", "").replaceAll("[^a-zA-Z0-9_-]", "_");
        String timestamp = String.valueOf(System.currentTimeMillis());
        String extractDir = skillsDir.resolve(skillName + "_" + timestamp).toString();
        
        Files.createDirectories(Paths.get(extractDir));
        
        // 先将上传文件保存到临时文件（ZipFile需要File对象，能正确处理STORED+EXTdescriptor等格式）
        Path tempZipFile = Files.createTempFile("skill_upload_", ".zip");
        file.transferTo(tempZipFile.toFile());
        
        // 先解压到临时目录
        Path tempDir = Files.createTempDirectory("skill_extract_");
        
        // 使用ZipFile解压（相比ZipInputStream，ZipFile能处理更多ZIP格式变体）
        try (ZipFile zipFile = new ZipFile(tempZipFile.toFile())) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                Path targetPath = tempDir.resolve(entry.getName());
                
                if (entry.isDirectory()) {
                    Files.createDirectories(targetPath);
                } else {
                    // 确保父目录存在
                    Files.createDirectories(targetPath.getParent());
                    try (InputStream is = zipFile.getInputStream(entry)) {
                        Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        } finally {
            // 清理临时zip文件
            Files.deleteIfExists(tempZipFile);
        }
        
        // 查找 SKILL.md 的位置
        File skillMdFile = findSkillMdFile(tempDir.toFile());
        
        if (skillMdFile == null) {
            // 清理临时目录
            deleteDirectory(tempDir.toFile());
            throw new Exception("压缩包中未找到 SKILL.md 文件");
        }
        
        // 获取 SKILL.md 所在的目录
        File skillDirFile = skillMdFile.getParentFile();
        
        // 将技能文件复制到目标目录
        copyDirectoryContents(skillDirFile, new File(extractDir));
        
        // 清理临时目录
        deleteDirectory(tempDir.toFile());
        
        log.info("Extracted zip to: {}", extractDir);
        return extractDir;
    }

    /**
     * 保存SKILL.md文件
     */
    private String saveSkillMdFile(MultipartFile file) throws Exception {
        // 创建技能仓库目录（所有技能的存储位置）
        Path skillsDir = Paths.get(skillRepositoryPath);
        if (!Files.exists(skillsDir)) {
            Files.createDirectories(skillsDir);
        }
        
        // 生成唯一目录名
        String timestamp = String.valueOf(System.currentTimeMillis());
        String skillDir = skillsDir.resolve("skill_" + timestamp).toString();
        
        Files.createDirectories(Paths.get(skillDir));
        
        // 保存SKILL.md
        Path skillMdPath = Paths.get(skillDir, "SKILL.md");
        Files.copy(file.getInputStream(), skillMdPath, StandardCopyOption.REPLACE_EXISTING);
        
        log.info("Saved SKILL.md to: {}", skillDir);
        return skillDir;
    }

    /**
     * 复制目录内容
     */
    private void copyDirectoryContents(File source, File dest) throws IOException {
        if (!dest.exists()) {
            dest.mkdirs();
        }
        
        File[] files = source.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            File destFile = new File(dest, file.getName());
            if (file.isDirectory()) {
                copyDirectoryContents(file, destFile);
            } else {
                Files.copy(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    /**
     * 删除目录及其内容
     */
    private void deleteDirectory(File dir) {
        if (!dir.exists()) return;
        
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        
        dir.delete();
    }
}
