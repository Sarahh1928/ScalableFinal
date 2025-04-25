package com.ecommerce.ProductService.services.factory;

import com.ecommerce.ProductService.models.Product;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
@Entity
@DiscriminatorValue("BOTTOM")
public class BottomProduct extends Product  {
    public BottomProduct() {
    }

    @Override
    public String getDescription() {
        return "Bottom product";
    }
}