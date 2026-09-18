package com.marketingsales.backend.repository;

import com.marketingsales.backend.constant.ProductStatus;
import com.marketingsales.backend.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySkuIgnoreCase(String sku);

    @Query("""
            SELECT p FROM Product p
            WHERE (:search IS NULL OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:category IS NULL OR LOWER(p.category) = LOWER(:category))
              AND (:uom IS NULL OR LOWER(p.uom) = LOWER(:uom))
              AND (:status IS NULL OR p.status = :status)
            """)
    Page<Product> searchProducts(
            @Param("search") String search,
            @Param("category") String category,
            @Param("uom") String uom,
            @Param("status") ProductStatus status,
            Pageable pageable
    );

    List<Product> findByStatusOrderByProductNameAsc(ProductStatus status);
}
