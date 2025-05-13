package com.ecommerce.UserService.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}") // Get the email address from application.properties
    private String fromEmail;

    @Value("${user.service.base-url}") // Get the email address from application.properties
    private String baseUrl;

    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        String resetLink = baseUrl+"/users/reset?token=" + resetToken;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Password Reset Request");
        message.setText("Click on the link to reset your password: " + resetLink);
        javaMailSender.send(message);
    }
    public void sendEmailVerification(String toEmail, String verificationToken) {
        String verificationLink = baseUrl+ "/users/verify-email?token=" + verificationToken;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Email Verification");
        message.setText("Please verify your email by clicking the following link: " + verificationLink);
        javaMailSender.send(message);
    }
}
