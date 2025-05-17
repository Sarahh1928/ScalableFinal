package com.ecommerce.PaymentService.controllers;

import com.ecommerce.PaymentService.dto.UserSessionDTO;
import com.ecommerce.PaymentService.models.Payment;
import com.ecommerce.PaymentService.models.PaymentRequest;
import com.ecommerce.PaymentService.models.enums.PaymentMethod;
import com.ecommerce.PaymentService.models.enums.PaymentStatus;
import com.ecommerce.PaymentService.services.PaymentSeederService;
import com.ecommerce.PaymentService.services.PaymentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/payments")
public class PaymentController {
    private final PaymentService paymentService;

    @Autowired
    private PaymentSeederService paymentSeederService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);  // Remove "Bearer " prefix
        }
        return null;  // If the header doesn't contain a Bearer token, return null
    }
    @GetMapping("/seed")
    public ResponseEntity<String> seedPayment() {
        try {
            String result = paymentSeederService.seedPayments();
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Error seeding payments: " + e.getMessage());
        }
    }



    @GetMapping("/getToken")
    public ResponseEntity<?> logoutUser(@RequestHeader("Authorization") String token) {
        try {
            String actualToken = extractToken(token);
            String response = paymentService.getToken(actualToken);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return new ResponseEntity<>("Error getting token: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Create payment
    @PostMapping
    public ResponseEntity<?> createPayment(
            @RequestParam Long userId,
            @RequestParam String customerEmail,
            @RequestParam PaymentMethod method,
            @RequestParam double amount,
            @RequestBody(required = false) PaymentRequest paymentRequest, // Make it optional
            @RequestParam String token) {

        try {
            // Get user session details from the token
            UserSessionDTO userSession = paymentService.getUserSessionFromToken(token);
            if (userSession == null || !"CUSTOMER".equals(userSession.getRole())) {
                return new ResponseEntity<>("Unauthorized: Invalid user session", HttpStatus.BAD_REQUEST);
            }

            // Get the payment details array
            String[] paymentDetails = paymentRequest != null ? paymentRequest.getPaymentDetails() : new String[0];

            Payment payment = paymentService.processPayment(token, userId, customerEmail, method, amount, paymentDetails);
            return ResponseEntity.status(HttpStatus.CREATED).body(payment);
        } catch (Exception e) {
            return new ResponseEntity<>("Error creating payment: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Get payment by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getPayment(@PathVariable Long id,
                                        @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = extractToken(authorizationHeader);
            UserSessionDTO userSession = paymentService.getUserSessionFromToken(token);
            if (userSession == null || "MERCHANT".equals(userSession.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized: Only customers can add products.");
            }
            Payment payment = paymentService.getPaymentById(id);
            return payment != null ? ResponseEntity.ok(payment) : new ResponseEntity<>("Payment not found", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Error retrieving payment: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Get user payments
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserPayments(@PathVariable Long userId) {
        try {
            List<Payment> payments = paymentService.getUserPayments(userId);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return new ResponseEntity<>("Error retrieving user payments: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Get payment history (admin)
    @GetMapping("/history")
    public ResponseEntity<?> getPaymentHistory(Pageable pageable,
                                               @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = extractToken(authorizationHeader);
            UserSessionDTO userSession = paymentService.getUserSessionFromToken(token);
            if (userSession == null || "MERCHANT".equals(userSession.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized: Only customers can add products.");
            }
            Page<Payment> paymentHistory = paymentService.getPaymentHistory(userSession.getRole(),userSession.getUserId(),pageable);
            return ResponseEntity.ok(paymentHistory);
        } catch (Exception e) {
            return new ResponseEntity<>("Error retrieving payment history: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Get payments by status
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getPaymentsByStatus(@PathVariable PaymentStatus status,
                                                 @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = extractToken(authorizationHeader);
            UserSessionDTO userSession = paymentService.getUserSessionFromToken(token);
            if (userSession == null || "MERCHANT".equals(userSession.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized: Only customers can add products.");
            }
            List<Payment> payments = paymentService.getPaymentsByStatus(userSession.getRole(),userSession.getUserId(),status);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return new ResponseEntity<>("Error retrieving payments by status: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Update payment status
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updatePaymentStatus(
            @PathVariable Long id,
            @RequestParam PaymentStatus status,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = extractToken(authorizationHeader);
            UserSessionDTO userSession = paymentService.getUserSessionFromToken(token);
            if (userSession == null || !"ADMIN".equals(userSession.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized: Only customers can add products.");
            }
            Payment payment = paymentService.updatePaymentStatus(id, status);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return new ResponseEntity<>("Error updating payment status: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Delete payment
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePayment(@PathVariable Long id,
                                           @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = extractToken(authorizationHeader);
            UserSessionDTO userSession = paymentService.getUserSessionFromToken(token);
            if (userSession == null || "MERCHANT".equals(userSession.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized: Only customers can add products.");
            }
            paymentService.deletePayment(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return new ResponseEntity<>("Error deleting payment: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<?> refundPayment(@PathVariable Long id,
                                           @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = extractToken(authorizationHeader);
            UserSessionDTO userSession = paymentService.getUserSessionFromToken(token);
            if (userSession == null || "CUSTOMER".equals(userSession.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized: Only customers can add products.");
            }
            paymentService.refundPayment(id);
            return ResponseEntity.ok("Refund processed successfully.");
        } catch (Exception e) {
            return new ResponseEntity<>("Error refunding payment: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelPayment(@PathVariable Long id,
                                           @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = extractToken(authorizationHeader);
            UserSessionDTO userSession = paymentService.getUserSessionFromToken(token);
            if (userSession == null || "MERCHANT".equals(userSession.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized: Only customers can add products.");
            }
            Payment canceledPayment = paymentService.cancelPayment(id);
            return ResponseEntity.ok(canceledPayment);
        } catch (Exception e) {
            return new ResponseEntity<>("Error canceling payment: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
