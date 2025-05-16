package com.ecommerce.OrderService.controllers;

import com.ecommerce.OrderService.Dto.UserSessionDTO;
import com.ecommerce.OrderService.models.Cart;
import com.ecommerce.OrderService.services.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/carts")
public class CartController {

    @Autowired
    private CartService cartService;

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);  // Remove "Bearer " prefix
        }
        return null;  // If the header doesn't contain a Bearer token, return null
    }

    @GetMapping("/get")
    public ResponseEntity<?> getSession(@RequestHeader("Authorization") String token) {
        try {
            UserSessionDTO session = cartService.getSession(extractToken(token));
            return ResponseEntity.ok(session);
        } catch (Exception e) {
            return new ResponseEntity<>("Error retrieving session: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Add item to cart (using JWT token in header)
    @PostMapping("/add/{productId}")
    public ResponseEntity<?> addItemToCart(
            @RequestHeader("Authorization") String token,
            @PathVariable Long productId,
            @RequestBody Integer quantity) {
        try {
            cartService.addItemToCart(extractToken(token), productId, quantity);
            return ResponseEntity.ok("Item added to cart");
        } catch (Exception e) {
            return new ResponseEntity<>("Error adding item to cart: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // View cart (using JWT token in header)
    @GetMapping
    public ResponseEntity<?> viewCart(@RequestHeader("Authorization") String token) {
        try {
            Cart cart = cartService.viewCart(extractToken(token));
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            return new ResponseEntity<>("Error viewing cart: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Remove item from cart (using JWT token in header)
    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<?> removeItemFromCart(
            @RequestHeader("Authorization") String token,
            @PathVariable Long productId,
            @RequestBody(required = false) Integer quantity) {
        try {
            cartService.removeItemFromCart(extractToken(token), productId, quantity);
            return ResponseEntity.ok("Item removed from cart");
        } catch (Exception e) {
            return new ResponseEntity<>("Error removing item from cart: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Clear cart (using JWT token in header)
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(@RequestHeader("Authorization") String token) {
        try {
            cartService.clearCart(extractToken(token));
            return ResponseEntity.ok("Cart cleared");
        } catch (Exception e) {
            return new ResponseEntity<>("Error clearing cart: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
