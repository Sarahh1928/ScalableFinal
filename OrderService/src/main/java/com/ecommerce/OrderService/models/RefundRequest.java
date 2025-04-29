package com.ecommerce.OrderService.models;

import com.ecommerce.OrderService.models.enums.RefundRequestStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "refundRequests")
public class RefundRequest {

    private Long userId;
    private Long merchantId;

    @OneToOne
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    @JsonIgnoreProperties("refundRequest")  // if you're serializing Order from here
    private Order order;

    private RefundRequestStatus status;  // Refund request status

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Constructors
    public RefundRequest() {
        this.status = RefundRequestStatus.PENDING; // Default status is PENDING
    }

    public RefundRequest(Long userId, Long merchantId, Order order, RefundRequestStatus status) {
        this.userId = userId;
        this.merchantId = merchantId;
        this.order = order;
        this.status = status != null ? status : RefundRequestStatus.PENDING; // Default to PENDING if status is null
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public RefundRequestStatus getStatus() {
        return status;
    }

    public void setStatus(RefundRequestStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "RefundRequest{" +
                "userId=" + userId +
                ", merchantId=" + merchantId +
                ", order=" + order +
                ", status=" + status +
                '}';
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
