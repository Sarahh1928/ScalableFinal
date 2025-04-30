package com.ecommerce.OrderService.Dto;

public class ProductResponseDTO {
    private Long id;
    private String name;
    private int stockLevel;
    private double price;
    private Long merchantId;



    // Getters and setters (boilerplate, you can generate)
    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStockLevel() {
        return stockLevel;
    }

    public void setStockLevel(int stockLevel) {
        this.stockLevel = stockLevel;
    }

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    @Override
    public String toString() {
        return "ProductResponseDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", stock=" + stockLevel +
                ", price=" + price +
                ", merchantId=" + merchantId +
                '}';
    }
}
