package com.ecommerce.ProductService.services.observer;

import com.ecommerce.ProductService.models.Product;

public interface ProductObserver {
    void onProductUpdated(Product product);

}
