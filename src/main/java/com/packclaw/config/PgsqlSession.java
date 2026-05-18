package com.packclaw.config;

import com.packclaw.mapper.SessionChatMapper;
import com.packclaw.model.po.SessionChat;
import io.agentscope.core.session.ListHashUtil;
import io.agentscope.core.session.Session;
import io.agentscope.core.state.SessionKey;
import io.agentscope.core.state.SimpleSessionKey;
import io.agentscope.core.state.State;
import io.agentscope.core.util.JsonUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * PostgreSQL implementation of AgentScope Session interface
 */
@Component
public class PgsqlSession implements Session {
    private static final String HASH_KEY_SUFFIX = ":_hash";
    private static final int SINGLE_STATE_INDEX = 0;

    @Resource
    private SessionChatMapper sessionChatMapper;

    @Override
    public void save(SessionKey sessionKey, String key, State value) {
        String sessionId = sessionKey.toIdentifier();
        validateSessionId(sessionId);
        validateStateKey(key);

        try {
            String json = JsonUtils.getJsonCodec().toJson(value);

            SessionChat entity = new SessionChat();
            entity.setSessionId(sessionId);
            entity.setStateKey(key);
            entity.setItemIndex(SINGLE_STATE_INDEX);
            entity.setStateData(json);

            sessionChatMapper.upsertState(entity);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save state: " + key, e);
        }
    }

    @Override
    public void save(SessionKey sessionKey, String key, List<? extends State> values) {
        String sessionId = sessionKey.toIdentifier();
        validateSessionId(sessionId);
        validateStateKey(key);

        if (values.isEmpty()) {
            return;
        }

        String hashKey = key + HASH_KEY_SUFFIX;

        try {
            String currentHash = ListHashUtil.computeHash(values);
            String storedHash = getStoredHash(sessionId, hashKey);
            int existingCount = getListCount(sessionId, key);

            boolean needsFullRewrite = ListHashUtil.needsFullRewrite(
                currentHash, storedHash, values.size(), existingCount);

            if (needsFullRewrite) {
                sessionChatMapper.deleteByStateKey(sessionId, key);
                insertAllItems(sessionId, key, values);
                saveHash(sessionId, hashKey, currentHash);
            } else if (values.size() > existingCount) {
                List<? extends State> newItems = values.subList(existingCount, values.size());
                insertItems(sessionId, key, newItems, existingCount);
                saveHash(sessionId, hashKey, currentHash);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to save list: " + key, e);
        }
    }

    private String getStoredHash(String sessionId, String hashKey) {
        SessionChat entity = sessionChatMapper.selectByKey(sessionId, hashKey, SINGLE_STATE_INDEX);
        return entity != null ? entity.getStateData() : null;
    }

    private void saveHash(String sessionId, String hashKey, String hash) {
        SessionChat entity = new SessionChat();
        entity.setSessionId(sessionId);
        entity.setStateKey(hashKey);
        entity.setItemIndex(SINGLE_STATE_INDEX);
        entity.setStateData(hash);
        sessionChatMapper.upsertState(entity);
    }

    private void insertAllItems(String sessionId, String key, List<? extends State> values) {
        insertItems(sessionId, key, values, 0);
    }

    private void insertItems(String sessionId, String key, List<? extends State> items, int startIndex) {
        List<SessionChat> entities = new ArrayList<>();
        int index = startIndex;

        for (State item : items) {
            String json = JsonUtils.getJsonCodec().toJson(item);
            SessionChat entity = new SessionChat();
            entity.setSessionId(sessionId);
            entity.setStateKey(key);
            entity.setItemIndex(index);
            entity.setStateData(json);
            entities.add(entity);
            index++;
        }

        if (!entities.isEmpty()) {
            sessionChatMapper.batchInsertStates(entities);
        }
    }

    private int getListCount(String sessionId, String key) {
        Integer maxIndex = sessionChatMapper.selectMaxIndex(sessionId, key);
        return maxIndex != null ? maxIndex + 1 : 0;
    }

    @Override
    public <T extends State> Optional<T> get(SessionKey sessionKey, String key, Class<T> type) {
        String sessionId = sessionKey.toIdentifier();
        validateSessionId(sessionId);
        validateStateKey(key);

        try {
            SessionChat entity = sessionChatMapper.selectByKey(sessionId, key, SINGLE_STATE_INDEX);
            if (entity != null) {
                return Optional.of(JsonUtils.getJsonCodec().fromJson(entity.getStateData(), type));
            }
            return Optional.empty();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get state: " + key, e);
        }
    }

    @Override
    public <T extends State> List<T> getList(SessionKey sessionKey, String key, Class<T> itemType) {
        String sessionId = sessionKey.toIdentifier();
        validateSessionId(sessionId);
        validateStateKey(key);

        try {
            List<SessionChat> entities = sessionChatMapper.selectListByKey(sessionId, key);
            return entities.stream()
                .map(entity -> JsonUtils.getJsonCodec().fromJson(entity.getStateData(), itemType))
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to get list: " + key, e);
        }
    }

    @Override
    public boolean exists(SessionKey sessionKey) {
        String sessionId = sessionKey.toIdentifier();
        validateSessionId(sessionId);

        try {
            Integer result = sessionChatMapper.existsSession(sessionId);
            return result != null && result > 0;
        } catch (Exception e) {
            throw new RuntimeException("Failed to check session existence: " + sessionId, e);
        }
    }

    @Override
    public void delete(SessionKey sessionKey) {
        String sessionId = sessionKey.toIdentifier();
        validateSessionId(sessionId);

        try {
            sessionChatMapper.deleteBySessionId(sessionId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete session: " + sessionId, e);
        }
    }

    @Override
    public Set<SessionKey> listSessionKeys() {
        try {
            List<String> sessionIds = sessionChatMapper.selectAllSessionIds();
            return sessionIds.stream()
                .map(SimpleSessionKey::of)
                .collect(Collectors.toSet());
        } catch (Exception e) {
            throw new RuntimeException("Failed to list sessions", e);
        }
    }

    @Override
    public void close() {
        // Spring manages bean lifecycle
    }

    public int clearAllSessions() {
        try {
            return sessionChatMapper.clearAllSessions();
        } catch (Exception e) {
            throw new RuntimeException("Failed to clear sessions", e);
        }
    }

    private void validateSessionId(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }
        if (sessionId.contains("/") || sessionId.contains("\\")) {
            throw new IllegalArgumentException("Session ID cannot contain path separators");
        }
        if (sessionId.length() > 255) {
            throw new IllegalArgumentException("Session ID cannot exceed 255 characters");
        }
    }

    private void validateStateKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("State key cannot be null or empty");
        }
        if (key.length() > 255) {
            throw new IllegalArgumentException("State key cannot exceed 255 characters");
        }
    }
}
