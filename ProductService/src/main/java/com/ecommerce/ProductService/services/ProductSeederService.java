package com.ecommerce.ProductService.services;

import com.ecommerce.ProductService.models.*;
import com.ecommerce.ProductService.repositories.ProductRepository;
import com.ecommerce.ProductService.repositories.ProductReviewRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProductSeederService {

    private final ProductRepository productRepository;
    private final ProductReviewRepository reviewRepository;

    public ProductSeederService(ProductRepository productRepository, ProductReviewRepository reviewRepository) {
        this.productRepository = productRepository;
        this.reviewRepository = reviewRepository;
    }

    public String seedEverything() {
        if (productRepository.count() > 0) return "❌ products have been seeded";

        List<Long> merchantIds = List.of(4L, 5L, 6L);
        int productCounter = 1;

        for (Long merchantId : merchantIds) {
            for (int i = 1; i <= 5; i++) {
                Product product;
                String productName = "Product-" + productCounter++;

                if (i % 2 == 0) {
                    // Even: Clothing
                    Clothing clothing = new Clothing();
                    clothing.setCommonAttributes(
                            productName, 150.0, "Zara",
                            "Black", merchantId, 30
                    );
                    clothing.setDetails("M", "Cotton", "Male", "Winter");
                    product = productRepository.save(clothing);
                } else {
                    // Odd: Accessory
                    Accessory accessory = new Accessory();
                    accessory.setCommonAttributes(
                            productName, 250.0, "Gucci",
                            "Gold", merchantId, 15
                    );
                    accessory.setDetails("Ring", "Metal", true);
                    product = productRepository.save(accessory);
                }

                seedReview(product.getUid());
            }
        }

        return "✅ Static products and reviews seeded.";
    }

    private void seedReview(Long productId) {
        List<String> comments = List.of("Top quality.", "Perfect gift.");
        List<Integer> ratings = List.of(5, 4);
        Long userId = 1L;  // Or random userId if you want

        for (int i = 0; i < comments.size(); i++) {
            ProductReview review = new ProductReview();
            review.setProductId(productId);
            review.setUserId(userId + i);  // just to differ userId maybe
            review.setComment(comments.get(i));
            review.setRating(ratings.get(i));

            reviewRepository.save(review);
        }
    }

}
