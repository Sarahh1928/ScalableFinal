package com.ecommerce.ProductService.services.factory;

import com.ecommerce.ProductService.models.Product;
import com.ecommerce.ProductService.models.enums.ProductCategory;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class ProductFactory {

    public static Product createProduct(ProductCategory category) {
        try {
            // Create the product instance based on the category
            Product product = category.getProductClass().getDeclaredConstructor().newInstance();

            // Add size list based on category
            List<String> sizeList = generateSizeList(category);
            product.setSizeList(sizeList);

            return product;
        } catch (Exception e) {
            throw new RuntimeException("Error creating product instance for category: " + category, e);
        }
    }

    // Method to generate size list based on product category
    private static List<String> generateSizeList(ProductCategory category) {
        List<String> sizeList = new ArrayList<>();

        switch (category) {
            case TOP:
                // For clothing, sizes could be S, M, L, XL, etc.
                sizeList.add("S");
                sizeList.add("M");
                sizeList.add("L");
                sizeList.add("XL");
                break;
            case BOTTOM:
                // For clothing, sizes could be S, M, L, XL, etc.
                sizeList.add("38");
                sizeList.add("40");
                sizeList.add("42");
                sizeList.add("44");
                sizeList.add("46");
                break;

            case SHOES:
                // For shoes, sizes could be 36-45 (odd/even logic)
                sizeList.add("36");
                sizeList.add("37");
                sizeList.add("38");
                sizeList.add("39");
                sizeList.add("40");
                sizeList.add("41");
                sizeList.add("42");
                sizeList.add("43");
                sizeList.add("44");
                break;

            case ACCESSORIES:
                // Accessories don't have specific sizes, so leave empty or set a default
                break;

            default:
                // Handle other categories or throw an exception if needed
                break;
        }

        return sizeList;
    }
}
