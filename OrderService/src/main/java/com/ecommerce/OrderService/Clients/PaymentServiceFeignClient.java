package com.ecommerce.OrderService.Clients;

import com.ecommerce.OrderService.Dto.PaymentMethodDTO;
import com.ecommerce.OrderService.Dto.PaymentRequestDTO;
import com.ecommerce.OrderService.Dto.PaymentResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "payment-service", url = "http://payment-service:8080") // or use service discovery
public interface PaymentServiceFeignClient {
    @PostMapping("/payments")
    PaymentResponseDTO createPayment(
            @RequestParam("userId") Long userId,
            @RequestParam("customerEmail") String customerEmail,
            @RequestParam("method") PaymentMethodDTO method,
            @RequestParam("amount") double amount,
            @RequestBody(required = false) PaymentRequestDTO paymentRequest,
            @RequestParam("token") String token
    );

    @PostMapping("/{id}/refund")
    void refundPayment(@PathVariable Long id);
}


