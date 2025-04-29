package com.ecommerce.OrderService.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Cart {

    private Long userId;
    private String email;
    private Map<Long, CartItem> items;
    private double totalPrice;
    private int totalItemCount;


    public Cart() {}

    public Cart(Long userId, String email) {
        this.userId = userId;
        this.email = email;
        this.items = new HashMap<>();
        this.totalPrice = 0.0;
        this.totalItemCount = 0;
    }

    // Add item to cart
    public void addItem(Long productId, int quantity, double price, Long merchantId) {
        CartItem cartItem = items.get(productId);
        if (cartItem != null) {
            // Update existing item
            int oldQuantity = cartItem.getQuantity();
            cartItem.setQuantity(oldQuantity + quantity);

            // Update totals
            totalItemCount += quantity;
            totalPrice += quantity * price;
        } else {
            // Add new item
            CartItem newItem = new CartItem(productId, quantity, price, merchantId);
            items.put(productId, newItem);

            // Update totals
            totalItemCount += quantity;
            totalPrice += quantity * price;
        }
    }

    // Remove item from cart
    public void removeItem(Long productId) {
        CartItem cartItem = items.remove(productId);
        if (cartItem != null) {
            int quantity = cartItem.getQuantity();
            double price = cartItem.getPrice();

            // Update totals
            totalItemCount -= quantity;
            totalPrice -= quantity * price;
        }
    }

    // Clear all items in the cart
    public void clear() {
        items.clear();
        totalItemCount = 0;
        totalPrice = 0.0;
    }

    // Getters and Setters
    public Map<Long, CartItem> getItems() {
        return items;
    }

    public void setItems(Map<Long, CartItem> items) {
        this.items = items;
        recalculateTotals(); // recalc when you manually set items
    }

    public int getTotalItemCount() {
        return totalItemCount;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return email;
    }

    public void setUserEmail(String userEmail) {
        this.email = userEmail;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setTotalItemCount(int totalItemCount) {
        this.totalItemCount = totalItemCount;
    }

    // private helper to recalculate from scratch (in case someone manually sets items map)
    public void recalculateTotals() {
        totalItemCount = 0;
        totalPrice = 0.0;
        for (CartItem item : items.values()) {
            totalItemCount += item.getQuantity();
            totalPrice += item.getQuantity() * item.getPrice();
        }
    }
}
