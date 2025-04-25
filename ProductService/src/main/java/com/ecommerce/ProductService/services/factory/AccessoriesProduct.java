package com.ecommerce.ProductService.services.factory;

import com.ecommerce.ProductService.models.Product;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
@Entity
@DiscriminatorValue("ACCESSORIES")
public class AccessoriesProduct extends Product {
    public AccessoriesProduct() {
    }

    @Override
    public String getDescription() {
        return "Accessory product";
    }
}

