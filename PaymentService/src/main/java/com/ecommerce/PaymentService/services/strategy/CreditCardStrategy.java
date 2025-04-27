package com.ecommerce.PaymentService.services.strategy;

public class CreditCardStrategy implements PaymentStrategy {
    private String cardNumber;
    private String expiryDate;
    private String cvv;

    public CreditCardStrategy(String cardNumber, String expiryDate, String cvv) {
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
    }

    @Override
    public boolean processPayment(double amount) {
        // Implement actual credit card payment processing logic
        System.out.println("Processing credit card payment of $" + amount);
        // Simulate payment processing
        return true;
    }

    @Override
    public String getPaymentMethodName() {
        return "Credit Card";
    }
}