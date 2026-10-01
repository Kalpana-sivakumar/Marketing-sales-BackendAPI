package com.marketingsales.backend.repository;

import com.marketingsales.backend.constant.CustomerStatus;
import com.marketingsales.backend.entity.Distributor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DistributorRepository extends JpaRepository<Distributor, UUID> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, UUID id);

    @Query("""
            SELECT d FROM Distributor d
            WHERE (:searchPattern = ''
                    OR LOWER(d.name) LIKE :searchPattern
                    OR LOWER(d.code) LIKE :searchPattern
                    OR LOWER(d.mobile) LIKE :searchPattern)
              AND (:status IS NULL OR d.status = :status)
            """)
    List<Distributor> searchDistributors(
            @Param("searchPattern") String searchPattern,
            @Param("status") CustomerStatus status,
            Sort sort
    );

    Optional<Distributor> findById(UUID id);

    List<Distributor> findAllByMasterRouteIdAndStatusOrderByNameAsc(UUID masterRouteId, CustomerStatus status);

    @Query("""
            SELECT d FROM Distributor d
            WHERE d.status = :status
              AND (:searchPattern = ''
                    OR LOWER(d.name) LIKE :searchPattern
                    OR LOWER(d.code) LIKE :searchPattern
                    OR LOWER(d.mobile) LIKE :searchPattern)
            ORDER BY d.name ASC
            """)
    List<Distributor> searchActiveDistributors(
            @Param("status") CustomerStatus status,
            @Param("searchPattern") String searchPattern
    );
}
