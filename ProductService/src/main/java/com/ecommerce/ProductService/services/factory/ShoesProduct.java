package com.ecommerce.ProductService.services.factory;

import com.ecommerce.ProductService.models.Product;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
@Entity
@DiscriminatorValue("SHOES")
public class ShoesProduct extends Product {
    @Override
    public String getDescription() {
        return "Shoes product";
    }

    public ShoesProduct() {
    }
}
