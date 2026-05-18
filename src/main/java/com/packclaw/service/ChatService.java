package com.packclaw.service;

public interface ChatService {

    /**
     * Create a session
     * @param ip User IP
     * @param query User message
     * @return Session ID
     */
    String getSessionId(String ip, String query);

    /**
     * Generate session title
     * @param sessionId Session ID
     * @return Session title
     */
    String generateSessionTitle(String sessionId);
}
