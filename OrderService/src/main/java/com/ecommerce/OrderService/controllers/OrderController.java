package com.ecommerce.OrderService.controllers;

import com.ecommerce.OrderService.Dto.PaymentMethodDTO;
import com.ecommerce.OrderService.Dto.PaymentRequestDTO;
import com.ecommerce.OrderService.models.Order;
import com.ecommerce.OrderService.models.RefundRequest;
import com.ecommerce.OrderService.services.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.List;

@RestController
@RequestMapping("/orders")
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

    @PostMapping("/checkoutOrder")
    public ResponseEntity<String> checkoutOrder(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam PaymentMethodDTO paymentMethod,
            @RequestBody PaymentRequestDTO paymentRequestDTO
    ) {
        extractToken(authorizationHeader);
        orderService.checkoutOrder(extractToken(authorizationHeader), paymentMethod, paymentRequestDTO);
        return ResponseEntity.ok("Order checkout initiated successfully.");
    }

    // POST: Create a new order
    @PostMapping("/checkout")
    public ResponseEntity<String> makeOrder(@RequestHeader("Authorization") String token, @RequestParam Long transactionId) {
        orderService.createOrder(extractToken(token), transactionId);
        return ResponseEntity.status(HttpStatus.CREATED).body("Order created successfully.");
    }

    // GET: Read an order by ID
    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(@RequestHeader("Authorization") String token, @PathVariable Long orderId) {
        Order order = orderService.getOrderById(extractToken(token), orderId);
        return ResponseEntity.status(HttpStatus.OK).body(order);
    }

    // PUT: Update an existing order
    @PutMapping("/{orderId}")
    public ResponseEntity<Order> updateOrder(
            @RequestHeader("Authorization") String token,
            @PathVariable Long orderId,
            @RequestBody Order updatedOrder) {
        Order order = orderService.updateOrder(extractToken(token),orderId, updatedOrder);
        return ResponseEntity.status(HttpStatus.OK).body(order);
    }

    // DELETE: Delete an order by ID
    @DeleteMapping("/{orderId}")
    public ResponseEntity<String> deleteOrder(@RequestHeader("Authorization") String token, @PathVariable Long orderId) {
        orderService.deleteOrder(extractToken(token), orderId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Order deleted successfully.");
    }

    // GET: List all orders (Optional, might be useful for admins)
    @GetMapping("/all")
    public ResponseEntity<Iterable<Order>> getAllOrders(@RequestHeader("Authorization") String token) {
        Iterable<Order> orders = orderService.getAllOrders(extractToken(token));
        return ResponseEntity.status(HttpStatus.OK).body(orders);
    }

    // Cancel Order API
    @PostMapping("/cancel/{orderId}")
    public ResponseEntity<String> cancelOrder(@RequestHeader("Authorization") String token, @PathVariable Long orderId) {
        try {
            orderService.cancelOrder(extractToken(token), orderId);
            return ResponseEntity.status(HttpStatus.OK).body("Order cancelled successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error cancelling order: " + e.getMessage());
        }
    }

    // Refund Order API
    @PostMapping("/acceptRefund/{orderId}")
    public ResponseEntity<String> refundOrder(@RequestHeader("Authorization") String token, @PathVariable Long orderId) {
        try {
            orderService.refundOrder(extractToken(token), orderId);
            return ResponseEntity.status(HttpStatus.OK).body("Order refunded successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error refunding order: " + e.getMessage());
        }
    }

    @PostMapping("/rejectRefund/{orderId}")
    public ResponseEntity<String> rejectRefund(@RequestHeader("Authorization") String token, @PathVariable Long orderId) {
        try {
            orderService.rejectRefund(extractToken(token), orderId);
            return ResponseEntity.status(HttpStatus.OK).body("Order refund rejected successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error rejecting request order: " + e.getMessage());
        }
    }

    @GetMapping("/refundRequests")
    public List<RefundRequest> refundRequests(@RequestHeader("Authorization") String token) {
        return orderService.getRefundRequests(extractToken(token));
    }

    @PostMapping("/requestRefund/{orderId}")
    public ResponseEntity<String> requestRefund(@RequestHeader("Authorization") String token, @PathVariable Long orderId) {
        try {
            orderService.requestRefund(extractToken(token),orderId);
            return ResponseEntity.status(HttpStatus.OK).body("Order refunded successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error refunding order: " + e.getMessage());
        }
    }

    // Ship Order API
    @PostMapping("/ship/{orderId}")
    public ResponseEntity<String> shipOrder(@RequestHeader("Authorization") String token, @PathVariable Long orderId, @RequestBody(required = false) Date deliveryDate) {
        try {
            orderService.shipOrder(extractToken(token),orderId, deliveryDate);
            return ResponseEntity.status(HttpStatus.OK).body("Order shipped successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error shipping order: " + e.getMessage());
        }
    }

    // Deliver Order API
    @PostMapping("/deliver/{orderId}")
    public ResponseEntity<String> deliverOrder(@RequestHeader("Authorization") String token, @PathVariable Long orderId) {
        try {
            orderService.deliverOrder(extractToken(token), orderId);
            return ResponseEntity.status(HttpStatus.OK).body("Order delivered successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error delivering order: " + e.getMessage());
        }
    }

    @GetMapping("/track/{orderId}")
    public String trackOrder(@RequestHeader("Authorization") String token, @PathVariable Long orderId) {
        return orderService.trackOrder(token, orderId);
    }
}
