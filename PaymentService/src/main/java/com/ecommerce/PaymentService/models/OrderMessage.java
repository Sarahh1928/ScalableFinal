package com.ecommerce.PaymentService.models;

public class OrderMessage {
    private String token;
    private Long transactionId;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }
// Getters and setters
}
