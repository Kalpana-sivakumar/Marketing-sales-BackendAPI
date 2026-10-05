package com.marketingsales.backend.service.impl;

import com.marketingsales.backend.constant.AttendanceStatus;
import com.marketingsales.backend.constant.RouteCounterType;
import com.marketingsales.backend.constant.RoutePlanStatus;
import com.marketingsales.backend.constant.RouteVisitStatus;
import com.marketingsales.backend.dto.request.AttendanceActionRequest;
import com.marketingsales.backend.dto.response.AdminRouteTrackingRowResponse;
import com.marketingsales.backend.dto.response.StaffRouteResponse;
import com.marketingsales.backend.dto.response.StaffRouteStopResponse;
import com.marketingsales.backend.dto.response.StaffRouteStopVisitResponse;
import com.marketingsales.backend.entity.CustomerCounter;
import com.marketingsales.backend.entity.Distributor;
import com.marketingsales.backend.entity.Retailer;
import com.marketingsales.backend.entity.Route;
import com.marketingsales.backend.entity.RoutePlan;
import com.marketingsales.backend.entity.RoutePlanItem;
import com.marketingsales.backend.entity.StaffRouteStopVisit;
import com.marketingsales.backend.exception.BadRequestException;
import com.marketingsales.backend.exception.ResourceNotFoundException;
import com.marketingsales.backend.repository.CustomerCounterRepository;
import com.marketingsales.backend.repository.DistributorRepository;
import com.marketingsales.backend.repository.RetailerRepository;
import com.marketingsales.backend.repository.RoutePlanItemRepository;
import com.marketingsales.backend.repository.RoutePlanRepository;
import com.marketingsales.backend.repository.RouteRepository;
import com.marketingsales.backend.repository.StaffAttendanceRepository;
import com.marketingsales.backend.repository.StaffRouteStopVisitRepository;
import com.marketingsales.backend.repository.UserRepository;
import com.marketingsales.backend.service.StaffRouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffRouteServiceImpl implements StaffRouteService {

    @Value("${app.route.stop-radius-meters:250}")
    private double stopRadiusMeters;

    private final RoutePlanRepository routePlanRepository;
    private final RoutePlanItemRepository routePlanItemRepository;
    private final RouteRepository routeRepository;
    private final DistributorRepository distributorRepository;
    private final CustomerCounterRepository customerCounterRepository;
    private final RetailerRepository retailerRepository;
    private final StaffAttendanceRepository staffAttendanceRepository;
    private final StaffRouteStopVisitRepository staffRouteStopVisitRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<StaffRouteResponse> getPublishedRouteForDate(UUID staffId, LocalDate date) {
        LocalDate actualDate = date != null ? date : LocalDate.now();
        LocalDate weekStart = actualDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        Optional<RoutePlan> planOptional = routePlanRepository.findByStaffAndWeekAndStatus(staffId, weekStart, RoutePlanStatus.PUBLISHED);
        if (planOptional.isEmpty()) {
            return Optional.empty();
        }

        RoutePlan plan = planOptional.get();
        List<RoutePlanItem> items = routePlanItemRepository.findAllByRoutePlanIdOrderByVisitOrderAsc(plan.getId());
        List<UUID> itemIds = items.stream().map(RoutePlanItem::getId).toList();

        List<StaffRouteStopVisit> visitRows = itemIds.isEmpty()
                ? List.of()
                : staffRouteStopVisitRepository.findAllByStaffIdAndVisitDateAndRoutePlanItemIdIn(staffId, actualDate, itemIds);
        Map<UUID, StaffRouteStopVisit> visitByItemId = visitRows.stream()
                .collect(Collectors.toMap(StaffRouteStopVisit::getRoutePlanItemId, Function.identity(), (left, right) -> left));

        List<UUID> distributorIds = items.stream()
                .filter(item -> item.getCounterType() == RouteCounterType.DISTRIBUTOR)
                .map(RoutePlanItem::getCounterId)
                .toList();
        List<UUID> customerIds = items.stream()
                .filter(item -> item.getCounterType() == RouteCounterType.CUSTOMER)
                .map(RoutePlanItem::getCounterId)
                .toList();
        List<UUID> retailerIds = items.stream()
                .filter(item -> item.getCounterType() == RouteCounterType.RETAILER)
                .map(RoutePlanItem::getCounterId)
                .toList();

        Map<UUID, Distributor> distributors = distributorIds.isEmpty()
                ? Map.of()
                : distributorRepository.findAllById(distributorIds).stream().collect(Collectors.toMap(Distributor::getId, Function.identity()));
        Map<UUID, CustomerCounter> customers = customerIds.isEmpty()
                ? Map.of()
                : customerCounterRepository.findAllById(customerIds).stream().collect(Collectors.toMap(CustomerCounter::getId, Function.identity()));
        Map<UUID, Retailer> retailers = retailerIds.isEmpty()
                ? Map.of()
                : retailerRepository.findAllById(retailerIds).stream().collect(Collectors.toMap(Retailer::getId, Function.identity()));

        List<StaffRouteStopResponse> stops = items.stream().map(item -> {
            StaffRouteStopVisit visit = visitByItemId.get(item.getId());
            RouteVisitStatus visitStatus = resolveVisitStatus(visit);
            Instant checkInTime = visit != null ? visit.getCheckInTime() : null;
            Instant checkOutTime = visit != null ? visit.getCheckOutTime() : null;
            Long durationSeconds = (visit != null && visit.getCheckOutTime() != null)
                    ? Duration.between(visit.getCheckInTime(), visit.getCheckOutTime()).getSeconds()
                    : null;

            if (item.getCounterType() == RouteCounterType.DISTRIBUTOR) {
                Distributor d = distributors.get(item.getCounterId());
                return StaffRouteStopResponse.builder()
                        .itemId(item.getId())
                        .visitOrder(item.getVisitOrder())
                        .counterType(item.getCounterType())
                        .category("DISTRIBUTOR")
                        .counterId(item.getCounterId())
                        .counterName(d != null ? d.getName() : null)
                        .contactPerson(d != null ? d.getContactPerson() : null)
                        .zone(d != null ? d.getZone() : null)
                        .address(d != null ? d.getAddress() : null)
                        .phone(d != null ? d.getMobile() : null)
                        .latitude(d != null ? d.getLatitude() : null)
                        .longitude(d != null ? d.getLongitude() : null)
                        .outstandingAmount(d != null ? d.getOutstandingAmount() : null)
                        .visitStatus(visitStatus)
                        .checkInTime(checkInTime)
                        .checkOutTime(checkOutTime)
                        .visitDurationSeconds(durationSeconds)
                        .build();
            }

            if (item.getCounterType() == RouteCounterType.CUSTOMER) {
                CustomerCounter c = customers.get(item.getCounterId());
                return StaffRouteStopResponse.builder()
                        .itemId(item.getId())
                        .visitOrder(item.getVisitOrder())
                        .counterType(item.getCounterType())
                        .category("CUSTOMER")
                        .counterId(item.getCounterId())
                        .counterName(c != null ? c.getName() : null)
                        .contactPerson(c != null ? c.getContactPerson() : null)
                        .zone(c != null && c.getDistributor() != null ? c.getDistributor().getZone() : null)
                        .address(c != null ? c.getAddress() : null)
                        .phone(c != null ? c.getMobile() : null)
                        .latitude(c != null ? c.getLatitude() : null)
                        .longitude(c != null ? c.getLongitude() : null)
                        .outstandingAmount(c != null ? c.getOutstandingAmount() : null)
                        .visitStatus(visitStatus)
                        .checkInTime(checkInTime)
                        .checkOutTime(checkOutTime)
                        .visitDurationSeconds(durationSeconds)
                        .build();
            }

            Retailer r = retailers.get(item.getCounterId());
            return StaffRouteStopResponse.builder()
                    .itemId(item.getId())
                    .visitOrder(item.getVisitOrder())
                    .counterType(item.getCounterType())
                    .category("RETAILER")
                    .counterId(item.getCounterId())
                    .counterName(r != null ? r.getName() : null)
                    .contactPerson(r != null ? r.getContactPerson() : null)
                    .zone(r != null ? r.getZone() : null)
                    .address(r != null ? r.getAddress() : null)
                    .phone(r != null ? r.getMobile() : null)
                    .latitude(r != null ? r.getLatitude() : null)
                    .longitude(r != null ? r.getLongitude() : null)
                    .outstandingAmount(r != null ? r.getOutstandingAmount() : null)
                    .visitStatus(visitStatus)
                    .checkInTime(checkInTime)
                    .checkOutTime(checkOutTime)
                    .visitDurationSeconds(durationSeconds)
                    .build();
        }).toList();

        Route route = routeRepository.findById(plan.getRouteId())
                .orElseThrow(() -> new ResourceNotFoundException("Route not found"));

        return Optional.of(StaffRouteResponse.builder()
                .routeId(route.getId())
                .routeName(route.getName())
                .zone(route.getZone())
                .routeDate(actualDate)
                .weekStart(plan.getWeekStart())
                .stops(stops)
                .build());
    }

    @Override
    @Transactional
    public StaffRouteStopVisitResponse checkInStop(UUID staffId, UUID itemId, AttendanceActionRequest request) {
        LocalDate today = LocalDate.now();
        RoutePlanItem planItem = getPublishedPlanItemForStaff(staffId, itemId, today);
        ensureStaffHasActiveAttendance(staffId, today);

        Coordinates target = resolveCounterCoordinates(planItem);
        double distanceMeters = calculateDistanceMeters(
                request.getLatitude(),
                request.getLongitude(),
                target.latitude(),
                target.longitude()
        );
        validateStopDistance(distanceMeters, "check-in");

        Optional<StaffRouteStopVisit> existing = staffRouteStopVisitRepository.findByStaffIdAndRoutePlanItemIdAndVisitDate(staffId, itemId, today);
        if (existing.isPresent()) {
            if (existing.get().getStatus() == RouteVisitStatus.CHECKED_IN) {
                throw new BadRequestException("This stop is already checked in");
            }
            if (existing.get().getStatus() == RouteVisitStatus.DONE) {
                throw new BadRequestException("This stop is already completed");
            }
        }

        StaffRouteStopVisit visit = StaffRouteStopVisit.builder()
                .staffId(staffId)
                .routePlanItemId(itemId)
                .visitDate(today)
                .checkInTime(Instant.now())
                .checkInLatitude(request.getLatitude())
                .checkInLongitude(request.getLongitude())
                .checkInLocationName(request.getPlaceName())
                .checkInGooglePlaceId(request.getGooglePlaceId())
                .checkInDistanceMeters(distanceMeters)
                .status(RouteVisitStatus.CHECKED_IN)
                .build();

        StaffRouteStopVisit saved = staffRouteStopVisitRepository.save(visit);
        return toVisitResponse(saved);
    }

    @Override
    @Transactional
    public StaffRouteStopVisitResponse checkOutStop(UUID staffId, UUID itemId, AttendanceActionRequest request) {
        LocalDate today = LocalDate.now();
        ensureStaffHasActiveAttendance(staffId, today);
        RoutePlanItem planItem = getPublishedPlanItemForStaff(staffId, itemId, today);

        Coordinates target = resolveCounterCoordinates(planItem);
        double distanceMeters = calculateDistanceMeters(
                request.getLatitude(),
                request.getLongitude(),
                target.latitude(),
                target.longitude()
        );
        validateStopDistance(distanceMeters, "check-out");

        StaffRouteStopVisit visit = staffRouteStopVisitRepository
                .findByStaffIdAndRoutePlanItemIdAndVisitDateAndStatus(staffId, itemId, today, RouteVisitStatus.CHECKED_IN)
                .orElseThrow(() -> new BadRequestException("This stop is not checked in yet"));

        visit.setCheckOutTime(Instant.now());
        visit.setCheckOutLatitude(request.getLatitude());
        visit.setCheckOutLongitude(request.getLongitude());
        visit.setCheckOutLocationName(request.getPlaceName());
        visit.setCheckOutGooglePlaceId(request.getGooglePlaceId());
        visit.setStatus(RouteVisitStatus.DONE);

        StaffRouteStopVisit saved = staffRouteStopVisitRepository.save(visit);
        return toVisitResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminRouteTrackingRowResponse> getRouteTracking(LocalDate date, UUID staffId) {
        LocalDate actualDate = date != null ? date : LocalDate.now();
        LocalDate weekStart = actualDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        List<RoutePlan> plans = staffId == null
                ? routePlanRepository.findAllByWeekStartAndStatus(weekStart, RoutePlanStatus.PUBLISHED)
                : routePlanRepository.findByStaffAndWeekAndStatus(staffId, weekStart, RoutePlanStatus.PUBLISHED)
                .map(List::of)
                .orElse(List.of());

        if (plans.isEmpty()) {
            return List.of();
        }

        List<UUID> routePlanIds = plans.stream().map(RoutePlan::getId).toList();
        List<RoutePlanItem> items = routePlanItemRepository.findAllByRoutePlanIdInOrderByRoutePlanIdAscVisitOrderAsc(routePlanIds);
        if (items.isEmpty()) {
            return List.of();
        }

        Map<UUID, RoutePlan> planById = plans.stream().collect(Collectors.toMap(RoutePlan::getId, Function.identity()));

        List<UUID> routeIds = plans.stream().map(RoutePlan::getRouteId).distinct().toList();
        Map<UUID, Route> routeById = routeRepository.findAllById(routeIds).stream()
                .collect(Collectors.toMap(Route::getId, Function.identity()));

        List<UUID> staffIds = plans.stream().map(RoutePlan::getStaffId).distinct().toList();
        Map<UUID, String> staffNameById = userRepository.findAllById(staffIds).stream()
                .collect(Collectors.toMap(user -> user.getId(), user -> user.getFullName()));

        List<UUID> itemIds = items.stream().map(RoutePlanItem::getId).toList();
        List<StaffRouteStopVisit> visits = staffRouteStopVisitRepository.findAllByVisitDateAndStaffIdInAndRoutePlanItemIdIn(actualDate, staffIds, itemIds);
        Map<String, StaffRouteStopVisit> visitByStaffAndItemKey = visits.stream()
                .collect(Collectors.toMap(
                        v -> trackingKey(v.getStaffId(), v.getRoutePlanItemId()),
                        Function.identity(),
                        (left, right) -> left
                ));

        List<UUID> distributorIds = items.stream()
                .filter(item -> item.getCounterType() == RouteCounterType.DISTRIBUTOR)
                .map(RoutePlanItem::getCounterId)
                .distinct()
                .toList();
        List<UUID> customerIds = items.stream()
                .filter(item -> item.getCounterType() == RouteCounterType.CUSTOMER)
                .map(RoutePlanItem::getCounterId)
                .distinct()
                .toList();
        List<UUID> retailerIds = items.stream()
                .filter(item -> item.getCounterType() == RouteCounterType.RETAILER)
                .map(RoutePlanItem::getCounterId)
                .distinct()
                .toList();

        Map<UUID, Distributor> distributors = distributorIds.isEmpty()
                ? Map.of()
                : distributorRepository.findAllById(distributorIds).stream().collect(Collectors.toMap(Distributor::getId, Function.identity()));
        Map<UUID, CustomerCounter> customers = customerIds.isEmpty()
                ? Map.of()
                : customerCounterRepository.findAllById(customerIds).stream().collect(Collectors.toMap(CustomerCounter::getId, Function.identity()));
        Map<UUID, Retailer> retailers = retailerIds.isEmpty()
                ? Map.of()
                : retailerRepository.findAllById(retailerIds).stream().collect(Collectors.toMap(Retailer::getId, Function.identity()));

        return items.stream().map(item -> {
            RoutePlan plan = planById.get(item.getRoutePlanId());
            if (plan == null || plan.getStaffId() == null) {
                return null;
            }

            StaffRouteStopVisit visit = visitByStaffAndItemKey.get(trackingKey(plan.getStaffId(), item.getId()));
            Route route = routeById.get(plan.getRouteId());
            CounterSnapshot snapshot = buildCounterSnapshot(item, distributors, customers, retailers);

            Instant checkInTime = visit != null ? visit.getCheckInTime() : null;
            Instant checkOutTime = visit != null ? visit.getCheckOutTime() : null;
            Long durationSeconds = (checkInTime != null && checkOutTime != null)
                    ? Duration.between(checkInTime, checkOutTime).getSeconds()
                    : null;
            RouteVisitStatus status = visit != null ? visit.getStatus() : RouteVisitStatus.PENDING;

            Double checkInDistanceMeters = null;
            boolean checkInMatched = false;
            if (visit != null && visit.getCheckInLatitude() != null && visit.getCheckInLongitude() != null
                    && snapshot.latitude() != null && snapshot.longitude() != null) {
                checkInDistanceMeters = visit.getCheckInDistanceMeters() != null
                        ? visit.getCheckInDistanceMeters()
                        : calculateDistanceMeters(
                                visit.getCheckInLatitude(),
                                visit.getCheckInLongitude(),
                                snapshot.latitude(),
                                snapshot.longitude()
                        );
                checkInMatched = checkInDistanceMeters <= stopRadiusMeters;
            }

            Double checkOutDistanceMeters = null;
            boolean checkOutMatched = false;
            if (visit != null && visit.getCheckOutLatitude() != null && visit.getCheckOutLongitude() != null
                    && snapshot.latitude() != null && snapshot.longitude() != null) {
                checkOutDistanceMeters = calculateDistanceMeters(
                        visit.getCheckOutLatitude(),
                        visit.getCheckOutLongitude(),
                        snapshot.latitude(),
                        snapshot.longitude()
                );
                checkOutMatched = checkOutDistanceMeters <= stopRadiusMeters;
            }

            // Admin-level verification requires both check-in and check-out to be within the configured radius.
            boolean verified = checkInMatched && checkOutMatched;

            return AdminRouteTrackingRowResponse.builder()
                    .visitId(visit != null ? visit.getId() : null)
                    .staffId(plan.getStaffId())
                    .staffName(staffNameById.get(plan.getStaffId()))
                    .routeId(route != null ? route.getId() : null)
                    .routeName(route != null ? route.getName() : null)
                    .routeWeekStart(plan.getWeekStart())
                    .visitDate(actualDate)
                    .routePlanItemId(item.getId())
                    .visitOrder(item.getVisitOrder())
                    .counterType(item.getCounterType())
                    .category(snapshot.category())
                    .counterId(item.getCounterId())
                    .counterName(snapshot.counterName())
                    .zone(snapshot.zone())
                    .address(snapshot.address())
                    .phone(snapshot.phone())
                    .visitStatus(status)
                    .checkInTime(checkInTime)
                    .checkOutTime(checkOutTime)
                    .checkInLatitude(visit != null ? visit.getCheckInLatitude() : null)
                    .checkInLongitude(visit != null ? visit.getCheckInLongitude() : null)
                    .checkInLocationName(visit != null ? visit.getCheckInLocationName() : null)
                    .checkInGooglePlaceId(visit != null ? visit.getCheckInGooglePlaceId() : null)
                    .checkOutLatitude(visit != null ? visit.getCheckOutLatitude() : null)
                    .checkOutLongitude(visit != null ? visit.getCheckOutLongitude() : null)
                    .checkOutLocationName(visit != null ? visit.getCheckOutLocationName() : null)
                    .checkOutGooglePlaceId(visit != null ? visit.getCheckOutGooglePlaceId() : null)
                    .visitDurationSeconds(durationSeconds)
                    .checkInCoordinateMatched(checkInMatched)
                    .checkOutCoordinateMatched(checkOutMatched)
                    .coordinateVerified(verified)
                    .checkInDistanceMeters(checkInDistanceMeters)
                    .checkOutDistanceMeters(checkOutDistanceMeters)
                    .build();
        }).filter(java.util.Objects::nonNull).toList();
    }

    private CounterSnapshot buildCounterSnapshot(
            RoutePlanItem item,
            Map<UUID, Distributor> distributors,
            Map<UUID, CustomerCounter> customers,
            Map<UUID, Retailer> retailers
    ) {
        if (item == null) {
            return new CounterSnapshot(null, null, null, null, null, null, null);
        }

        if (item.getCounterType() == RouteCounterType.DISTRIBUTOR) {
            Distributor d = distributors.get(item.getCounterId());
            return new CounterSnapshot(
                    "DISTRIBUTOR",
                    d != null ? d.getName() : null,
                    d != null ? d.getZone() : null,
                    d != null ? d.getAddress() : null,
                    d != null ? d.getMobile() : null,
                    d != null ? d.getLatitude() : null,
                    d != null ? d.getLongitude() : null
            );
        }

        if (item.getCounterType() == RouteCounterType.CUSTOMER) {
            CustomerCounter c = customers.get(item.getCounterId());
            return new CounterSnapshot(
                    "CUSTOMER",
                    c != null ? c.getName() : null,
                    c != null && c.getDistributor() != null ? c.getDistributor().getZone() : null,
                    c != null ? c.getAddress() : null,
                    c != null ? c.getMobile() : null,
                    c != null ? c.getLatitude() : null,
                    c != null ? c.getLongitude() : null
            );
        }

        Retailer r = retailers.get(item.getCounterId());
        return new CounterSnapshot(
                "RETAILER",
                r != null ? r.getName() : null,
                r != null ? r.getZone() : null,
                r != null ? r.getAddress() : null,
                r != null ? r.getMobile() : null,
                r != null ? r.getLatitude() : null,
                r != null ? r.getLongitude() : null
        );
    }

    private RouteVisitStatus resolveVisitStatus(StaffRouteStopVisit visit) {
        if (visit == null) {
            return RouteVisitStatus.PENDING;
        }
        if (visit.getStatus() == RouteVisitStatus.DONE || visit.getCheckOutTime() != null) {
            return RouteVisitStatus.DONE;
        }
        return RouteVisitStatus.CHECKED_IN;
    }

    private RoutePlanItem getPublishedPlanItemForStaff(UUID staffId, UUID itemId, LocalDate date) {
        LocalDate weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        RoutePlan plan = routePlanRepository.findByStaffAndWeekAndStatus(staffId, weekStart, RoutePlanStatus.PUBLISHED)
                .orElseThrow(() -> new BadRequestException("No published route is assigned for this week"));

        return routePlanItemRepository.findByIdAndRoutePlanId(itemId, plan.getId())
                .orElseThrow(() -> new BadRequestException("This stop does not belong to your assigned route"));
    }

    private void ensureStaffHasActiveAttendance(UUID staffId, LocalDate date) {
        staffAttendanceRepository.findTopByStaffIdAndAttendanceDateAndStatusAndCheckOutTimeIsNullOrderByCheckInTimeDesc(
                        staffId,
                        date,
                        AttendanceStatus.CHECKED_IN
                )
                .orElseThrow(() -> new BadRequestException("You must check in attendance before route stop check-in"));
    }

    private Coordinates resolveCounterCoordinates(RoutePlanItem item) {
        if (item.getCounterType() == RouteCounterType.DISTRIBUTOR) {
            Distributor d = distributorRepository.findById(item.getCounterId())
                    .orElseThrow(() -> new ResourceNotFoundException("Distributor not found"));
            return requireCoordinates(d.getLatitude(), d.getLongitude(), d.getName());
        }

        if (item.getCounterType() == RouteCounterType.CUSTOMER) {
            CustomerCounter c = customerCounterRepository.findById(item.getCounterId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer counter not found"));
            return requireCoordinates(c.getLatitude(), c.getLongitude(), c.getName());
        }

        Retailer r = retailerRepository.findById(item.getCounterId())
                .orElseThrow(() -> new ResourceNotFoundException("Retailer not found"));
        return requireCoordinates(r.getLatitude(), r.getLongitude(), r.getName());
    }

    private Coordinates requireCoordinates(Double latitude, Double longitude, String counterName) {
        if (latitude == null || longitude == null) {
            throw new BadRequestException("Coordinates are not set for counter: " + counterName + ". Please update it in customer network");
        }
        return new Coordinates(latitude, longitude);
    }

    private double calculateDistanceMeters(double lat1, double lon1, double lat2, double lon2) {
        double earthRadiusMeters = 6371000.0;
        double latRad1 = Math.toRadians(lat1);
        double latRad2 = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(latRad1) * Math.cos(latRad2)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusMeters * c;
    }

    private StaffRouteStopVisitResponse toVisitResponse(StaffRouteStopVisit visit) {
        Long durationSeconds = visit.getCheckOutTime() == null
                ? null
                : Duration.between(visit.getCheckInTime(), visit.getCheckOutTime()).getSeconds();

        boolean checkInMatched = visit.getCheckInDistanceMeters() != null
                && visit.getCheckInDistanceMeters() <= stopRadiusMeters;

        Double checkOutDistanceMeters = null;
        boolean checkOutMatched = false;
        if (visit.getCheckOutLatitude() != null && visit.getCheckOutLongitude() != null) {
            RoutePlanItem item = routePlanItemRepository.findById(visit.getRoutePlanItemId())
                    .orElse(null);
            if (item != null) {
                Coordinates target = resolveCounterCoordinates(item);
                checkOutDistanceMeters = calculateDistanceMeters(
                        visit.getCheckOutLatitude(),
                        visit.getCheckOutLongitude(),
                        target.latitude(),
                        target.longitude()
                );
                checkOutMatched = checkOutDistanceMeters <= stopRadiusMeters;
            }
        }

        boolean verified = checkInMatched && (visit.getCheckOutTime() == null || checkOutMatched);

        return StaffRouteStopVisitResponse.builder()
                .itemId(visit.getRoutePlanItemId())
                .status(visit.getStatus())
                .checkInTime(visit.getCheckInTime())
                .checkOutTime(visit.getCheckOutTime())
                .checkInLatitude(visit.getCheckInLatitude())
                .checkInLongitude(visit.getCheckInLongitude())
                .checkInLocationName(visit.getCheckInLocationName())
                .checkInGooglePlaceId(visit.getCheckInGooglePlaceId())
                .checkOutLatitude(visit.getCheckOutLatitude())
                .checkOutLongitude(visit.getCheckOutLongitude())
                .checkOutLocationName(visit.getCheckOutLocationName())
                .checkOutGooglePlaceId(visit.getCheckOutGooglePlaceId())
                .visitDurationSeconds(durationSeconds)
                .checkInDistanceMeters(visit.getCheckInDistanceMeters())
                .checkOutDistanceMeters(checkOutDistanceMeters)
                .coordinateVerified(verified)
                .build();
    }

    private void validateStopDistance(double distanceMeters, String action) {
        if (distanceMeters > stopRadiusMeters) {
            throw new BadRequestException(
                    "You are too far from this counter for " + action + ". Distance is "
                            + Math.round(distanceMeters)
                            + " meters. Allowed radius is "
                            + Math.round(stopRadiusMeters)
                            + " meters"
            );
        }
    }

    private String trackingKey(UUID staffId, UUID itemId) {
        return staffId + "::" + itemId;
    }

    private record Coordinates(Double latitude, Double longitude) {
    }

    private record CounterSnapshot(
            String category,
            String counterName,
            String zone,
            String address,
            String phone,
            Double latitude,
            Double longitude
    ) {
    }
}
