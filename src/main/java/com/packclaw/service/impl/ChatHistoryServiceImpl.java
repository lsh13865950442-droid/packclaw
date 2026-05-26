package com.packclaw.service.impl;

import com.packclaw.common.domain.TableDataVO;
import com.packclaw.mapper.SessionMapper;
import com.packclaw.model.po.Session;
import com.packclaw.model.resp.ChatDetailsResp;
import com.packclaw.model.resp.ChatListResp;
import com.packclaw.service.ChatHistoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.agentscope.core.message.Msg;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 会话历史记录服务实现类
 *
 * @author PackClaw
 * @version 1.0
 */
@Slf4j
@Service
public class ChatHistoryServiceImpl implements ChatHistoryService {

    @Resource
    private SessionMapper sessionMapper;

    @Value("${packclaw.workspace.path:./data/workspace}")
    private String workspacePath;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public TableDataVO<ChatListResp> getChatHistoryList(Integer pageNum, Integer pageSize, String name) {
        // 查询所有活跃会话
        List<Session> sessions = sessionMapper.selectByUserIdAndName(name);

        // 手动分页
        int total = sessions.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, total);

        List<Session> pageSessions = sessions.subList(Math.max(0, start), Math.min(end, total));

        // 转换为响应对象
        List<ChatListResp> respList = pageSessions.stream()
                .map(this::convertToChatListResp)
                .collect(Collectors.toList());

        return TableDataVO.<ChatListResp>builder()
                .rows(respList)
                .total((long) total)
                .build();
    }

    @Override
    public ChatDetailsResp getChatDetailsById(String chatId) {
        // 查询会话基本信息
        Session session = sessionMapper.selectById(chatId);
        if (session == null) {
            log.warn("会话不存在, chatId: {}", chatId);
            return null;
        }

        // 从 Harness JSONL 文件读取消息
        List<Msg> msgList = readSessionMessages(chatId);

        return ChatDetailsResp.builder()
                .chatId(session.getId())
                .title(session.getTitle())
                .createTime(session.getCreateTime())
                .updateTime(session.getUpdateTime())
                .messages(msgList)
                .build();
    }

    /**
     * 从 Harness workspace 目录读取会话消息
     * Harness 将消息存储在 workspace/agents/PackClawAgent/sessions/<sessionId>.log.jsonl
     */
    private List<Msg> readSessionMessages(String chatId) {
        List<Msg> messages = new ArrayList<>();
        Path workspace = Path.of(workspacePath).toAbsolutePath();

        Path jsonlPath = workspace.resolve("agents")
                .resolve("PackClawAgent")
                .resolve("sessions")
                .resolve(chatId + ".log.jsonl");

        log.info("Reading session messages from: {} (exists: {})", jsonlPath.toAbsolutePath(), Files.exists(jsonlPath));

        if (!Files.exists(jsonlPath)) {
            log.info("No JSONL file found for session: {}", chatId);
            return messages;
        }

        try (BufferedReader reader = Files.newBufferedReader(jsonlPath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                try {
                    Msg msg = objectMapper.readValue(line, Msg.class);
                    // 过滤掉压缩摘要消息
                    if (msg.getName() != null && "__compaction_summary__".equals(msg.getName())) {
                        log.debug("Skipping compaction summary message for session {}", chatId);
                        continue;
                    }
                    messages.add(msg);
                } catch (Exception e) {
                    log.debug("Skipping invalid JSON line in session {}: {}", chatId, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to read session messages from: {}", jsonlPath, e);
        }
        log.info("Read {} messages for session {}", messages.size(), chatId);
        return messages;
    }

    @Override
    public int logicDeleteChatHistory(String chatId) {
        log.info("逻辑删除会话, chatId: {}", chatId);
        return sessionMapper.deleteById(chatId);
    }

    @Override
    public int updateChatTitle(String chatId, String title) {
        log.info("更新会话标题, chatId: {}, title: {}", chatId, title);
        return sessionMapper.updateNameById(chatId, title);
    }

    /**
     * 将Session转换为ChatListResp
     */
    private ChatListResp convertToChatListResp(Session session) {
        return ChatListResp.builder()
                .chatId(session.getId())
                .title(session.getTitle())
                .createTime(session.getCreateTime())
                .updateTime(session.getUpdateTime())
                .build();
    }
}
