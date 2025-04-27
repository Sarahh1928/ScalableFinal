package com.ecommerce.PaymentService.services;
import com.ecommerce.PaymentService.models.Payment;
import com.ecommerce.PaymentService.models.enums.PaymentMethod;
import com.ecommerce.PaymentService.models.enums.PaymentStatus;
import com.ecommerce.PaymentService.repositories.PaymentRepository;
import com.ecommerce.PaymentService.services.Factory.PaymentStrategyFactory;
import com.ecommerce.PaymentService.services.strategy.PaymentStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    // Create: Initiate payment
    @Transactional
    public Payment processPayment(Long orderId, Long userId, String customerEmail,
                                  PaymentMethod method, double amount, Object... paymentDetails) {
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setUserId(userId);
        payment.setCustomerEmail(customerEmail);
        payment.setMethod(method);
        payment.setAmount(amount);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setTransactionDate(LocalDateTime.now());

        // Save initially as PENDING
        payment = paymentRepository.save(payment);

        try {
            PaymentStrategy strategy = PaymentStrategyFactory.createPaymentStrategy(method, paymentDetails);
            boolean isSuccessful = strategy.processPayment(amount);

            payment.setStatus(isSuccessful ? PaymentStatus.SUCCESSFUL : PaymentStatus.FAILED);
            payment.setTransactionId(UUID.randomUUID().toString());


            return paymentRepository.save(payment);
        } catch (Exception e) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new RuntimeException("Payment processing failed", e);
        }
    }

    // Read: Get payment by ID
    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id).orElse(null);
    }

    // Read: Get all payments for a user
    public List<Payment> getUserPayments(Long userId) {
        return paymentRepository.findByUserId(userId);
    }

    // Read: Get payment history (admin)
    public Page<Payment> getPaymentHistory(Pageable pageable) {
        return paymentRepository.findAll(pageable);
    }

    // Read: Get payments by status
    public List<Payment> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status);
    }

    // Update: Update payment status
    @Transactional
    public Payment updatePaymentStatus(Long paymentId, PaymentStatus newStatus) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setStatus(newStatus);
        return paymentRepository.save(payment);
    }

    // Delete: Remove payment from history (soft delete)
    @Transactional
    public void deletePayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        paymentRepository.delete(payment);
    }

    // Additional Function: Refund payment
    @Transactional
    public Payment refundPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.SUCCESSFUL) {
            throw new RuntimeException("Only successful payments can be refunded");
        }

        // In a real implementation, call the payment provider's refund API
        payment.setStatus(PaymentStatus.REFUNDED);
        payment = paymentRepository.save(payment);

        return payment;
    }

    // Additional Function: Cancel payment
    @Transactional
    public Payment cancelPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new RuntimeException("Only pending payments can be cancelled");
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        payment = paymentRepository.save(payment);

        return payment;
    }
}