package com.ecommerce.OrderService.services;

import com.ecommerce.OrderService.Clients.ProductServiceFeignClient;
import com.ecommerce.OrderService.Dto.UserSessionDTO;
import com.ecommerce.OrderService.models.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class CartService {

    private final RedisTemplate<String, Cart> cartRedisTemplate;
    private final RedisTemplate<String, UserSessionDTO> sessionRedisTemplate;
    private final ProductServiceFeignClient productServiceFeignClient;

    @Autowired
    public CartService(RedisTemplate<String, Cart> cartRedisTemplate,
                       RedisTemplate<String, UserSessionDTO> sessionRedisTemplate, ProductServiceFeignClient productServiceFeignClient) {
        this.cartRedisTemplate = cartRedisTemplate;
        this.sessionRedisTemplate = sessionRedisTemplate;
        this.productServiceFeignClient = productServiceFeignClient;
    }

    public UserSessionDTO getSession(String token) {
        UserSessionDTO session = sessionRedisTemplate.opsForValue().get(token);
        if (session == null) {
            throw new RuntimeException("Session not found in cache for token: " + token);
        }
        return session;
    }

    // Add item to cart
    public void addItemToCart(String token, Long productId, int quantity) {
        if(productServiceFeignClient.getProductById(productId) == null) {
            throw new RuntimeException("Product not found");
        }
        else if(productServiceFeignClient.getProductById(productId).getStock() < quantity) {
            throw new RuntimeException("Not Enough Stock");
        }
        Cart cart = cartRedisTemplate.opsForValue().get(token);
        Long userId = Objects.requireNonNull(sessionRedisTemplate.opsForValue().get(token)).getUserId();
        if (cart == null) {
            cart = new Cart(token, userId); // Create new cart if missing
        }
        cart.addItem(productId, quantity,productServiceFeignClient.getProductById(productId).getPrice());
        cartRedisTemplate.opsForValue().set(token, cart);
    }

    // View cart
    public Cart viewCart(String token) {
        Cart cart = cartRedisTemplate.opsForValue().get(token);
        Long userId = Objects.requireNonNull(sessionRedisTemplate.opsForValue().get(token)).getUserId();
        return cart != null ? cart : new Cart(token, userId); // Return empty cart if not found
    }

    // Remove item from cart
    public void removeItemFromCart(String token, Long productId) {
        Cart cart = cartRedisTemplate.opsForValue().get(token);

        if (cart != null) {
            cart.removeItem(productId);
            cartRedisTemplate.opsForValue().set(token, cart);
        }
    }

    // Clear cart
    public void clearCart(String token) {
        cartRedisTemplate.delete(token);
    }
}
