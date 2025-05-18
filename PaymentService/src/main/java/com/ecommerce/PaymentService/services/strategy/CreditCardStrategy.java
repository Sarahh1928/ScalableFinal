package com.ecommerce.PaymentService.services.strategy;


import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

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
        Stripe.apiKey="sk_test_51RIuE7B1hAAEfUPLesi319NQR01TUBIp5YFUN8oB01AzEbgu1XpPOfZXWvhfP6gDVEXHYvJrCyG9jIX1emSnHC8100lAz3jNdA";
        try {
            if (!isValidCardNumber(cardNumber) || !isValidExpiryDate(expiryDate) || !isValidCvv(cvv)) {
                return false;
            }


            PaymentIntentCreateParams intentParams = PaymentIntentCreateParams.builder()
                    .setAmount((long) (amount * 100)) // Amount in cents
                    .setCurrency("usd")
                    .setPaymentMethod("pm_card_visa")
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                                    .build()
                    )
                    .build();

            // Create the PaymentIntent
            PaymentIntent intent = PaymentIntent.create(intentParams);

            // Step 3: Confirm the PaymentIntent
            PaymentIntent confirmedIntent = intent.confirm();

            // Check if the payment succeeded
            if ("succeeded".equals(confirmedIntent.getStatus())) {
                return true;
            } else {
                // Log the failure reason (Stripe error details)
                throw new RuntimeException("Payment failed: " + confirmedIntent.getStatus()+"      hihihihihih     "+PaymentIntent.create(intentParams));
            }
        } catch (StripeException e) {
            e.printStackTrace();
            // Log the exception message
            throw new RuntimeException("Payment failed StripeException: " + e.getMessage());
        }
    }



    @Override
    public String getPaymentMethodName() {
        return "Credit Card";
    }
    private boolean isValidCardNumber(String cardNumber) {
        return cardNumber != null && cardNumber.matches("\\d{16}");
    }

    private boolean isValidExpiryDate(String expiryDate) {
        if (expiryDate == null || !expiryDate.matches("(0[1-9]|1[0-2])/\\d{2}")) {
            return false;
        }
        String[] parts = expiryDate.split("/");
        int month = Integer.parseInt(parts[0]);
        int year = Integer.parseInt(parts[1]) + 2000; // Convert to full year, e.g., "26" -> 2026

        java.time.YearMonth now = java.time.YearMonth.now();
        java.time.YearMonth expiry = java.time.YearMonth.of(year, month);

        return expiry.isAfter(now) || expiry.equals(now);
    }

    private boolean isValidCvv(String cvv) {
        return cvv != null && cvv.matches("\\d{3}");
    }

}
