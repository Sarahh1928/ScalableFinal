package com.ecommerce.OrderService.services.observer;

import com.ecommerce.OrderService.models.Order;
import com.ecommerce.OrderService.services.EmailService;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationObserver implements OrderStatusObserver {

    private final EmailService emailService; // you MUST have an EmailService

    public EmailNotificationObserver(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void update(Order order) {
        String subject = "Order Status Updated!";
        String message = "Hey, your order #" + order.getId() +
                " status changed to: " + order.getStatus();
        emailService.sendEmail(order.getUserEmail(), subject, message);
    }
}
