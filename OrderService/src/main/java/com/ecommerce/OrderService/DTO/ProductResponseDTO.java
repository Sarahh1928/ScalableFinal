package com.ecommerce.OrderService.DTO;

public class ProductResponseDTO {
    private Long id;
    private String name;
    private int stock;
    private double price;


    // Getters and setters (boilerplate, you can generate)
    public double getPrice() {
        return price;
    }
    public double setPrice(double price) {
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

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }
}
