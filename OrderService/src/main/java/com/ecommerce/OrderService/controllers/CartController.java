package com.ecommerce.OrderService.controllers;

import com.ecommerce.OrderService.Clients.UserServiceFeignClient;
import com.ecommerce.OrderService.models.Cart;
import com.ecommerce.OrderService.models.CartItem;
import com.ecommerce.OrderService.models.UserSession;
import com.ecommerce.OrderService.services.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/carts")
public class CartController {

    @Autowired
    private CartService cartService;
    @Autowired
    private UserServiceFeignClient userServiceFeignClient;

    @GetMapping("/get")
    public UserSession getSession(@RequestHeader("Authorization") String token) {
        ResponseEntity<UserSession> response = userServiceFeignClient.getSessionFromToken(token);
        return response.getBody();
    }
    // Add item to cart (using JWT token in header)
    @PostMapping("/add")
    public ResponseEntity<String> addItemToCart(@RequestHeader("Authorization") String token, @RequestBody CartItem cartItem) {
        ResponseEntity<UserSession> response = userServiceFeignClient.getSessionFromToken(token);
        cartService.addItemToCart(token, Objects.requireNonNull(response.getBody()).getUserId(), cartItem.getProductId(), cartItem.getQuantity());
        return ResponseEntity.ok("Item added to cart");
    }

    // View cart (using JWT token in header)
    @GetMapping
    public ResponseEntity<Cart> viewCart(@RequestHeader("Authorization") String token) {
        String jwtToken = token.replace("Bearer ", "");
        Cart cart = cartService.viewCart(jwtToken);
        return ResponseEntity.ok(cart);
    }

    // Remove item from cart (using JWT token in header)
    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<String> removeItemFromCart(@RequestHeader("Authorization") String token, @PathVariable Long productId) {
        String jwtToken = token.replace("Bearer ", "");
        cartService.removeItemFromCart(jwtToken, productId);
        return ResponseEntity.ok("Item removed from cart");
    }

    // Clear cart (using JWT token in header)
    @DeleteMapping("/clear")
    public ResponseEntity<String> clearCart(@RequestHeader("Authorization") String token) {
        String jwtToken = token.replace("Bearer ", "");
        cartService.clearCart(jwtToken);
        return ResponseEntity.ok("Cart cleared");
    }
}
