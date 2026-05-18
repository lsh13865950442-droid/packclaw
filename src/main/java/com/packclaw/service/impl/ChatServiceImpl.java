package com.packclaw.service.impl;

import com.packclaw.manager.SimpleAgentManager;
import com.packclaw.mapper.SessionMapper;
import com.packclaw.model.po.Session;
import com.packclaw.prompt.PromptConstants;
import com.packclaw.service.ChatService;
import com.packclaw.utils.UUIDUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    @Resource
    private SimpleAgentManager simpleAgentManager;

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
        String title = simpleAgentManager.call(query, PromptConstants.GENERATE_SESSION_TITLE);

        if (title == null || title.length() > 100) {
            return query.length() > 10 ? query.substring(0, 10) : query;
        }

        sessionMapper.updateNameById(sessionId, title);
        return title;
    }
}
