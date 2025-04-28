package com.ecommerce.ProductService.Dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserSessionDTO {
    private String token;
    private Long userId;
    private String role;
    private String email;

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
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }


    @Override
    public String toString() {
        return "UserSessionDTO{" +
                "token='" + token + '\'' +
                ", userId=" + userId +
                ", role='" + role + '\'' +
                '}';
    }
}

