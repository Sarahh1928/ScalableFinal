package com.ecommerce.PaymentService.services;

import com.ecommerce.PaymentService.models.Payment;
import com.ecommerce.PaymentService.models.enums.PaymentMethod;
import com.ecommerce.PaymentService.models.enums.PaymentStatus;
import com.ecommerce.PaymentService.repositories.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentSeederService {

    @Autowired
    private PaymentRepository paymentRepository;

    public String seedPayments() {
        if (paymentRepository.count() > 0) {
            return "⚠️ Payments already seeded.";
        }

        List<Payment> payments = new ArrayList<>();
        double orderTotals = 650.0;

        payments.add(createPayment(1L, "TXN-2", PaymentStatus.FAILED, PaymentMethod.CREDIT_CARD, LocalDateTime.now().minusDays(1), orderTotals));
        payments.add(createPayment(1L, "TXN-1", PaymentStatus.REFUNDED, PaymentMethod.CREDIT_CARD, LocalDateTime.now().minusDays(2), orderTotals));

        payments.add(createPayment(2L, "TXN-3", PaymentStatus.REFUNDED, PaymentMethod.WALLET, LocalDateTime.now().minusDays(2), orderTotals));

        payments.add(createPayment(3L, "TXN-5", PaymentStatus.FAILED, PaymentMethod.CREDIT_CARD, LocalDateTime.now().minusDays(3), orderTotals));
        payments.add(createPayment(3L, "TXN-4", PaymentStatus.SUCCESSFUL, PaymentMethod.CREDIT_CARD, LocalDateTime.now().minusDays(3), orderTotals));

        payments.add(createPayment(4L, "TXN-6", PaymentStatus.SUCCESSFUL, PaymentMethod.WALLET, LocalDateTime.now().minusDays(4), orderTotals));
        payments.add(createPayment(5L, "TXN-7", PaymentStatus.SUCCESSFUL, PaymentMethod.CREDIT_CARD, LocalDateTime.now().minusDays(5), orderTotals));
        payments.add(createPayment(6L, "TXN-8", PaymentStatus.SUCCESSFUL, PaymentMethod.CREDIT_CARD, LocalDateTime.now().minusDays(6), orderTotals));

        paymentRepository.saveAll(payments);

        return "✅ Payments seeded with correct amounts matching orders.";
    }

    private Payment createPayment(Long orderId, String txnId, PaymentStatus status, PaymentMethod method, LocalDateTime date, double amount) {
        Payment payment = new Payment();
        Long userId = ((orderId - 1) % 3) + 1;
        payment.setUserId(userId);
        payment.setCustomerEmail("scalifyteam@gmail.com");
        payment.setMethod(method);
        payment.setAmount(amount);
        payment.setStatus(status);
        payment.setTransactionDate(date);
        payment.setTransactionId(txnId);
        return payment;
    }
}
