package com.ecommerce.OrderService.services;

import com.ecommerce.OrderService.models.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class CartService {

    private final RedisTemplate<String, Cart> redisTemplate;

    @Autowired
    public CartService(RedisTemplate<String, Cart> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Add item to cart (using JWT token as key)
    public void addItemToCart(String token, Long userId, Long productId, int quantity) {
        String cartKey = "cart:" + token; // Use token as the key
        Cart cart = (Cart) redisTemplate.opsForValue().get(cartKey);

        if (cart == null) {
            cart = new Cart(token, userId); // If no cart exists, create a new one
        }

        cart.addItem(productId, quantity);
        redisTemplate.opsForValue().set(cartKey, cart); // Store in Redis
    }

    // View cart (using JWT token as key)
    public Cart viewCart(String token) {
        String cartKey = "cart:" + token;
        Cart cart = (Cart) redisTemplate.opsForValue().get(cartKey);
        return cart != null ? cart : new Cart(token, null); // Return empty cart if not found
    }

    // Remove item from cart (using JWT token as key)
    public void removeItemFromCart(String token, Long productId) {
        String cartKey = "cart:" + token;
        Cart cart = (Cart) redisTemplate.opsForValue().get(cartKey);

        if (cart != null) {
            cart.removeItem(productId);
            redisTemplate.opsForValue().set(cartKey, cart); // Update Redis
        }
    }

    // Clear cart (using JWT token as key)
    public void clearCart(String token) {
        String cartKey = "cart:" + token;
        redisTemplate.delete(cartKey);
    }
}

