package com.ecommerce.OrderService.services.command;

import com.ecommerce.OrderService.models.Order;
import com.ecommerce.OrderService.models.enums.OrderStatus;
import com.ecommerce.OrderService.repositories.OrderRepository;
import com.ecommerce.OrderService.services.observer.EmailNotificationObserver;
import org.springframework.beans.factory.annotation.Autowired;

public class DeliverOrderCommand extends OrderCommand {

    private final OrderRepository orderRepository;

    public DeliverOrderCommand(Order order, OrderRepository orderRepository, EmailNotificationObserver emailNotificationObserver) {
        super(order);
        this.orderRepository = orderRepository;
    }

    @Override
    public void execute() {
        order.setStatus(OrderStatus.DELIVERED);
        orderRepository.save(order);
    }
}
