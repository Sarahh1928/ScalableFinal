package com.ecommerce.OrderService.Clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service", url = "http://localhost:8080/users")  // Replace with the actual URL of your User Service
public interface UserServiceFeignClient {
    @GetMapping("/session")
    ResponseEntity<UserSession> getSessionFromToken(@RequestHeader("Authorization") String token);
}