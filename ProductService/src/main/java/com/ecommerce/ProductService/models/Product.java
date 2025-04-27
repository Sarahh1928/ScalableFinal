package com.ecommerce.ProductService.models;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Generates the UID automatically
    private long uid; // The unique identifier for each product

    protected String name;
    protected double price;
    protected String brand;
    protected String color;
    protected Long merchantId;
    protected int stockLevel;

    public Product() {
        // Empty constructor for reflection
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    // common setters
    public void setCommonAttributes(String name, double price, String brand, String color, Long merchantId, int stockLevel) {
        this.name = name;
        this.price = price;
        this.brand = brand;
        this.color = color;
        this.merchantId = merchantId;
        this.stockLevel = stockLevel;
    }

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }


    public int getStockLevel() {
        return stockLevel;
    }

    public void setStockLevel(int stockLevel) {
        this.stockLevel = stockLevel;
    }
}