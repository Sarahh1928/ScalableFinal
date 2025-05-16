package com.ecommerce.OrderService.repositories;

import com.ecommerce.OrderService.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByMerchantId(Long merchantId);
    List<Order> findByUserId(Long userId);
}
