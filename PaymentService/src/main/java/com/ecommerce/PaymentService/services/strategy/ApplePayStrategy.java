package com.ecommerce.PaymentService.services.strategy;


public class ApplePayStrategy implements PaymentStrategy {
    private String applePayToken;

    public ApplePayStrategy(String applePayToken) {
        this.applePayToken = applePayToken;
    }

    @Override
    public boolean processPayment(double amount) {
        // Implement actual Apple Pay payment processing logic
        System.out.println("Processing Apple Pay payment of $" + amount);
        // Simulate payment processing
        return true;
    }

    @Override
    public String getPaymentMethodName() {
        return "Apple Pay";
    }
}