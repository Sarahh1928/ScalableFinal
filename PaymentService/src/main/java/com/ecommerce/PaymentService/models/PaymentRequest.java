package com.ecommerce.PaymentService.models;

public class PaymentRequest {
    private String[] paymentDetails;

    public String[] getPaymentDetails() {
        return paymentDetails;
    }

    public void setPaymentDetails(String[] paymentDetails) {
        this.paymentDetails = paymentDetails;
    }
}
