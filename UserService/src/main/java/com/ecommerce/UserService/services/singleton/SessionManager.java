package com.ecommerce.UserService.services.singleton;

import com.ecommerce.UserService.models.UserSession;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
public class SessionManager {

    private static SessionManager instance;

    // Maps for fast lookup
    private final Map<String, UserSession> activeSessionsByToken = new HashMap<>();
    private final Map<Long, String> userIdToToken = new HashMap<>();

    private RedisTemplate<String, UserSession> redisTemplate;

    private SessionManager() {
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setRedisTemplate(RedisTemplate<String, UserSession> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void addSession(String token, UserSession userSession) {
        activeSessionsByToken.put(token, userSession);
        userIdToToken.put(userSession.getUserId(), token);

        if (redisTemplate != null) {
            // Set with TTL, no need to call expire separately
            redisTemplate.opsForValue().set(token, userSession, Duration.ofHours(6));
        }
    }

    public void invalidateToken(String token) {
        UserSession session = activeSessionsByToken.remove(token);
        if (session != null) {
            userIdToToken.remove(session.getUserId());
        }
        if (redisTemplate != null) {
            redisTemplate.delete(token);
        }
    }

    public boolean isTokenValid(String token) {
        if (!activeSessionsByToken.containsKey(token)) {
            if (redisTemplate == null || redisTemplate.opsForValue().get(token) == null) {
                return false;
            }
        } else {
            // If Redis says token is gone, clean it from memory
            if (redisTemplate != null && redisTemplate.opsForValue().get(token) == null) {
                UserSession staleSession = activeSessionsByToken.remove(token);
                if (staleSession != null) {
                    userIdToToken.remove(staleSession.getUserId());
                }
                return false;
            }
        }
        return true;
    }


    public UserSession getSession(String token) {
        UserSession session = activeSessionsByToken.get(token);

        if (session == null && redisTemplate != null) {
            session = redisTemplate.opsForValue().get(token);
            if (session != null) {
                activeSessionsByToken.put(token, session);
                userIdToToken.put(session.getUserId(), token);
            } else {
                // token expired, remove stale entry just in case
                activeSessionsByToken.remove(token);
            }
        }

        return session;
    }

    public UserSession getSessionByUserId(Long userId) {
        String token = userIdToToken.get(userId);
        if (token == null) {
            return null; // no session, force re-login
        }

        UserSession session = activeSessionsByToken.get(token);

        if (session != null) {
            if (redisTemplate != null) {
                session = redisTemplate.opsForValue().get(token);

                if (session == null) {
                    activeSessionsByToken.remove(token);
                    userIdToToken.remove(userId);
                    return null;
                } else {
                    activeSessionsByToken.put(token, session);
                    userIdToToken.put(userId, token);
                }
            }
        } else {
            return null;
        }

        return session;
    }

}