package com.ecommerce.ProductService.services;

import com.ecommerce.ProductService.models.Product;
import com.ecommerce.ProductService.models.ProductReview;
import com.ecommerce.ProductService.repositories.ProductRepository;
import com.ecommerce.ProductService.repositories.ProductReviewRepository;
import com.ecommerce.ProductService.services.factory.ProductFactory;
import com.ecommerce.ProductService.services.observer.ProductSubject;
import com.ecommerce.ProductService.services.observer.StockAlertObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductReviewRepository productReviewRepository;

    private final ProductSubject subject = new ProductSubject();

    public ProductService() {
        subject.registerObserver(new StockAlertObserver());
    }

    public Product createProduct(Product input) {
        // Create the appropriate product type
        Product product = ProductFactory.createProduct(input.getCategory());

        // Copy all properties from input to the new product
        product.setName(input.getName());
        product.setDescription(input.getDescription());
        product.setPrice(input.getPrice());
        product.setStock(input.getStock());
        product.setCategory(input.getCategory());
        product.setMerchantId(input.getMerchantId());

        // Save and return
        Product saved = productRepository.save(product);
        subject.notifyObservers(saved);
        return saved;
    }


    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> filterProductsByPrice(double min, double max) {
        return productRepository.findByPriceBetween(min, max);
    }

    public Product updateProduct(Long id, Product updated) {
        Product product = productRepository.findById(id).orElseThrow();
        product.setName(updated.getName());
        product.setDescription(updated.getDescription());
        product.setPrice(updated.getPrice());
        product.setStock(updated.getStock());
        product.setCategory(updated.getCategory());
        product.setMerchantId(updated.getMerchantId());

        Product saved = productRepository.save(product);
        subject.notifyObservers(saved);
        return saved;
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id).orElseThrow();
    }
    public ProductReview addReview(Long productId, ProductReview incomingReview) {
        if (!productRepository.existsById(productId)) {
            throw new IllegalArgumentException("Product not found with ID: " + productId);
        }

        // Find existing ProductReview for this product (assuming 1 per product)
        ProductReview reviewDoc = productReviewRepository.findByProductId(productId)
                .stream().findFirst()
                .orElseGet(() -> {
                    ProductReview newReview = new ProductReview();
                    newReview.setProductId(productId);
                    return newReview;
                });

        // Append new reviews/ratings
        if (incomingReview.getReviews() != null) {
            reviewDoc.getReviews().addAll(incomingReview.getReviews());
        }
        if (incomingReview.getRatings() != null) {
            reviewDoc.getRatings().addAll(incomingReview.getRatings());
        }

        return productReviewRepository.save(reviewDoc);
    }


    public List<ProductReview> getReviews(Long productId) {
        return productReviewRepository.findByProductId(productId);
    }
    public double getAverageRating(Long productId) {
        List<ProductReview> reviews = getReviews(productId);
        if (reviews.isEmpty()) return 0.0;

        return reviews.stream()
                .flatMapToInt(review -> review.getRatings().stream().mapToInt(Integer::intValue))
                .average()
                .orElse(0.0);
    }
}
