package com.marketingsales.backend.repository;

import com.marketingsales.backend.entity.Route;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface RouteRepository extends JpaRepository<Route, UUID> {

    boolean existsByNameIgnoreCaseAndZoneIgnoreCase(String name, String zone);

    boolean existsByNameIgnoreCaseAndZoneIgnoreCaseAndIdNot(String name, String zone, UUID id);

    @Query("""
            SELECT r FROM Route r
            WHERE (:searchPattern = '' OR LOWER(r.name) LIKE :searchPattern)
              AND (:zonePattern = '' OR LOWER(r.zone) LIKE :zonePattern)
              AND (:active IS NULL OR r.active = :active)
            """)
    Page<Route> searchRoutes(
            @Param("searchPattern") String searchPattern,
            @Param("zonePattern") String zonePattern,
            @Param("active") Boolean active,
            Pageable pageable
    );
}
