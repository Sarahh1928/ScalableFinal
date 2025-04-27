package com.ecommerce.OrderService.models;

import com.ecommerce.OrderService.models.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.sql.Date;
import java.util.List;

@Entity
@Table(name = "orders")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    private String userEmail;

    @ElementCollection
    @CollectionTable(name = "order_cart_items", joinColumns = @JoinColumn(name = "order_id"))
    private List<CartItem> orderProducts;

    @Column(nullable = false)
    private Long merchantId;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.CONFIRMED;

    // Add totalPrice and totalItemCount
    @Column(nullable = false)
    private double totalPrice;

    @Column(nullable = false)
    private int totalItemCount;

    // New deliveryDate field (date-only)
    @Column(nullable = true)
    private Date deliveryDate;  // Using java.sql.Date for date-only storage

    @OneToOne(mappedBy = "order")  // Reference back to the RefundRequest (mappedBy means the relationship is owned by RefundRequest)
    private RefundRequest refundRequest;  // This will hold the reference to the RefundRequest entity

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<CartItem> getOrderProducts() {
        return orderProducts;
    }

    public void setOrderProducts(List<CartItem> orderProducts) {
        this.orderProducts = orderProducts;
        // Recalculate total price and item count when the products are set
        calculateTotal();
    }

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    // Getters and Setters for totalPrice and totalItemCount
    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public int getTotalItemCount() {
        return totalItemCount;
    }

    public void setTotalItemCount(int totalItemCount) {
        this.totalItemCount = totalItemCount;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    // Getter and Setter for deliveryDate (date-only)
    public Date getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    // Getter and Setter for refundRequest
    public RefundRequest getRefundRequest() {
        return refundRequest;
    }

    public void setRefundRequest(RefundRequest refundRequest) {
        this.refundRequest = refundRequest;
    }

    // Helper method to calculate the total price and item count based on order products
    private void calculateTotal() {
        double price = 0.0;
        int itemCount = 0;

        for (CartItem item : orderProducts) {
            price += item.getTotalPrice();  // Add the total price of each cart item
            itemCount += item.getQuantity();  // Add the quantity of each cart item
        }

        this.totalPrice = price;
        this.totalItemCount = itemCount;
    }
}
