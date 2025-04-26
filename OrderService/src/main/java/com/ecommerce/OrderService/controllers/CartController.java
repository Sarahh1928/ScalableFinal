package com.ecommerce.OrderService.controllers;

import com.ecommerce.OrderService.Dto.UserSessionDTO;
import com.ecommerce.OrderService.models.Cart;
import com.ecommerce.OrderService.services.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/carts")
public class CartController {

    @Autowired
    private CartService cartService;

    private static final String USER_SESSION_CACHE_PREFIX = "user_session::";

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);  // Remove "Bearer " prefix
        }
        return null;  // If the header doesn't contain a Bearer token, return null
    }

    @GetMapping("/get")
    public UserSessionDTO getSession(@RequestHeader("Authorization") String token) {
        return cartService.getSession(extractToken(token));
    }

    // Add item to cart (using JWT token in header)
    @PostMapping("/add/{productId}")
    public ResponseEntity<String> addItemToCart(
            @RequestHeader("Authorization") String token,
            @PathVariable Long productId,
            @RequestBody Integer quantity
    ) {
        cartService.addItemToCart(extractToken(token), productId, quantity);
        return ResponseEntity.ok("Item added to cart");
    }


    // View cart (using JWT token in header)
    @GetMapping
    public ResponseEntity<Cart> viewCart(@RequestHeader("Authorization") String token) {
        Cart cart = cartService.viewCart(extractToken(token));
        return ResponseEntity.ok(cart);
    }

    // Remove item from cart (using JWT token in header)
    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<String> removeItemFromCart(
            @RequestHeader("Authorization") String token,
            @PathVariable Long productId,
            @RequestBody(required = false) Integer quantity) {

        cartService.removeItemFromCart(extractToken(token), productId, quantity);
        return ResponseEntity.ok("Item removed from cart");
    }



    // Clear cart (using JWT token in header)
    @DeleteMapping("/clear")
    public ResponseEntity<String> clearCart(@RequestHeader("Authorization") String token) {
        cartService.clearCart(extractToken(token));
        return ResponseEntity.ok("Cart cleared");
    }
}
