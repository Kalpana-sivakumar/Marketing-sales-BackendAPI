package com.marketingsales.backend.repository;

import com.marketingsales.backend.constant.CustomerStatus;
import com.marketingsales.backend.entity.CustomerCounter;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CustomerCounterRepository extends JpaRepository<CustomerCounter, UUID> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, UUID id);

    @Query("""
            SELECT c FROM CustomerCounter c
            JOIN c.distributor d
            WHERE (:searchPattern = ''
                    OR LOWER(c.name) LIKE :searchPattern
                    OR LOWER(c.code) LIKE :searchPattern
                    OR LOWER(c.mobile) LIKE :searchPattern)
              AND (:status IS NULL OR c.status = :status)
              AND (:distributorId IS NULL OR c.distributorId = :distributorId)
            """)
    List<CustomerCounter> searchCounters(
            @Param("searchPattern") String searchPattern,
            @Param("status") CustomerStatus status,
            @Param("distributorId") UUID distributorId,
            Sort sort
    );

    List<CustomerCounter> findAllByMasterRouteIdAndStatusOrderByNameAsc(UUID masterRouteId, CustomerStatus status);

    @Query("""
            SELECT c FROM CustomerCounter c
            WHERE c.status = :status
              AND (:searchPattern = ''
                    OR LOWER(c.name) LIKE :searchPattern
                    OR LOWER(c.code) LIKE :searchPattern
                    OR LOWER(c.mobile) LIKE :searchPattern)
            ORDER BY c.name ASC
            """)
    List<CustomerCounter> searchActiveCustomerCounters(
            @Param("status") CustomerStatus status,
            @Param("searchPattern") String searchPattern
    );
}
