package com.ecommerce.OrderService.Clients;

import com.ecommerce.OrderService.Dto.ProductResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", url = "http://product-service:8080")
public interface ProductServiceFeignClient {
    @GetMapping("/products/{id}")
    ProductResponseDTO getProductById(@PathVariable("id") Long id);
}
