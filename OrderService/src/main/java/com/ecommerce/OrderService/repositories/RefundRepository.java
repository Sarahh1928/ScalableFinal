package com.ecommerce.OrderService.repositories;

import com.ecommerce.OrderService.models.RefundRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundRepository  extends JpaRepository<RefundRequest, Long> {

}