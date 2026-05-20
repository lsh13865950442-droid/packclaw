package com.packclaw.service.impl;

import com.alibaba.fastjson2.JSON;
import com.packclaw.mapper.SessionMapper;
import com.packclaw.model.po.Session;
import com.packclaw.service.ChatService;
import com.packclaw.utils.UUIDUtil;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.ChatModelBase;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    @Resource
    private ChatModelBase chatModelBase;

    @Resource
    private SessionMapper sessionMapper;

    @Override
    public String getSessionId(String ip, String query) {
        Session session = new Session();
        String id = UUIDUtil.getUUID();
        session.setId(id);
        String title = query.length() > 10 ? query.substring(0, 10) : query;
        session.setTitle(title);
        session.setIp(ip);
        session.setQuery(query);
        session.setStatus(true);
        session.setCreateTime(new Date());
        session.setUpdateTime(new Date());

        sessionMapper.insert(session);
        log.debug("Session inserted to database, id: {}", id);
        return id;
    }

    @Override
    public String generateSessionTitle(String sessionId) {
        Session session = sessionMapper.selectById(sessionId);
        if (session == null || session.getQuery() == null) {
            return "Untitled";
        }

        String query = session.getQuery();
        String prompt = "请为以下用户问题生成一个简洁的标题（10字以内），只返回标题，不要其他内容：\n" + query;

        try {
            ReActAgent agent = ReActAgent.builder()
                    .name("TitleAgent")
                    .sysPrompt("你是一个标题生成助手，只返回标题，不返回其他内容。")
                    .model(chatModelBase)
                    .maxIters(1)
                    .build();

            Msg response = agent.call(Msg.builder().textContent(prompt).build()).block();
            if (response != null && response.getTextContent() != null) {
                String title = response.getTextContent().trim();
                if (!title.isEmpty() && title.length() <= 100) {
                    sessionMapper.updateNameById(sessionId, title);
                    return title;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to generate title for session: {}", sessionId, e);
        }

        // Fallback to query substring
        return query.length() > 10 ? query.substring(0, 10) : query;
    }

    @Override
    public List<String> generateNextSuggestions(String sessionId) {
        try {
            Session session = sessionMapper.selectById(sessionId);
            String userQuery = session != null ? session.getQuery() : "";

            String prompt = "请基于以下用户问题生成3个可能的后续问题或指令。返回一个JSON数组，只包含3个字符串，例如：[\"问题1\", \"问题2\", \"问题3\"]\n\n用户问题: " + userQuery;

            ReActAgent agent = ReActAgent.builder()
                    .name("SuggestionsAgent")
                    .sysPrompt("你是一个后续问题生成助手，只返回JSON数组，不返回其他内容。")
                    .model(chatModelBase)
                    .maxIters(1)
                    .build();

            Msg response = agent.call(Msg.builder().textContent(prompt).build()).block();
            if (response != null && response.getTextContent() != null) {
                String textContent = response.getTextContent().trim();
                try {
                    String[] suggestions = JSON.parseObject(textContent, String[].class);
                    if (suggestions != null && suggestions.length > 0) {
                        return List.of(suggestions);
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse suggestions JSON, text: {}", textContent, e);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to generate suggestions for session: {}", sessionId, e);
        }

        return new ArrayList<>();
    }
}
