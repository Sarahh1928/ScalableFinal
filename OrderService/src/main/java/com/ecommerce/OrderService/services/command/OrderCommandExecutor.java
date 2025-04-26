package com.ecommerce.OrderService.services.command;

import java.util.List;

public class OrderCommandExecutor {

    private List<OrderCommand> commands;

    public OrderCommandExecutor(List<OrderCommand> commands) {
        this.commands = commands;
    }

    public void executeCommands() {
        // Iterate over each command and execute it
        for (OrderCommand command : commands) {
            command.execute();
        }
    }
}

