package com.ecommerce.ProductService.models.enums;

import com.ecommerce.ProductService.models.Product;
import com.ecommerce.ProductService.services.factory.*;

public enum ProductCategory {
    TOP(TopProduct.class),
    BOTTOM(BottomProduct.class),
    ACCESSORIES(AccessoriesProduct.class),
    SHOES(ShoesProduct.class);

    private final Class<? extends Product> productClass;

    ProductCategory(Class<? extends Product> productClass) {
        this.productClass = productClass;
    }

    public Class<? extends Product> getProductClass() {
        return productClass;
    }
}