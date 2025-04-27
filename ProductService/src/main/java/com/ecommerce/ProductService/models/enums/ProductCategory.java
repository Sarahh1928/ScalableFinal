package com.ecommerce.ProductService.models.enums;

import com.ecommerce.ProductService.models.Accessory;
import com.ecommerce.ProductService.models.Clothing;
import com.ecommerce.ProductService.models.Product;

public enum ProductCategory {
    CLOTHING(Clothing.class),
    ACCESSORY(Accessory.class);

    private final Class<? extends Product> productClass;

    ProductCategory(Class<? extends Product> productClass) {
        this.productClass = productClass;
    }

    public Class<? extends Product> getProductClass() {
        return productClass;
    }
}
