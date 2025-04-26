package com.ecommerce.OrderService.models;

public class CartItem {

    private Long productId;
    private int quantity;
    private double price; // 👈 finally the price exists, as it SHOULD

    // Constructor
    public CartItem(Long productId, int quantity, double price) {
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
    }

    // Overloaded Constructor if you want backward compatibility (optional)
    public CartItem(Long productId, int quantity) {
        this(productId, quantity, 0.0);
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

