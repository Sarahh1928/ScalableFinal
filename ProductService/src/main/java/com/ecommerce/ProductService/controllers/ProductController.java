package com.ecommerce.ProductService.controllers;

import com.ecommerce.ProductService.models.Product;
import com.ecommerce.ProductService.models.ProductReview;
import com.ecommerce.ProductService.services.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // 📌 Add a new product (Factory pattern used internally)
    @PostMapping
    public Product addProduct(@RequestBody Product product) {
        return productService.createProduct(product);
    }

    // 📌 Get all products
    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    // 📌 Filter by price range
    @GetMapping("/filter")
    public List<Product> filterByPriceRange(
            @RequestParam double min,
            @RequestParam double max) {
        return productService.filterProductsByPrice(min, max);
    }

    // 📌 Update product
    @PutMapping("/{id}")
    public Product updateProduct(
            @PathVariable Long id,
            @RequestBody Product updatedProduct) {
        return productService.updateProduct(id, updatedProduct);
    }

    // 📌 Delete product
    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
    }

    // 📌 Get product by ID
    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @PostMapping("/{productId}/reviews")
    public ProductReview addReview(
            @PathVariable Long productId,
            @RequestBody ProductReview review) {
        return productService.addReview(productId, review);
    }

    @GetMapping("/{productId}/reviews")
    public List<ProductReview> getReviews(@PathVariable Long productId) {
        return productService.getReviews(productId);
    }
    @GetMapping("/{productId}/average-rating")
    public double getAverageRating(@PathVariable Long productId) {
        return productService.getAverageRating(productId);
    }
    @PutMapping("/{id}/stock")
    public Product updateStock(
            @PathVariable Long id,
            @RequestParam int stock) {
        Product product = productService.updateStock(id, stock);
        return product;
    }
}
