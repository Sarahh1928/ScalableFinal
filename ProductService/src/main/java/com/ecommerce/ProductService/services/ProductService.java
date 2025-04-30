package com.ecommerce.ProductService.services;

import com.ecommerce.ProductService.Dto.UserSessionDTO;
import com.ecommerce.ProductService.models.Product;
import com.ecommerce.ProductService.models.ProductReview;
import com.ecommerce.ProductService.models.enums.ProductCategory;
import com.ecommerce.ProductService.repositories.ProductRepository;
import com.ecommerce.ProductService.repositories.ProductReviewRepository;
import com.ecommerce.ProductService.models.Accessory;
import com.ecommerce.ProductService.models.Clothing;
import com.ecommerce.ProductService.services.factory.ProductFactory;
import com.ecommerce.ProductService.services.observer.ProductSubject;
import com.ecommerce.ProductService.services.observer.StockAlertObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductReviewRepository productReviewRepository;

    private final ProductSubject subject = new ProductSubject();

    private final RedisTemplate<String, UserSessionDTO> sessionRedisTemplate;


    @Autowired
    public ProductService(StockAlertObserver observer, RedisTemplate<String, UserSessionDTO> sessionRedisTemplate) {
        this.sessionRedisTemplate = sessionRedisTemplate;
        subject.registerObserver(observer);
    }

    public String getToken(String token) {
        UserSessionDTO userSession=sessionRedisTemplate.opsForValue().get(token);
        assert userSession != null;
        return userSession.toString();
    }
    public UserSessionDTO getUserSessionFromToken(String token) {
        // Retrieve the user session from Redis using the provided token
        UserSessionDTO userSession=sessionRedisTemplate.opsForValue().get(token);

        // Check if the session exists and if the user role is "merchant"
        return userSession;
    }

    public Product createProduct(long merchantId,ProductCategory category, Map<String, Object> input) {
        try {
            Product newProduct = ProductFactory.createProduct(category);

            // Common attributes
            newProduct.setName((String) input.get("name"));
            newProduct.setPrice(Double.parseDouble(input.get("price").toString()));
            newProduct.setBrand((String) input.get("brand"));
            newProduct.setColor((String) input.get("color"));
            newProduct.setMerchantId(merchantId);
            newProduct.setStockLevel(Integer.parseInt(input.get("stockLevel").toString()));

            // Specific attributes
            if (newProduct instanceof Clothing) {
                Clothing clothing = (Clothing) newProduct;
                clothing.setDetails(
                        (String) input.get("size"),
                        (String) input.get("material"),
                        (String) input.get("gender"),
                        (String) input.get("season")
                );
            } else if (newProduct instanceof Accessory) {
                Accessory accessory = (Accessory) newProduct;
                accessory.setDetails(
                        (String) input.get("type"),
                        (String) input.get("material"),
                        Boolean.parseBoolean(input.get("unisex").toString())
                );
            }

             productRepository.save(newProduct);
            return newProduct;
        } catch (Exception e) {
            throw new RuntimeException("Error creating a new product from input: " + input, e);
        }
    }



    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> filterProductsByPrice(double min, double max) {
        return productRepository.findByPriceBetween(min, max);
    }

    public Product updateProduct(Long userId,String alertEmail,long uid, Product updatedProduct) {
        Product existingProduct = productRepository.findById(uid)
                .orElseThrow(() -> new RuntimeException("Product not found with UID: " + uid));
        if(existingProduct.getMerchantId()!=userId){
            throw new RuntimeException("Only the owner of Product can update it");
        }
        // Update common attributes
        existingProduct.setCommonAttributes(
                updatedProduct.getName(),
                updatedProduct.getPrice(),
                updatedProduct.getBrand(),
                updatedProduct.getColor(),
                updatedProduct.getMerchantId(),
                updatedProduct.getStockLevel()
        );

        // Handle specific attributes for different product types
        if (existingProduct instanceof Clothing && updatedProduct instanceof Clothing) {
            Clothing clothing = (Clothing) existingProduct;
            Clothing updatedClothing = (Clothing) updatedProduct;
            clothing.setDetails(
                    updatedClothing.getSize(),
                    updatedClothing.getMaterial(),
                    updatedClothing.getGender(),
                    updatedClothing.getSeason()
            );
        } else if (existingProduct instanceof Accessory && updatedProduct instanceof Accessory) {
            Accessory accessory = (Accessory) existingProduct;
            Accessory updatedAccessory = (Accessory) updatedProduct;
            accessory.setDetails(
                    updatedAccessory.getType(),
                    updatedAccessory.getMaterial(),
                    updatedAccessory.isUnisex()
            );
        }
        // Notify observers (make sure subject is properly initialized)
        if (subject != null) {
            subject.notifyObservers(alertEmail,updatedProduct);
        }


        // Save the updated product to the repository (DB)
        return productRepository.save(existingProduct);
    }

    public void deleteProduct(Long userId,Long id) {
        Product product=productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));
        if(product.getMerchantId()!=userId){
            throw new RuntimeException("Only the owner of Product can delete it");
        }
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

    public Product addStock(String alertEmail,Long id, int stock) {
        // Find the product by ID
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Update the stock level
        product.setStockLevel(product.getStockLevel()+stock);

        // Save the updated product to the database
        Product updatedProduct = productRepository.save(product);

        // Notify observers (make sure subject is properly initialized)
        if (subject != null) {
            subject.notifyObservers(alertEmail,updatedProduct);
        }

        return updatedProduct; // Return the updated product
    }
    public Product removeStock(String alertEmail,Long id, int stock) {
        // Find the product by ID
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Update the stock level
        product.setStockLevel(product.getStockLevel()-stock);

        // Save the updated product to the database
        Product updatedProduct = productRepository.save(product);

        // Notify observers (make sure subject is properly initialized)
        if (subject != null) {
            subject.notifyObservers(alertEmail,updatedProduct);
        }

        return updatedProduct; // Return the updated product
    }

}
