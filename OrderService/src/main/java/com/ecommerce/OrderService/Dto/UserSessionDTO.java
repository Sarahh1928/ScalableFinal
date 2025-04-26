package com.ecommerce.OrderService.Dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserSessionDTO {
    private String token;
    private Long userId;
    private String role;

    // No-arg constructor
    public UserSessionDTO() {}

    // All-arg constructor
    public UserSessionDTO(String token, Long userId, String role) {
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
