package com.ecommerce.ProductService.services.factory;

import com.ecommerce.ProductService.models.Product;
import com.ecommerce.ProductService.models.enums.ProductCategory;

public class ProductFactory {
    public static Product createProduct(ProductCategory category) {
        try {
            return category.getProductClass()
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Error creating product instance for category: " + category, e);
        }
    }
}