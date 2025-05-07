package com.ecommerce.PaymentService.services.Factory;


import com.ecommerce.PaymentService.clients.UserServiceClient;
import com.ecommerce.PaymentService.dto.UserDto;
import com.ecommerce.PaymentService.models.enums.PaymentMethod;
import com.ecommerce.PaymentService.services.MailService;
import com.ecommerce.PaymentService.services.strategy.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentStrategyFactory {

    private UserServiceClient userServiceClient;

    @Autowired
    private MailService mailService;
    @Autowired
    public PaymentStrategyFactory(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }
    public PaymentStrategy createPaymentStrategy(Long userId,String token,PaymentMethod method, Object... params) {

        switch (method) {
            case CREDIT_CARD:
                return new CreditCardStrategy(
                        (String) params[0], // cardNumber
                        (String) params[1], // expiryDate
                        (String) params[2]  // cvv
                );
            case WALLET:
                return new WalletStrategy(
                        userId,
                        token
                        ,mailService,userServiceClient
                );
            default:
                throw new IllegalArgumentException("Unsupported payment method");
        }
    }
}