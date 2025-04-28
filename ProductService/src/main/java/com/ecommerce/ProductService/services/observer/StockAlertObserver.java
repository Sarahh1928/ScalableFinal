package com.ecommerce.ProductService.services.observer;

import com.ecommerce.ProductService.models.Product;
import com.ecommerce.ProductService.services.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StockAlertObserver implements ProductObserver {

    @Autowired
    private MailService mailService;

    @Override
    public void onProductUpdated(String alertEmail,Product product) {
        System.out.println("Stock alert observer triggered for: " + product.getName());

        // Ensure alertEmail is not null or empty
        if (alertEmail == null || alertEmail.isEmpty()) {
            throw new IllegalArgumentException("Alert email must not be null or empty.");
        }
        if (product.getStockLevel() < 5) {
            mailService.sendStockAlert(alertEmail, product.getName(), product.getStockLevel());
        }
    }

}
