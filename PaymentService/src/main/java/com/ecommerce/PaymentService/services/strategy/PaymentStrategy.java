package com.ecommerce.PaymentService.services.strategy;


public interface PaymentStrategy {
    boolean processPayment(double amount);
    String getPaymentMethodName();
}