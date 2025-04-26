package com.ecommerce.OrderService.services.command;

import com.ecommerce.OrderService.models.Order;
import com.ecommerce.OrderService.models.enums.OrderStatus;
import com.ecommerce.OrderService.repositories.OrderRepository;

public class DeliverOrderCommand extends OrderCommand {

    private final OrderRepository orderRepository;

    // Constructor to inject order and repository
    public DeliverOrderCommand(Order order, OrderRepository orderRepository) {
        super(order);
        this.orderRepository = orderRepository;
    }

    // The execute method contains the logic to deliver the order
    @Override
    public void execute() {
        // Add any logic required to deliver the order (e.g., notify customer, update delivery details)

        // Update the order status to DELIVERED
        order.setStatus(OrderStatus.DELIVERED);

        // Persist the changes to the database
        orderRepository.save(order);
    }
}
