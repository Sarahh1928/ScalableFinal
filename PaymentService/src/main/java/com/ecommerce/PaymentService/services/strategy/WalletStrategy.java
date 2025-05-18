package com.ecommerce.PaymentService.services.strategy;

import com.ecommerce.PaymentService.clients.UserServiceClient;
import com.ecommerce.PaymentService.dto.UserDto;
import com.ecommerce.PaymentService.dto.WalletUpdateRequest;
import com.ecommerce.PaymentService.services.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

public class WalletStrategy implements PaymentStrategy {

    private static final Logger logger = LoggerFactory.getLogger(WalletStrategy.class);

    private final UserServiceClient userServiceClient;
    private final Long userId;
    private final String token; // Bearer token
    private double currentWalletBalance;

    public WalletStrategy( Long userId, String token,UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
        this.userId = userId;
        this.token = token;
        this.currentWalletBalance=0.0;

    }

    @Override
    public boolean processPayment(double amount) {
        logger.info("Starting processPayment with amount: {}", amount);
        logger.info("Starting processPayment with token: {}", token);

        logger.info("Fetched user with wallet balance1111111111111: {}", userServiceClient.getUser(userId,"Bearer " + token));
        UserDto user = userServiceClient.getUser(userId,"Bearer " + token);
        logger.info("Fetched user with wallet balance: {}", user.getWallet());

        this.currentWalletBalance = user.getWallet();

        if (currentWalletBalance < amount) {
            logger.warn("Insufficient balance: {}, required: {}", currentWalletBalance, amount);
            throw new RuntimeException("Insufficient balance: " + currentWalletBalance + " required: " + amount);
        }

        double newBalance = currentWalletBalance - amount;
        logger.info("New balance calculated: {}", newBalance);

        WalletUpdateRequest request = new WalletUpdateRequest(newBalance);

        try {
            userServiceClient.updateWallet("Bearer " + token, userId, newBalance);
            logger.info("Wallet updated successfully!");
            return true;
        } catch (Exception e) {
            logger.error("Error updating wallet: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update wallet: " + e.getMessage());
        }
    }

    @Override
    public String getPaymentMethodName() {
        return "Wallet";
    }
}
