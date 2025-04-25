package com.ecommerce.ProductService.services.observer;

import com.ecommerce.ProductService.models.Product;

public class StockAlertObserver implements ProductObserver {
    @Override
    public void onProductUpdated(Product product) {
        if (product.getStock() < 5) {
            System.out.println("Stock Alert: Product " + product.getName() + " is running low!");
        }
    }
}