package com.ecommerce.ProductService.models;

import com.ecommerce.ProductService.models.enums.ProductCategory;
import com.ecommerce.ProductService.services.factory.*;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "products")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "dtype"  // Match JPA discriminator column
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TopProduct.class, name = "TOP"),
        @JsonSubTypes.Type(value = BottomProduct.class, name = "BOTTOM"),
        @JsonSubTypes.Type(value = AccessoriesProduct.class, name = "ACCESSORIES"),
        @JsonSubTypes.Type(value = ShoesProduct.class, name = "SHOES")
})
@DiscriminatorValue("PRODUCT")
public abstract class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private double price;
    private int stock;

    @Enumerated(EnumType.STRING)
    private ProductCategory category;
    private Long merchantId;
    @ElementCollection
    private List<String> sizeList;  // New field for sizes

    // Getters and setters
    public List<String> getSizeList() {
        return sizeList;
    }

    public void setSizeList(List<String> sizeList) {
        this.sizeList = sizeList;
    }

    // Getters and Setters

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public void setCategory(ProductCategory category) {
        this.category = category;
    }

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

}