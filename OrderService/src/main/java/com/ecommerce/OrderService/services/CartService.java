package com.ecommerce.OrderService.services;

import com.ecommerce.OrderService.Clients.ProductServiceFeignClient;
import com.ecommerce.OrderService.Dto.UserSessionDTO;
import com.ecommerce.OrderService.models.Cart;
import com.ecommerce.OrderService.models.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class CartService {

    private final RedisTemplate<String, Cart> cartRedisTemplate;
    private final RedisTemplate<String, UserSessionDTO> sessionRedisTemplate;
    private final ProductServiceFeignClient productServiceFeignClient;

    @Autowired
    public CartService(
            @Qualifier("cartRedisTemplate") RedisTemplate<String, Cart> cartRedisTemplate,
            @Qualifier("userSessionDTORedisTemplate") RedisTemplate<String, UserSessionDTO> sessionRedisTemplate,
            ProductServiceFeignClient productServiceFeignClient
    ) {
        this.cartRedisTemplate = cartRedisTemplate;
        this.sessionRedisTemplate = sessionRedisTemplate;
        this.productServiceFeignClient = productServiceFeignClient;
    }


    public UserSessionDTO getSession(String token) {
        // Fetch the session for the token
        UserSessionDTO session = sessionRedisTemplate.opsForValue().get(token);
        if (session == null) {
            throw new RuntimeException("Session not found in cache for token: " + token);
        }
        else if(!session.getRole().equalsIgnoreCase("CUSTOMER")){
            throw new RuntimeException("You are not a customer");
        }
        return session;
    }

    public void addItemToCart(String token, Long productId, int quantity) {
        System.out.println("Adding item to cart...");
        System.out.println("Token: " + token + ", Product ID: " + productId + ", Quantity: " + quantity);

        // Check product availability
        var product = productServiceFeignClient.getProductById(productId);
        if (product == null) {
            System.out.println("Product not found for Product ID: " + productId);
            throw new RuntimeException("Product not found");
        } else if (product.getStock() < quantity) {
            System.out.println("Not Enough Stock for Product ID: " + productId + ". Available stock: " + product.getStock());
            throw new RuntimeException("Not Enough Stock");
        }

        UserSessionDTO session = getSession(token);
        Long userId = session.getUserId();
        System.out.println("User Session: " + session);

        Cart cart = cartRedisTemplate.opsForValue().get(token);
        if (cart == null) {
            System.out.println("Cart not found for Token: " + token + ". Creating a new cart.");
            cart = new Cart(token, userId);
        } else {
            System.out.println("Found existing cart for Token: " + token);
        }

        // Add the item to the cart
        cart.addItem(productId, quantity, product.getPrice(), product.getMerchantId());
        System.out.println("Added item to cart. Product ID: " + productId + ", Quantity: " + quantity + ", Total Price: " + (product.getPrice() * quantity));

        // Save the updated cart back to Redis
        cartRedisTemplate.opsForValue().set(token, cart);
        System.out.println("Cart updated in Redis for Token: " + token);
    }

    public Cart viewCart(String token) {
        // Fetch the cart or create a new one if not present
        UserSessionDTO session = getSession(token);
        Cart cart = cartRedisTemplate.opsForValue().get(token);
        return cart != null ? cart : new Cart(token, session.getUserId());
    }

    public void removeItemFromCart(String token, Long productId, Integer quantity) {
        UserSessionDTO session = getSession(token);
        Cart cart = cartRedisTemplate.opsForValue().get(token);

        if (cart != null) {
            if (quantity == null) {
                // Remove entire item if no quantity specified
                cart.removeItem(productId);
            } else {
                // Reduce quantity
                CartItem item = cart.getItems().get(productId);
                if (item != null) {
                    int newQuantity = item.getQuantity() - quantity;
                    if (newQuantity <= 0) {
                        // If quantity after removal <= 0, remove the whole item
                        cart.removeItem(productId);
                    } else {
                        // Else, update quantity and adjust totals
                        item.setQuantity(newQuantity);
                        // Update the cart total manually
                        cart.recalculateTotals();
                    }
                }
            }
            // Save updated cart back to Redis
            cartRedisTemplate.opsForValue().set(token, cart);
        } else {
            throw new RuntimeException("Cart not found for token: " + token);
        }
    }


    public void clearCart(String token) {
        // Delete the cart from Redis
        UserSessionDTO session = getSession(token);
        cartRedisTemplate.delete(token);
    }
}
