package com.ecommerce.OrderService.services.observer;

import com.ecommerce.OrderService.models.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrderStatusSubject {
    private final List<OrderStatusObserver> observers = new ArrayList<>();

    public void addObserver(OrderStatusObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(OrderStatusObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers(Order order) {
        for (OrderStatusObserver observer : observers) {
            observer.update(order);
        }
    }
}
