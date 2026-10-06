package com.marketingsales.backend.repository;

import com.marketingsales.backend.constant.RouteCounterType;
import com.marketingsales.backend.entity.RoutePlanItem;
import com.marketingsales.backend.repository.projection.RoutePlanItemCountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoutePlanItemRepository extends JpaRepository<RoutePlanItem, UUID> {

    List<RoutePlanItem> findAllByRoutePlanIdOrderByVisitOrderAsc(UUID routePlanId);

    List<RoutePlanItem> findAllByWeekStart(LocalDate weekStart);

    Optional<RoutePlanItem> findByIdAndRoutePlanId(UUID id, UUID routePlanId);

    boolean existsByWeekStartAndCounterTypeAndCounterId(LocalDate weekStart, RouteCounterType counterType, UUID counterId);

    Optional<RoutePlanItem> findByWeekStartAndCounterTypeAndCounterId(LocalDate weekStart, RouteCounterType counterType, UUID counterId);

    long countByRoutePlanId(UUID routePlanId);

    void deleteByRoutePlanId(UUID routePlanId);

    @Query("""
            SELECT i FROM RoutePlanItem i
            WHERE i.routePlanId = :routePlanId
              AND i.id IN :itemIds
            """)
    List<RoutePlanItem> findAllByRoutePlanIdAndIds(
            @Param("routePlanId") UUID routePlanId,
            @Param("itemIds") Collection<UUID> itemIds
    );

    @Query("""
            SELECT i.routePlanId AS routePlanId, COUNT(i.id) AS counterCount
            FROM RoutePlanItem i
            WHERE i.routePlanId IN :routePlanIds
            GROUP BY i.routePlanId
            """)
    List<RoutePlanItemCountProjection> countByRoutePlanIds(@Param("routePlanIds") Collection<UUID> routePlanIds);

    List<RoutePlanItem> findAllByRoutePlanIdInOrderByRoutePlanIdAscVisitOrderAsc(Collection<UUID> routePlanIds);
}