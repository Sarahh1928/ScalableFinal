package com.ecommerce.OrderService.services.command;

import com.ecommerce.OrderService.models.Order;

public abstract class OrderCommand {
    protected Order order;

    public OrderCommand(Order order) {
        this.order = order;
    }

    public abstract void execute();  // Each command will have a specific implementation of execute()
}

