package com.ecommerce.ProductService.services.factory;

import com.ecommerce.ProductService.models.Product;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
@Entity
@DiscriminatorValue("TOP")
public class TopProduct extends Product {
    // Add any Top-specific fields here

    @Override
    public String getDescription() {
        return "Top wear product: " + super.getDescription();
    }

    // Must have a no-arg constructor for JPA and Jackson
    public TopProduct() {
    }

    // Add any other constructors you need
}