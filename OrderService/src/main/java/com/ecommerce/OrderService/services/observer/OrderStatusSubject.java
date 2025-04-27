package com.ecommerce.OrderService.services.observer;

import com.ecommerce.OrderService.models.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrderStatusSubject {
    private final List<EmailNotificationObserver> observers = new ArrayList<>();

    public void addObserver(EmailNotificationObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(EmailNotificationObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers(Order order) {
        for (EmailNotificationObserver observer : observers) {
            observer.update(order);
        }
    }
}
