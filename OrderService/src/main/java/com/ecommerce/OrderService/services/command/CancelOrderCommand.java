package com.ecommerce.OrderService.services.command;

import com.ecommerce.OrderService.models.Order;
import com.ecommerce.OrderService.models.enums.OrderStatus;
import com.ecommerce.OrderService.repositories.OrderRepository;

public class CancelOrderCommand extends OrderCommand {

    private final OrderRepository orderRepository;

    public CancelOrderCommand(Order order, OrderRepository orderRepository) {
        super(order);
        this.orderRepository = orderRepository;
    }

    @Override
    public void execute() {
        if(!order.getStatus().equals(OrderStatus.CONFIRMED)) {
            throw new RuntimeException("Order Can't Be Cancelled");
        }
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);  // Persist the changes in DB
    }
}