package com.marketingsales.backend.repository;

import com.marketingsales.backend.constant.RoutePlanStatus;
import com.marketingsales.backend.entity.RoutePlan;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoutePlanRepository extends JpaRepository<RoutePlan, UUID> {

    boolean existsByWeekStartAndStaffIdAndRouteIdNot(LocalDate weekStart, UUID staffId, UUID routeId);

    boolean existsByWeekStartAndStaffId(LocalDate weekStart, UUID staffId);

    @EntityGraph(attributePaths = {"route", "staff"})
    Optional<RoutePlan> findByRouteIdAndWeekStart(UUID routeId, LocalDate weekStart);

    @Query("""
            SELECT rp FROM RoutePlan rp
            WHERE rp.weekStart = :weekStart
              AND rp.staffId = :staffId
              AND rp.status = :status
            """)
    Optional<RoutePlan> findByStaffAndWeekAndStatus(
            @Param("staffId") UUID staffId,
            @Param("weekStart") LocalDate weekStart,
            @Param("status") RoutePlanStatus status
    );

    @Query("""
            SELECT rp FROM RoutePlan rp
            WHERE rp.weekStart = :weekStart
              AND rp.routeId IN :routeIds
            """)
    List<RoutePlan> findAllByRouteIdsAndWeekStart(
            @Param("routeIds") List<UUID> routeIds,
            @Param("weekStart") LocalDate weekStart
    );

    List<RoutePlan> findAllByWeekStart(LocalDate weekStart);
}