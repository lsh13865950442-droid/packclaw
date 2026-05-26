package com.packclaw.controller;

import com.packclaw.common.domain.Response;
import com.packclaw.model.po.SkillConfig;
import com.packclaw.service.SkillConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 技能配置控制器
 */
@Slf4j
@RestController
@RequestMapping("/skill-config")
@Tag(name = "Skill Config", description = "技能配置管理")
public class SkillConfigController {

    @Resource
    private SkillConfigService skillConfigService;

    /**
     * 获取所有技能配置列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取技能配置列表", description = "获取所有已配置的技能列表")
    public Response<List<SkillConfig>> getList() {
        log.info("Getting skill config list");
        return Response.ok(skillConfigService.getList());
    }

    /**
     * 添加技能配置
     */
    @PostMapping
    @Operation(summary = "添加技能配置", description = "从本地路径安装新技能")
    public Response<SkillConfig> addSkill(@RequestBody SkillConfig skillConfig) {
        log.info("Adding skill config: name={}, path={}", skillConfig.getSkillName(), skillConfig.getSkillPath());
        
        // 验证技能路径
        try {
            skillConfigService.validateSkillPath(skillConfig.getSkillPath());
        } catch (Exception e) {
            log.error("Skill path validation failed: {}", e.getMessage());
            return Response.error("技能路径验证失败: " + e.getMessage());
        }
        
        return Response.ok(skillConfigService.addSkill(skillConfig));
    }

    /**
     * 更新技能配置
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新技能配置", description = "更新指定的技能配置")
    public Response<SkillConfig> updateSkill(@PathVariable Integer id, @RequestBody SkillConfig skillConfig) {
        skillConfig.setId(id);
        log.info("Updating skill config: id={}, name={}, path={}", id, skillConfig.getSkillName(), skillConfig.getSkillPath());
        
        // 验证技能路径
        try {
            skillConfigService.validateSkillPath(skillConfig.getSkillPath());
        } catch (Exception e) {
            log.error("Skill path validation failed: {}", e.getMessage());
            return Response.error("技能路径验证失败: " + e.getMessage());
        }
        
        return Response.ok(skillConfigService.updateSkill(skillConfig));
    }

    /**
     * 删除技能配置
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除技能配置", description = "删除指定的技能配置")
    public Response<Void> deleteSkill(@PathVariable Integer id) {
        log.info("Deleting skill config: id={}", id);
        skillConfigService.deleteSkill(id);
        return Response.ok();
    }

    /**
     * 切换技能启用状态
     */
    @PostMapping("/{id}/toggle")
    @Operation(summary = "切换技能启用状态", description = "启用或禁用指定的技能")
    public Response<Void> toggleEnabled(
            @PathVariable Integer id,
            @RequestParam Boolean isEnabled) {
        log.info("Toggling skill enabled status: id={}, enabled={}", id, isEnabled);
        skillConfigService.toggleEnabled(id, isEnabled);
        return Response.ok();
    }

    /**
     * 上传技能文件（zip或SKILL.md）
     */
    @PostMapping("/upload")
    @Operation(summary = "上传技能文件", description = "上传zip压缩包或SKILL.md文件")
    public Response<String> uploadSkill(@RequestParam("file") MultipartFile file) {
        log.info("Uploading skill file: {}", file.getOriginalFilename());
        
        try {
            String extractedPath = skillConfigService.uploadSkillFile(file);
            return Response.ok(extractedPath);
        } catch (Exception e) {
            log.error("Failed to upload skill file: {}", e.getMessage(), e);
            return Response.error("上传失败: " + e.getMessage());
        }
    }

    /**
     * 读取SKILL.md内容
     */
    @GetMapping("/read-skill")
    @Operation(summary = "读取技能文件内容", description = "从解压目录读取SKILL.md内容")
    public Response<String> readSkillContent(@RequestParam("path") String path) {
        log.info("Reading skill content from: {}", path);
        
        try {
            String content = skillConfigService.readSkillContent(path);
            return Response.ok(content);
        } catch (Exception e) {
            log.error("Failed to read skill content: {}", e.getMessage(), e);
            return Response.error("读取失败: " + e.getMessage());
        }
    }
}
