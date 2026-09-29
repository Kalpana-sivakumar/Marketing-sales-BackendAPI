package com.marketingsales.backend.repository;

import com.marketingsales.backend.constant.CustomerStatus;
import com.marketingsales.backend.entity.Retailer;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RetailerRepository extends JpaRepository<Retailer, UUID> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, UUID id);

    @Query("""
            SELECT r FROM Retailer r
            WHERE (:searchPattern = ''
                    OR LOWER(r.name) LIKE :searchPattern
                    OR LOWER(r.code) LIKE :searchPattern
                    OR LOWER(r.mobile) LIKE :searchPattern)
              AND (:status IS NULL OR r.status = :status)
            """)
    List<Retailer> searchRetailers(
            @Param("searchPattern") String searchPattern,
            @Param("status") CustomerStatus status,
            Sort sort
    );
}
