package com.ecommerce.ProductService.repositories;

import com.ecommerce.ProductService.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByPriceBetween(double min, double max);
    boolean existsByMerchantIdAndName(long merchantId, String name);


}
