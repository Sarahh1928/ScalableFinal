package com.ecommerce.ProductService.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}") // Get the email address from application.properties
    private String fromEmail;

    public MailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }
    public void sendStockAlert(String toEmail, String productName, int stock) {
        if (toEmail == null || toEmail.isEmpty()) {
            throw new IllegalArgumentException("Recipient email must not be null or empty.");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Stock Alert: " + productName);
        message.setText("Attention!\n\nThe stock for product '" + productName + "' is running low (" + stock + " units left).\n\nPlease take action.");

        javaMailSender.send(message);
    }
}
