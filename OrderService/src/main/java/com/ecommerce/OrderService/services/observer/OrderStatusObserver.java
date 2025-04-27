package com.ecommerce.OrderService.services.observer;

import com.ecommerce.OrderService.models.Order;

public interface OrderStatusObserver {
    void update(Order order);
}
