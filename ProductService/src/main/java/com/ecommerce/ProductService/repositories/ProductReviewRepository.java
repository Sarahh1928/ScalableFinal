package com.ecommerce.ProductService.repositories;

import com.ecommerce.ProductService.models.ProductReview;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductReviewRepository extends MongoRepository<ProductReview, String> {
    List<ProductReview> findByProductId(Long productId);
    boolean existsByProductIdAndUserId(Long productId, Long userId);

}
