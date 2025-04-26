package com.ecommerce.OrderService.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CartItem {

    private Long productId;
    private int quantity;
    private double price;
    private Long merchantId;

    public CartItem() {
    }

    // Constructor
    public CartItem(Long productId, int quantity, double price, Long merchantId) {
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
        this.merchantId = merchantId;
    }

    // Getters and Setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    // You could also add a helper to get total price for this item
    public double getTotalPrice() {
        return quantity * price;
    }
}

