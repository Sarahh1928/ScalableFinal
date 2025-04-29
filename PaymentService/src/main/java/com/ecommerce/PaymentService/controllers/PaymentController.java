package com.ecommerce.PaymentService.controllers;

import com.ecommerce.PaymentService.models.Payment;
import com.ecommerce.PaymentService.models.PaymentRequest;
import com.ecommerce.PaymentService.models.enums.PaymentMethod;
import com.ecommerce.PaymentService.models.enums.PaymentStatus;
import com.ecommerce.PaymentService.services.PaymentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
public class PaymentController {
    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Create payment
    @PostMapping
    public ResponseEntity<Payment> createPayment(
            @RequestParam Long orderId,
            @RequestParam Long userId,
            @RequestParam String customerEmail,
            @RequestParam PaymentMethod method,
            @RequestParam double amount,
            @RequestBody PaymentRequest paymentRequest,
            @RequestHeader("Authorization") String authorizationHeader) {  // Expect PaymentRequest here

        // Get the payment details array
        String[] paymentDetails = paymentRequest.getPaymentDetails();

        Payment payment = paymentService.processPayment(orderId, userId, customerEmail, method, amount, paymentDetails);
        return ResponseEntity.ok(payment);
    }

    // Get payment by ID
    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPayment(@PathVariable Long id) {
        Payment payment = paymentService.getPaymentById(id);
        return payment != null ? ResponseEntity.ok(payment) : ResponseEntity.notFound().build();
    }

    // Get user payments
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Payment>> getUserPayments(@PathVariable Long userId) {
        List<Payment> payments = paymentService.getUserPayments(userId);
        return ResponseEntity.ok(payments);
    }

    // Get payment history (admin)
    @GetMapping("/history")
    public ResponseEntity<Page<Payment>> getPaymentHistory(Pageable pageable) {
        return ResponseEntity.ok(paymentService.getPaymentHistory(pageable));
    }

    // Get payments by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Payment>> getPaymentsByStatus(@PathVariable PaymentStatus status) {
        return ResponseEntity.ok(paymentService.getPaymentsByStatus(status));
    }

    // Update payment status
    @PutMapping("/{id}/status")
    public ResponseEntity<Payment> updatePaymentStatus(
            @PathVariable Long id,
            @RequestParam PaymentStatus status) {

        return ResponseEntity.ok(paymentService.updatePaymentStatus(id, status));
    }

    // Delete payment
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }

    // Refund payment
    @PostMapping("/{id}/refund")
    public ResponseEntity<Payment> refundPayment(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.refundPayment(id));
    }

    // Cancel payment
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Payment> cancelPayment(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.cancelPayment(id));
    }
}

