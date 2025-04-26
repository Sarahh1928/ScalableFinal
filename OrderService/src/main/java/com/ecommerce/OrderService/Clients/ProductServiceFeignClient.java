package com.ecommerce.OrderService.Clients;

import com.ecommerce.OrderService.Dto.ProductResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", url = "http://localhost:8080/products")  // Replace with the actual URL of your User Service
public interface ProductServiceFeignClient {
    @GetMapping("/{id}")
    ProductResponseDTO getProductById(@PathVariable Long id);
}