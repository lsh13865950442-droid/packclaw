package com.packclaw.tools;

import io.agentscope.core.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class GetCurrentTimeTool {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Tool(name = "get_current_time", description = "获取当前系统时间，当需要知道当前日期或时间时调用此工具")
    public String getCurrentTime() {
        String currentTime = LocalDateTime.now().format(FORMATTER);
        log.info("获取当前时间: {}", currentTime);
        return currentTime;
    }
}
