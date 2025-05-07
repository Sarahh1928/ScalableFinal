package com.ecommerce.PaymentService.services.strategy;

import com.ecommerce.PaymentService.clients.UserServiceClient;
import com.ecommerce.PaymentService.dto.UserDto;
import com.ecommerce.PaymentService.dto.WalletUpdateRequest;
import com.ecommerce.PaymentService.services.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.font.TextHitInfo;

public class WalletStrategy implements PaymentStrategy {

    private static final Logger logger = LoggerFactory.getLogger(WalletStrategy.class);

    private final UserServiceClient userServiceClient;
    private final Long userId;
    private final String token; // Bearer token
    private double currentWalletBalance;
    public MailService mailService;

    public WalletStrategy( Long userId, String token,MailService mailService,UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
        this.userId = userId;
        this.token = token;
        this.currentWalletBalance=0.0;
        this.mailService=mailService;
    }


    @Override
    public boolean processPayment(double amount) {
        logger.info("Starting processPayment with amount: {}", amount);

        try {
            UserDto user = userServiceClient.getUser(userId, "Bearer " + token);
            double currentWalletBalance = user.getWallet();

            logger.info("Fetched user wallet balance: {}", currentWalletBalance);

            if (currentWalletBalance < amount) {
                logger.warn("Insufficient balance: {}, required: {}", currentWalletBalance, amount);
                mailService.sendEmail(
                        user.getEmail(),
                        "Payment Failed",
                        "Your payment of $" + amount + " failed due to insufficient wallet balance. Your current balance is $" + currentWalletBalance + "."
                );
                return false;
            }

            double newBalance = currentWalletBalance - amount;
            userServiceClient.updateWallet("Bearer " + token, userId, newBalance);

            logger.info("Wallet updated successfully to: {}", newBalance);

            mailService.sendEmail(
                    user.getEmail(),
                    "Payment Successful",
                    "Your payment of $" + amount + " was successful. Your new wallet balance is $" + newBalance + "."
            );

            return true;

        } catch (Exception e) {
            logger.error("Error processing payment: {}", e.getMessage(), e);
            try {
                mailService.sendEmail(
                        "admin@example.com", // Replace with your admin email or system monitor
                        "Payment Processing Error",
                        "Error processing payment for user ID " + userId + ": " + e.getMessage()
                );
            } catch (Exception mailEx) {
                logger.error("Failed to send error notification email: {}", mailEx.getMessage(), mailEx);
            }

            throw new RuntimeException("Payment processing failed", e);
        }
    }

    @Override
    public String getPaymentMethodName() {
        return "Wallet";
    }
}
