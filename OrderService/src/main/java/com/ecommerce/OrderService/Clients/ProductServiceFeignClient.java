package com.ecommerce.OrderService.Clients;

import com.ecommerce.OrderService.DTO.ProductResponseDTO;
import com.ecommerce.OrderService.models.UserSession;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", url = "http://localhost:8080/products")  // Replace with the actual URL of your User Service
public interface ProductServiceFeignClient {
    @GetMapping("/{id}")
    ProductResponseDTO getProductById(@PathVariable Long id);
}