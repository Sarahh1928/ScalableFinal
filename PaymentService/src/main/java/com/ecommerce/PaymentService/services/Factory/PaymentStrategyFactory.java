package com.ecommerce.PaymentService.services.Factory;


import com.ecommerce.PaymentService.models.enums.PaymentMethod;
import com.ecommerce.PaymentService.services.strategy.*;

public class PaymentStrategyFactory {
    public static PaymentStrategy createPaymentStrategy(PaymentMethod method, Object... params) {
        switch (method) {
            case CREDIT_CARD:
                return new CreditCardStrategy(
                        (String) params[0], // cardNumber
                        (String) params[1], // expiryDate
                        (String) params[2]  // cvv
                );
            case PAYPAL:
                return new PaypalStrategy(
                        (String) params[0], // email
                        (String) params[1]  // password
                );
            case APPLE_PAY:
                return new ApplePayStrategy(
                        (String) params[0]  // applePayToken
                );
            default:
                throw new IllegalArgumentException("Unsupported payment method");
        }
    }
}