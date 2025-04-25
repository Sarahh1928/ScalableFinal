package com.ecommerce.UserService.services.singleton;

import com.ecommerce.UserService.models.UserSession;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SessionManager {
    private static SessionManager instance;
    private final Map<String, UserSession> activeSessions = new HashMap<>();
    private final Set<String> invalidatedTokens = new HashSet<>();

    private SessionManager() {}

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void addSession(String token, UserSession userSession) {
        activeSessions.put(token, userSession);
    }

    public void invalidateToken(String token) {
        invalidatedTokens.add(token);
        activeSessions.remove(token); // Optionally, remove from in-memory session manager
    }

    public boolean isTokenValid(String token) {
        return !invalidatedTokens.contains(token) && activeSessions.containsKey(token);
    }

    public UserSession getSession(String token) {
        return activeSessions.get(token);
    }

    public UserSession getSessionByUserId(Long userId) {
        // Find session by userId in the in-memory activeSessions map
        return activeSessions.values().stream()
                .filter(session -> session.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
    }
}
