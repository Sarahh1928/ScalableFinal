package com.ecommerce.PaymentService.clients;

import com.ecommerce.PaymentService.dto.UserDto;
import com.ecommerce.PaymentService.dto.WalletUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "user-service", url = "http://user-service:8080/users")
public interface UserServiceClient {

    @PostMapping("/deduct/{id}")
    void updateWallet(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestBody Double amount
    );

    @PostMapping("/deposit/{userId}")
    void deposit(
            @PathVariable Long userId,
            @RequestBody Double amount
    );


    @GetMapping("/{id}")
    UserDto getUser(@PathVariable("id") Long id, @RequestHeader("Authorization") String token);
}
