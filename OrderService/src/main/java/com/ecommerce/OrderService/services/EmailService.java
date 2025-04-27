package com.ecommerce.OrderService.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}") // Get the email address from application.properties
    private String fromEmail;

    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }


    public void sendEmail(String userEmail, String subject, String message) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(fromEmail);
            mailMessage.setTo(userEmail);
            mailMessage.setSubject(subject);
            mailMessage.setText(message);

            javaMailSender.send(mailMessage);  // 🚀 Actually sends the email

            System.out.println("Email sent successfully to " + userEmail);
        } catch (Exception e) {
            System.err.println("Failed to send email to " + userEmail);
            e.printStackTrace();  // Don't be shy, throw that ugly error in console
            throw new RuntimeException("Failed to send email", e);
        }
    }

}
