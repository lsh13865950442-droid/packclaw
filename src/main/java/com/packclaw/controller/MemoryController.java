package com.packclaw.controller;

import com.packclaw.common.domain.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 记忆管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/memory")
@Tag(name = "Memory", description = "记忆管理")
public class MemoryController {

    @Value("${packclaw.workspace.path:./data/workspace}")
    private String workspacePath;

    /**
     * 读取记忆文件内容
     */
    @GetMapping("/read")
    @Operation(summary = "读取记忆文件", description = "读取指定的记忆文件内容")
    public Response<String> readMemoryFile(@RequestParam("file") String fileName) {
        log.info("Reading memory file: {}", fileName);
        
        try {
            String filePath = resolveFilePath(fileName);
            String content = Files.readString(Path.of(filePath));
            return Response.ok(content);
        } catch (Exception e) {
            log.error("Failed to read memory file: {}", e.getMessage(), e);
            return Response.error("读取失败: " + e.getMessage());
        }
    }

    /**
     * 保存记忆文件内容
     */
    @PostMapping("/save")
    @Operation(summary = "保存记忆文件", description = "保存记忆文件内容")
    public Response<Void> saveMemoryFile(
            @RequestParam("file") String fileName,
            @RequestBody String content) {
        log.info("Saving memory file: {}", fileName);
        
        try {
            // 只允许修改 AGENTS.md 和 KNOWLEDGE.md
            if (!fileName.equals("AGENTS.md") && !fileName.equals("knowledge/KNOWLEDGE.md")) {
                return Response.error("此文件不允许修改");
            }
            
            String filePath = resolveFilePath(fileName);
            Files.writeString(Path.of(filePath), content);
            return Response.ok();
        } catch (Exception e) {
            log.error("Failed to save memory file: {}", e.getMessage(), e);
            return Response.error("保存失败: " + e.getMessage());
        }
    }

    /**
     * 获取每日记忆文件列表
     */
    @GetMapping("/daily/list")
    @Operation(summary = "获取每日记忆列表", description = "获取所有每日记忆文件")
    public Response<List<String>> listDailyMemories() {
        log.info("Listing daily memories");
        
        try {
            String memoryDir = workspacePath + "/memory";
            File dir = new File(memoryDir);
            
            if (!dir.exists() || !dir.isDirectory()) {
                return Response.ok(List.of());
            }
            
            // 获取所有 .md 文件，按文件名排序（最新的在前）
            List<String> files = Arrays.stream(dir.listFiles((d, name) -> name.endsWith(".md")))
                    .map(File::getName)
                    .sorted(Comparator.reverseOrder())
                    .collect(Collectors.toList());
            
            return Response.ok(files);
        } catch (Exception e) {
            log.error("Failed to list daily memories: {}", e.getMessage(), e);
            return Response.error("获取列表失败: " + e.getMessage());
        }
    }

    /**
     * 读取每日记忆文件
     */
    @GetMapping("/daily/read")
    @Operation(summary = "读取每日记忆", description = "读取指定的每日记忆文件")
    public Response<String> readDailyMemory(@RequestParam("file") String fileName) {
        log.info("Reading daily memory: {}", fileName);
        
        try {
            // 安全检查：防止路径遍历攻击
            if (fileName.contains("..") || fileName.contains("/")) {
                return Response.error("非法文件名");
            }
            
            String filePath = workspacePath + "/memory/" + fileName;
            String content = Files.readString(Path.of(filePath));
            return Response.ok(content);
        } catch (Exception e) {
            log.error("Failed to read daily memory: {}", e.getMessage(), e);
            return Response.error("读取失败: " + e.getMessage());
        }
    }

    /**
     * 解析文件路径
     */
    private String resolveFilePath(String fileName) {
        switch (fileName) {
            case "MEMORY.md":
                return workspacePath + "/MEMORY.md";
            case "AGENTS.md":
                return workspacePath + "/AGENTS.md";
            case "knowledge/KNOWLEDGE.md":
                return workspacePath + "/knowledge/KNOWLEDGE.md";
            default:
                throw new IllegalArgumentException("不支持的文件: " + fileName);
        }
    }
}
