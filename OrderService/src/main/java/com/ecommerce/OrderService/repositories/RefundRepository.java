package com.ecommerce.OrderService.repositories;

import com.ecommerce.OrderService.models.RefundRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RefundRepository  extends JpaRepository<RefundRequest, Long> {
    List<RefundRequest> findByMerchantId(Long merchantId);
    List<RefundRequest> findByUserId(Long userId);

}