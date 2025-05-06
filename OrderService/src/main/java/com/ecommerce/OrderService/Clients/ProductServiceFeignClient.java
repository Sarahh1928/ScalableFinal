package com.ecommerce.OrderService.Clients;

import com.ecommerce.OrderService.Dto.ProductResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "product-service", url = "http://product-service:8080")
public interface ProductServiceFeignClient {

    @GetMapping("/products/{id}")
    ProductResponseDTO getProductById(@PathVariable("id") Long id);

    @PutMapping("/products/{id}/addstock")
    void addStock(
            @PathVariable("id") Long id,
            @RequestParam("stock") int stock,
            @RequestHeader("Authorization") String authorizationHeader
    );

    @PutMapping("/products/{id}/removestock")
    void removeStock(
            @PathVariable("id") Long id,
            @RequestParam("stock") int stock,
            @RequestHeader("Authorization") String authorizationHeader
    );
}
