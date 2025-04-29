package com.ecommerce.OrderService.Dto;

public class PaymentRequestDTO {

    private String[] paymentDetails;

    public String[] getPaymentDetails() {
        return paymentDetails;
    }

    public void setPaymentDetails(String[] paymentDetails) {
        this.paymentDetails = paymentDetails;
    }
}
