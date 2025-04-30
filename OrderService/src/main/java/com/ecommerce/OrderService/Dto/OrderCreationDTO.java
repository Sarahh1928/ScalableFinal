package com.ecommerce.OrderService.Dto;

public class OrderCreationDTO {
    private String token;
    private Long transactionId;

    public OrderCreationDTO(String token, Long transactionId) {
            this.token = token;
            this.transactionId = transactionId;
    }

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
    public String toString(){
        System.out.println("token: "+token+" transactionId: "+transactionId);
        return "token: "+token+" transactionId: "+transactionId;
    }
// Getters and Setters
    }


