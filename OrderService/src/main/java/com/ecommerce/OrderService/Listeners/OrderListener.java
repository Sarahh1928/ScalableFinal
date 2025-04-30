package com.ecommerce.OrderService.Listeners;

import com.ecommerce.OrderService.Dto.OrderCreationDTO;
import com.ecommerce.OrderService.services.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderListener {

    private final OrderService orderService;

    public OrderListener(OrderService orderService) {
        this.orderService = orderService;
    }

    // Listen for order creation request from RabbitMQ
    @RabbitListener(queues = "order")
    public void processOrder(OrderCreationDTO orderCreationDTO) {
        orderService.createOrder(orderCreationDTO.getToken(), orderCreationDTO.getTransactionId());
    }
}

