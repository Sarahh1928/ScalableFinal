package com.ecommerce.ProductService.controllers;

import com.ecommerce.ProductService.Dto.UserSessionDTO;
import com.ecommerce.ProductService.models.Product;
import com.ecommerce.ProductService.models.ProductReview;
import com.ecommerce.ProductService.models.enums.ProductCategory;
import com.ecommerce.ProductService.services.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    private boolean isMerchantUser(String token) {
        // Assuming you decode the token to get user information
        // You can implement this method to validate the user role from the token
        UserSessionDTO userSession = productService.getUserSessionFromToken(token);  // You need to implement this method
        return userSession != null && "MERCHANT".equalsIgnoreCase(userSession.getRole());
    }
    private String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);  // Remove "Bearer " prefix
        }
        return null;  // If the header doesn't contain a Bearer token, return null
    }
    // 📌 Add a new product (Factory pattern used internally)
    @PostMapping
    public ResponseEntity<?> addProduct(
            @RequestParam("category") ProductCategory category,
            @RequestBody Map<String, Object> product,
            @RequestHeader("Authorization") String authorizationHeader) {

        // Extract token from Authorization header
        String token = extractToken(authorizationHeader);

        // Get user session details from the token
        UserSessionDTO userSession = productService.getUserSessionFromToken(token);
        if (userSession == null || !"MERCHANT".equals(userSession.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized: Only merchants can add products."+userSession);
        }

        // Proceed to add product if the user is authorized
        Product newProduct = productService.createProduct(userSession.getUserId(),category, product);
        return ResponseEntity.status(HttpStatus.CREATED).body(newProduct);
    }

    // 📌 Get all products
    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/getToken")
    public String logoutUser(@RequestHeader("Authorization") String token) {
        String actualToken = extractToken(token);
        return productService.getToken(actualToken);
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
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestBody Product updatedProduct,
            @RequestHeader("Authorization") String authorizationHeader) {
        String token = extractToken(authorizationHeader);

        // Get user session details from the token
        UserSessionDTO userSession = productService.getUserSessionFromToken(token);
        if (userSession == null || !"MERCHANT".equals(userSession.getRole())) {
            throw new NullPointerException("Unauthorized: Only merchants can add products."+userSession);
        }

        Product product = productService.updateProduct(userSession.getUserId(),userSession.getEmail(),id, updatedProduct);
        return ResponseEntity.ok(product);
    }

    // 📌 Delete product
    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id,
                              @RequestHeader("Authorization") String authorizationHeader) {
        String token = extractToken(authorizationHeader);

        // Get user session details from the token
        UserSessionDTO userSession = productService.getUserSessionFromToken(token);
        if (userSession == null || !"MERCHANT".equals(userSession.getRole())) {
            throw new NullPointerException("Unauthorized: Only merchants can add products."+userSession);
        }
        productService.deleteProduct(userSession.getUserId(),id);
    }

    // 📌 Get product by ID
    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @PostMapping("/{productId}/reviews")
    public ProductReview addReview(
            @PathVariable Long productId,
            @RequestBody ProductReview review,
            @RequestHeader("Authorization") String authorizationHeader) {
        // Extract token from Authorization header
        String token = extractToken(authorizationHeader);

        // Get user session details from the token
        UserSessionDTO userSession = productService.getUserSessionFromToken(token);
        if (userSession == null || !"CUSTOMER".equals(userSession.getRole())) {
            throw new NullPointerException("Unauthorized: Only merchants can add products."+userSession);
        }
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

    @PutMapping("/{id}/addstock")
    public Product addStock(
            @PathVariable Long id,
            @RequestParam int stock,
            @RequestHeader("Authorization") String authorizationHeader) {
        String token = extractToken(authorizationHeader);

        // Get user session details from the token
        UserSessionDTO userSession = productService.getUserSessionFromToken(token);
        if (userSession == null || !"CUSTOMER".equals(userSession.getRole())) {

            throw new NullPointerException("Unauthorized: Only merchants can add products."+userSession);
        }
        Product product = productService.addStock(userSession.getEmail(),id, stock);
        return product;
    }

    @PutMapping("/{id}/removestock")
    public Product removeStock(
            @PathVariable Long id,
            @RequestParam int stock,
            @RequestHeader("Authorization") String authorizationHeader) {
        String token = extractToken(authorizationHeader);

        // Get user session details from the token
        UserSessionDTO userSession = productService.getUserSessionFromToken(token);
        if (userSession == null || !"CUSTOMER".equals(userSession.getRole())) {
            throw new NullPointerException("Unauthorized: Only merchants can add products."+userSession);
        }
        Product product = productService.removeStock(userSession.getEmail(),id, stock);
        return product;
    }
}
