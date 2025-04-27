package com.ecommerce.OrderService.Clients;

import com.ecommerce.OrderService.Dto.ProductResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "user-service", url = "http://user-service:8080")
public interface UserServiceFeignClient {
    @PostMapping("/deposit/{userId}")
    void deposit(@RequestHeader("Authorization") String token,
                 @PathVariable("userId") Long userId,
                 @RequestBody Double amount);
}