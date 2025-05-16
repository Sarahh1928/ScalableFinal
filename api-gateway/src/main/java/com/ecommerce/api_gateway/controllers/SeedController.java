package com.ecommerce.api_gateway.controllers;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/seed")
public class SeedController {

    private final WebClient webClient;

    public SeedController(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    @GetMapping
    public Mono<String> seedAll() {
        Mono<String> users = webClient.get()
                .uri("http://user-service:8080/users/seed")
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> Mono.just("❌ User service failed: " + extractErrorMessage(e)));

        Mono<String> products = webClient.get()
                .uri("http://product-service:8080/products/seed")
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> Mono.just("❌ Product service failed: " + extractErrorMessage(e)));

        Mono<String> payments = webClient.get()
                .uri("http://payment-service:8080/payments/seed")
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> Mono.just("❌ Payment service failed: " + extractErrorMessage(e)));

        Mono<String> orders = webClient.get()
                .uri("http://order-service:8080/orders/seed")
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> Mono.just("❌ Order service failed: " + extractErrorMessage(e)));



        return Mono.zip(users, orders, payments, products)
                .map(results -> "💾 Seeding completed:\n" +
                        "- Users: " + results.getT1() + "\n" +
                        "- Orders: " + results.getT2() + "\n" +
                        "- Payments: " + results.getT3() + "\n" +
                        "- Products: " + results.getT4());
    }

    private String extractErrorMessage(Throwable e) {
        if (e instanceof WebClientResponseException we) {
            return we.getStatusCode() + " - " + we.getResponseBodyAsString();
        }
        return e.getMessage();
    }
}
