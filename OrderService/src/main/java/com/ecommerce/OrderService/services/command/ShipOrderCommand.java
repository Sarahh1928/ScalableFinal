package com.ecommerce.OrderService.services.command;

import com.ecommerce.OrderService.models.Order;
import com.ecommerce.OrderService.models.enums.OrderStatus;
import com.ecommerce.OrderService.repositories.OrderRepository;

public class ShipOrderCommand extends OrderCommand {

    private final OrderRepository orderRepository;

    public ShipOrderCommand(Order order, OrderRepository orderRepository) {
        super(order);
        this.orderRepository = orderRepository;
    }

    @Override
    public void execute() {
        // Logic to ship the order (for example, generate shipping labels, notify warehouse, etc.)
        if(!order.getStatus().equals(OrderStatus.CONFIRMED)) {
            throw new RuntimeException("Order Can't Be Shipped");
        }
        // Update the order status to SHIPPED
        order.setStatus(OrderStatus.SHIPPED);
        orderRepository.save(order);  // Persist the changes in DB
    }
}

