package com.ecommerce.OrderService.controllers;

import com.ecommerce.OrderService.models.Order;
import com.ecommerce.OrderService.services.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // Utility method to extract the token from the Authorization header
    private String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);  // Remove "Bearer " prefix
        }
        return null;  // If the header doesn't contain a Bearer token, return null
    }

    // POST: Create a new order
    @PostMapping("/checkout")
    public ResponseEntity<String> makeOrder(@RequestHeader("Authorization") String token) {
        orderService.createOrder(extractToken(token));
        return ResponseEntity.status(HttpStatus.CREATED).body("Order created successfully.");
    }

    // GET: Read an order by ID
    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(@PathVariable Long orderId) {
        Order order = orderService.getOrderById(orderId);
        return ResponseEntity.status(HttpStatus.OK).body(order);
    }

    // PUT: Update an existing order
    @PutMapping("/{orderId}")
    public ResponseEntity<Order> updateOrder(
            @PathVariable Long orderId,
            @RequestBody Order updatedOrder) {
        Order order = orderService.updateOrder(orderId, updatedOrder);
        return ResponseEntity.status(HttpStatus.OK).body(order);
    }

    // DELETE: Delete an order by ID
    @DeleteMapping("/{orderId}")
    public ResponseEntity<String> deleteOrder(@PathVariable Long orderId) {
        orderService.deleteOrder(orderId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Order deleted successfully.");
    }

    // GET: List all orders (Optional, might be useful for admins)
    @GetMapping("/all")
    public ResponseEntity<Iterable<Order>> getAllOrders() {
        Iterable<Order> orders = orderService.getAllOrders();
        return ResponseEntity.status(HttpStatus.OK).body(orders);
    }

    // Cancel Order API
    @PostMapping("/cancel/{orderId}")
    public ResponseEntity<String> cancelOrder(@PathVariable Long orderId) {
        try {
            orderService.cancelOrder(orderId);
            return ResponseEntity.status(HttpStatus.OK).body("Order cancelled successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error cancelling order: " + e.getMessage());
        }
    }

    // Refund Order API
    @PostMapping("/refund/{orderId}")
    public ResponseEntity<String> refundOrder(@PathVariable Long orderId) {
        try {
            orderService.refundOrder(orderId);
            return ResponseEntity.status(HttpStatus.OK).body("Order refunded successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error refunding order: " + e.getMessage());
        }
    }

    // Ship Order API
    @PostMapping("/ship/{orderId}")
    public ResponseEntity<String> shipOrder(@PathVariable Long orderId) {
        try {
            orderService.shipOrder(orderId);
            return ResponseEntity.status(HttpStatus.OK).body("Order shipped successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error shipping order: " + e.getMessage());
        }
    }

    // Deliver Order API
    @PostMapping("/deliver/{orderId}")
    public ResponseEntity<String> deliverOrder(@PathVariable Long orderId) {
        try {
            orderService.deliverOrder(orderId);
            return ResponseEntity.status(HttpStatus.OK).body("Order delivered successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error delivering order: " + e.getMessage());
        }
    }
}
