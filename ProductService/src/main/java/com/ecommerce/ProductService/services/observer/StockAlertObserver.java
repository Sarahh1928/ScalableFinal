package com.ecommerce.ProductService.services.observer;

import com.ecommerce.ProductService.models.Product;
import com.ecommerce.ProductService.services.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StockAlertObserver implements ProductObserver {

    @Autowired
    private MailService mailService;

    private final String alertEmail = "sarahmohamed1928@gmail.com"; // or dynamically fetched

    @Override
    public void onProductUpdated(Product product) {
        System.out.println("Stock alert observer triggered for: " + product.getName());
        if (product.getStockLevel() < 5) {
            mailService.sendStockAlert(alertEmail, product.getName(), product.getStockLevel());
        }
    }

}
