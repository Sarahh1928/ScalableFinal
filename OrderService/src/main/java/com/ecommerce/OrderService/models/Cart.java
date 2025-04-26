package com.ecommerce.OrderService.models;

import java.util.HashMap;
import java.util.Map;

public class Cart {

    private String token;   // JWT Token for the session
    private Long userId;    // User ID associated with the cart
    private Map<Long, CartItem> items;  // A map where the key is the product ID and the value is the CartItem

    // Constructor to initialize the cart with token and userId
    public Cart(String token, Long userId) {
        this.token = token;
        this.userId = userId;
        this.items = new HashMap<>();
    }

    // Add item to cart
    public void addItem(Long productId, int quantity) {
        CartItem cartItem = items.get(productId);
        if (cartItem != null) {
            cartItem.setQuantity(cartItem.getQuantity() + quantity); // If the item exists, just update the quantity
        } else {
            items.put(productId, new CartItem(productId, quantity)); // Otherwise, create a new CartItem
        }
    }

    // Remove item from cart
    public void removeItem(Long productId) {
        items.remove(productId);
    }

    // Get list of all items in the cart
    public Map<Long, CartItem> getItems() {
        return items;
    }

    // Clear all items in the cart
    public void clear() {
        items.clear();
    }

    // Get the total number of items in the cart
    public int getTotalItemCount() {
        return items.values().stream().mapToInt(CartItem::getQuantity).sum();
    }

    // Get the total price of items in the cart (you would need to integrate with product data)
    public double getTotalPrice() {
        double totalPrice = 0.0;
        for (CartItem item : items.values()) {
            // Example: totalPrice += item.getQuantity() * productPrice(item.getProductId());
        }
        return totalPrice;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setItems(Map<Long, CartItem> items) {
        this.items = items;
    }

    // Optional: Override toString(), equals(), hashCode() methods if needed
}
