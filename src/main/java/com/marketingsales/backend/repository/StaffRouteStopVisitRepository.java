package com.marketingsales.backend.repository;

import com.marketingsales.backend.constant.RouteVisitStatus;
import com.marketingsales.backend.entity.StaffRouteStopVisit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StaffRouteStopVisitRepository extends JpaRepository<StaffRouteStopVisit, UUID> {

    Optional<StaffRouteStopVisit> findByStaffIdAndRoutePlanItemIdAndVisitDate(UUID staffId, UUID routePlanItemId, LocalDate visitDate);

    List<StaffRouteStopVisit> findAllByStaffIdAndVisitDateAndRoutePlanItemIdIn(UUID staffId, LocalDate visitDate, Collection<UUID> routePlanItemIds);

    Optional<StaffRouteStopVisit> findByStaffIdAndRoutePlanItemIdAndVisitDateAndStatus(
            UUID staffId,
            UUID routePlanItemId,
            LocalDate visitDate,
            RouteVisitStatus status
    );

    List<StaffRouteStopVisit> findAllByVisitDateOrderByCheckInTimeDesc(LocalDate visitDate);

    List<StaffRouteStopVisit> findAllByStaffIdAndVisitDateOrderByCheckInTimeDesc(UUID staffId, LocalDate visitDate);

    List<StaffRouteStopVisit> findAllByVisitDateAndStaffIdInAndRoutePlanItemIdIn(
            LocalDate visitDate,
            Collection<UUID> staffIds,
            Collection<UUID> routePlanItemIds
    );
}