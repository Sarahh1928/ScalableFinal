package com.ecommerce.PaymentService.services.strategy;

public class PaypalStrategy implements PaymentStrategy {
    private String email;
    private String password;

    public PaypalStrategy(String email, String password) {
        this.email = email;
        this.password = password;
    }

    @Override
    public boolean processPayment(double amount) {
        // Implement actual PayPal payment processing logic
        System.out.println("Processing PayPal payment of $" + amount);
        // Simulate payment processing
        return true;
    }

    @Override
    public String getPaymentMethodName() {
        return "PayPal";
    }
}