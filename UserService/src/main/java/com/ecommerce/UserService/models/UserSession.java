package com.ecommerce.UserService.models;

public class UserSession {
    private String token;
    private Long userId;
    private String role;

    // Constructor
    public UserSession() {}

    public UserSession(String token, Long userId, String role) {
        this.token = token;
        this.userId = userId;
        this.role = role;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
