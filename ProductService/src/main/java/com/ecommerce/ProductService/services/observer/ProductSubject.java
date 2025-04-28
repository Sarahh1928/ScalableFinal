package com.ecommerce.ProductService.services.observer;

import com.ecommerce.ProductService.models.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductSubject {
    private final List<ProductObserver> observers = new ArrayList<>();

    public void registerObserver(ProductObserver observer) {
        observers.add(observer);
    }

    public void notifyObservers(String alertEmail,Product product) {
        for (ProductObserver observer : observers) {
            observer.onProductUpdated(alertEmail,product);
        }
    }
}