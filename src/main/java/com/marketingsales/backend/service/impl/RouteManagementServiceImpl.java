package com.marketingsales.backend.service.impl;

import com.marketingsales.backend.constant.CustomerStatus;
import com.marketingsales.backend.constant.Role;
import com.marketingsales.backend.constant.RouteCounterType;
import com.marketingsales.backend.constant.RoutePlanStatus;
import com.marketingsales.backend.dto.request.AddRoutePlanCounterRequest;
import com.marketingsales.backend.dto.request.AssignRoutePlanStaffRequest;
import com.marketingsales.backend.dto.request.RoutePlanOrderRequest;
import com.marketingsales.backend.dto.request.UpsertRouteRequest;
import com.marketingsales.backend.dto.response.AvailableCounterResponse;
import com.marketingsales.backend.dto.response.RoutePageResponse;
import com.marketingsales.backend.dto.response.RoutePlanItemResponse;
import com.marketingsales.backend.dto.response.RoutePlanResponse;
import com.marketingsales.backend.dto.response.RouteResponse;
import com.marketingsales.backend.dto.response.RouteRowResponse;
import com.marketingsales.backend.dto.response.StaffCounterResponse;
import com.marketingsales.backend.dto.response.StaffOptionResponse;
import com.marketingsales.backend.entity.CustomerCounter;
import com.marketingsales.backend.entity.Distributor;
import com.marketingsales.backend.entity.Retailer;
import com.marketingsales.backend.entity.Route;
import com.marketingsales.backend.entity.RoutePlan;
import com.marketingsales.backend.entity.RoutePlanItem;
import com.marketingsales.backend.entity.User;
import com.marketingsales.backend.exception.BadRequestException;
import com.marketingsales.backend.exception.DuplicateResourceException;
import com.marketingsales.backend.exception.ResourceNotFoundException;
import com.marketingsales.backend.repository.CustomerCounterRepository;
import com.marketingsales.backend.repository.DistributorRepository;
import com.marketingsales.backend.repository.RetailerRepository;
import com.marketingsales.backend.repository.RoutePlanItemRepository;
import com.marketingsales.backend.repository.RoutePlanRepository;
import com.marketingsales.backend.repository.RouteRepository;
import com.marketingsales.backend.repository.UserRepository;
import com.marketingsales.backend.repository.projection.RoutePlanItemCountProjection;
import com.marketingsales.backend.service.RouteManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RouteManagementServiceImpl implements RouteManagementService {

    private static final Map<String, String> ROUTE_SORT_MAP = Map.of(
            "name", "name",
            "zone", "zone",
            "active", "active",
            "createdAt", "createdAt"
    );

    private final RouteRepository routeRepository;
    private final RoutePlanRepository routePlanRepository;
    private final RoutePlanItemRepository routePlanItemRepository;
    private final DistributorRepository distributorRepository;
    private final CustomerCounterRepository customerCounterRepository;
    private final RetailerRepository retailerRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public RoutePageResponse listRoutes(
            LocalDate weekStart,
            String search,
            String zone,
            RoutePlanStatus status,
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {
        LocalDate resolvedWeek = normalizeListWeek(weekStart);
        PageRequest pageable = PageRequest.of(page, size, buildSort(sortBy, sortDir));
        Page<Route> routePage = routeRepository.searchRoutes(buildLikePattern(search), buildLikePattern(zone), null, pageable);

        List<UUID> routeIds = routePage.getContent().stream().map(Route::getId).toList();
        List<RoutePlan> plans = routeIds.isEmpty() ? List.of() : routePlanRepository.findAllByRouteIdsAndWeekStart(routeIds, resolvedWeek);
        Map<UUID, RoutePlan> planByRouteId = plans.stream().collect(Collectors.toMap(RoutePlan::getRouteId, Function.identity()));

        List<UUID> planIds = plans.stream().map(RoutePlan::getId).toList();
        Map<UUID, Long> countByPlanId = planIds.isEmpty()
                ? Map.of()
                : routePlanItemRepository.countByRoutePlanIds(planIds)
                .stream()
                .collect(Collectors.toMap(RoutePlanItemCountProjection::getRoutePlanId, RoutePlanItemCountProjection::getCounterCount));

        List<RouteRowResponse> rows = routePage.getContent().stream()
                .map(route -> buildRouteRow(route, resolvedWeek, planByRouteId.get(route.getId()), countByPlanId))
                .filter(row -> status == null || row.getPlanStatus() == status)
                .toList();

        return RoutePageResponse.builder()
                .content(rows)
                .page(routePage.getNumber())
                .size(routePage.getSize())
                .totalElements(routePage.getTotalElements())
                .totalPages(routePage.getTotalPages())
                .build();
    }

    @Override
    @Transactional
    public RouteResponse createRoute(UpsertRouteRequest request) {
        String name = request.getName().trim();
        String zone = request.getZone().trim();
        if (routeRepository.existsByNameIgnoreCaseAndZoneIgnoreCase(name, zone)) {
            throw new DuplicateResourceException("A route with this name already exists in the selected zone");
        }

        Route route = Route.builder()
                .name(name)
                .zone(zone)
                .active(request.getActive())
                .build();
        return RouteResponse.from(routeRepository.save(route));
    }

    @Override
    @Transactional
    public RouteResponse updateRoute(UUID routeId, UpsertRouteRequest request) {
        Route route = getRoute(routeId);

        String name = request.getName().trim();
        String zone = request.getZone().trim();
        if (routeRepository.existsByNameIgnoreCaseAndZoneIgnoreCaseAndIdNot(name, zone, routeId)) {
            throw new DuplicateResourceException("A route with this name already exists in the selected zone");
        }

        route.setName(name);
        route.setZone(zone);
        route.setActive(request.getActive());
        return RouteResponse.from(routeRepository.saveAndFlush(route));
    }

    @Override
    @Transactional
    public void deleteRoute(UUID routeId) {
        Route route = getRoute(routeId);
        routeRepository.delete(route);
    }

    @Override
    @Transactional(readOnly = true)
    public RoutePlanResponse getRoutePlan(UUID routeId, LocalDate weekStart) {
        Route route = getRoute(routeId);
        LocalDate validatedWeek = requireMonday(weekStart, "weekStart");
        RoutePlan routePlan = routePlanRepository.findByRouteIdAndWeekStart(routeId, validatedWeek)
                .orElseGet(() -> RoutePlan.builder()
                        .routeId(routeId)
                        .route(route)
                        .weekStart(validatedWeek)
                        .status(RoutePlanStatus.DRAFT)
                        .version(0L)
                        .build());
        return toRoutePlanResponse(routePlan);
    }

    @Override
    @Transactional
    public RoutePlanResponse assignStaff(UUID routeId, LocalDate weekStart, AssignRoutePlanStaffRequest request) {
        Route route = getRoute(routeId);
        LocalDate validatedWeek = requireEditableWeek(weekStart);
        User staff = getStaffUser(request.getStaffId());

        RoutePlan plan = getOrCreatePlan(route, validatedWeek);
        ensureStaffNotDoubleBooked(plan, staff.getId());

        plan.setStaffId(staff.getId());
        plan.setStaff(staff);
        markPlanDraftIfPublished(plan);
        routePlanRepository.saveAndFlush(plan);

        // Always sync the plan with the staff's assigned network from the customer network page
        routePlanItemRepository.deleteByRoutePlanId(plan.getId());
        autoLoadStaffCounters(route, plan, staff.getId());
        return toRoutePlanResponse(plan);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffOptionResponse> getAssignableStaff(LocalDate weekStart, UUID routeId) {
        LocalDate validatedWeek = requireMonday(weekStart, "weekStart");
        getRoute(routeId);

        Map<UUID, RoutePlan> planByStaffId = routePlanRepository.findAllByWeekStart(validatedWeek).stream()
                .filter(p -> p.getStaffId() != null)
                .collect(Collectors.toMap(RoutePlan::getStaffId, Function.identity(), (left, right) -> left));
        Map<UUID, Route> routeById = routeRepository.findAll().stream()
                .collect(Collectors.toMap(Route::getId, Function.identity()));

        List<StaffOptionResponse> options = new ArrayList<>();
        for (User staff : userRepository.findAllByRoleAndEnabledTrueOrderByFullNameAsc(Role.STAFF)) {
            RoutePlan planned = planByStaffId.get(staff.getId());
            UUID plannedRouteId = null;
            String plannedRouteName = null;
            if (planned != null && !planned.getRouteId().equals(routeId)) {
                plannedRouteId = planned.getRouteId();
                Route plannedRoute = routeById.get(plannedRouteId);
                plannedRouteName = plannedRoute != null ? plannedRoute.getName() : null;
            }

            long distributorCount = distributorRepository.findAllByAssignedStaffIdAndStatusOrderByNameAsc(staff.getId(), CustomerStatus.ACTIVE).size();
            long customerCount = customerCounterRepository.findAllByDistributorAssignedStaffIdAndStatusOrderByNameAsc(staff.getId(), CustomerStatus.ACTIVE).size();
            long retailerCount = retailerRepository.findAllByAssignedStaffIdAndStatusOrderByNameAsc(staff.getId(), CustomerStatus.ACTIVE).size();

            options.add(StaffOptionResponse.builder()
                    .staffId(staff.getId())
                    .fullName(staff.getFullName())
                    .email(staff.getEmail())
                    .region(staff.getRegion())
                    .assignedDistributorCount(distributorCount)
                    .assignedCustomerCount(customerCount)
                    .assignedRetailerCount(retailerCount)
                    .plannedRouteId(plannedRouteId)
                    .plannedRouteName(plannedRouteName)
                    .build());
        }
        return options;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffCounterResponse> getStaffCounters(UUID staffId, LocalDate weekStart) {
        LocalDate validatedWeek = requireMonday(weekStart, "weekStart");
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff user not found"));
        if (staff.getRole() != Role.STAFF) {
            throw new BadRequestException("User must have STAFF role");
        }

        List<RoutePlanItem> weekItems = routePlanItemRepository.findAllByWeekStart(validatedWeek);
        Map<String, RoutePlanItem> occupiedByKey = weekItems.stream()
                .collect(Collectors.toMap(this::keyOf, Function.identity(), (left, right) -> left));
        Map<UUID, RoutePlan> planById = routePlanRepository.findAllByWeekStart(validatedWeek).stream()
                .collect(Collectors.toMap(RoutePlan::getId, Function.identity()));
        Map<UUID, Route> routeById = routeRepository.findAll().stream()
                .collect(Collectors.toMap(Route::getId, Function.identity()));

        List<StaffCounterResponse> responses = new ArrayList<>();

        for (Distributor distributor : distributorRepository.findAllByAssignedStaffIdAndStatusOrderByNameAsc(staffId, CustomerStatus.ACTIVE)) {
            responses.add(buildStaffCounterResponse(
                    RouteCounterType.DISTRIBUTOR, distributor.getId(), distributor.getCode(), distributor.getName(),
                    distributor.getZone(), distributor.getRoute(), occupiedByKey, planById, routeById));
        }
        for (CustomerCounter counter : customerCounterRepository.findAllByDistributorAssignedStaffIdAndStatusOrderByNameAsc(staffId, CustomerStatus.ACTIVE)) {
            responses.add(buildStaffCounterResponse(
                    RouteCounterType.CUSTOMER, counter.getId(), counter.getCode(), counter.getName(),
                    counter.getDistributor().getZone(), counter.getDistributor().getRoute(), occupiedByKey, planById, routeById));
        }
        for (Retailer retailer : retailerRepository.findAllByAssignedStaffIdAndStatusOrderByNameAsc(staffId, CustomerStatus.ACTIVE)) {
            responses.add(buildStaffCounterResponse(
                    RouteCounterType.RETAILER, retailer.getId(), retailer.getCode(), retailer.getName(),
                    retailer.getZone(), retailer.getRoute(), occupiedByKey, planById, routeById));
        }

        responses.sort(Comparator.comparing(StaffCounterResponse::getName, String.CASE_INSENSITIVE_ORDER));
        return responses;
    }

    private StaffCounterResponse buildStaffCounterResponse(
            RouteCounterType type,
            UUID counterId,
            String code,
            String name,
            String zone,
            String route,
            Map<String, RoutePlanItem> occupiedByKey,
            Map<UUID, RoutePlan> planById,
            Map<UUID, Route> routeById
    ) {
        UUID plannedRouteId = null;
        String plannedRouteName = null;
        RoutePlanItem occupiedItem = occupiedByKey.get(keyOf(type, counterId));
        if (occupiedItem != null) {
            RoutePlan occupiedPlan = planById.get(occupiedItem.getRoutePlanId());
            if (occupiedPlan != null) {
                plannedRouteId = occupiedPlan.getRouteId();
                Route r = routeById.get(plannedRouteId);
                plannedRouteName = r != null ? r.getName() : null;
            }
        }
        return StaffCounterResponse.builder()
                .counterType(type)
                .counterId(counterId)
                .code(code)
                .name(name)
                .zone(zone)
                .route(route)
                .plannedRouteId(plannedRouteId)
                .plannedRouteName(plannedRouteName)
                .build();
    }

    @Override
    @Transactional
    public RoutePlanResponse reorderPlanItems(UUID routeId, LocalDate weekStart, RoutePlanOrderRequest request) {
        RoutePlan plan = getExistingPlan(routeId, requireEditableWeek(weekStart));
        markPlanDraftIfPublished(plan);

        List<RoutePlanItem> currentItems = routePlanItemRepository.findAllByRoutePlanIdOrderByVisitOrderAsc(plan.getId());
        if (currentItems.isEmpty()) {
            throw new BadRequestException("Cannot reorder an empty route plan");
        }

        List<UUID> orderedIds = request.getItemIds();
        Set<UUID> currentIds = currentItems.stream().map(RoutePlanItem::getId).collect(Collectors.toSet());
        if (orderedIds.size() != currentItems.size() || !currentIds.equals(new HashSet<>(orderedIds))) {
            throw new BadRequestException("itemIds must contain each plan item exactly once");
        }

        Map<UUID, RoutePlanItem> itemById = currentItems.stream().collect(Collectors.toMap(RoutePlanItem::getId, Function.identity()));
        for (int i = 0; i < orderedIds.size(); i++) {
            itemById.get(orderedIds.get(i)).setVisitOrder(i + 1);
        }

        applyVisitOrderSafely(currentItems);
        routePlanRepository.saveAndFlush(plan);
        return toRoutePlanResponse(plan);
    }

    @Override
    @Transactional
    public RoutePlanResponse addCounter(UUID routeId, LocalDate weekStart, AddRoutePlanCounterRequest request) {
        Route route = getRoute(routeId);
        LocalDate validatedWeek = requireEditableWeek(weekStart);
        RoutePlan targetPlan = getOrCreatePlan(route, validatedWeek);
        markPlanDraftIfPublished(targetPlan);

        RoutePlanItem existing = routePlanItemRepository
                .findByWeekStartAndCounterTypeAndCounterId(validatedWeek, request.getCounterType(), request.getCounterId())
                .orElse(null);

        if (existing != null && !existing.getRoutePlanId().equals(targetPlan.getId())) {
            RoutePlan sourcePlan = routePlanRepository.findById(existing.getRoutePlanId())
                    .orElseThrow(() -> new ResourceNotFoundException("Source route plan not found"));
            markPlanDraftIfPublished(sourcePlan);

            existing.setRoutePlanId(targetPlan.getId());
            existing.setRoutePlan(targetPlan);
            existing.setVisitOrder(nextVisitOrder(targetPlan.getId()));
            routePlanItemRepository.save(existing);

            renumberPlan(sourcePlan.getId());
            renumberPlan(targetPlan.getId());
            routePlanRepository.saveAndFlush(sourcePlan);
        } else if (existing == null) {
            RoutePlanItem item = RoutePlanItem.builder()
                    .routePlanId(targetPlan.getId())
                    .routePlan(targetPlan)
                    .weekStart(validatedWeek)
                    .counterType(request.getCounterType())
                    .counterId(request.getCounterId())
                    .visitOrder(nextVisitOrder(targetPlan.getId()))
                    .build();
            routePlanItemRepository.save(item);
        }

        routePlanRepository.saveAndFlush(targetPlan);
        return toRoutePlanResponse(targetPlan);
    }

    @Override
    @Transactional
    public void removeCounter(UUID routeId, UUID itemId) {
        RoutePlanItem item = routePlanItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Route plan item not found"));

        RoutePlan plan = routePlanRepository.findById(item.getRoutePlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Route plan not found"));

        if (!plan.getRouteId().equals(routeId)) {
            throw new BadRequestException("Item does not belong to this route");
        }

        requireEditableWeek(plan.getWeekStart());
        markPlanDraftIfPublished(plan);

        routePlanItemRepository.delete(item);
        renumberPlan(plan.getId());
        routePlanRepository.saveAndFlush(plan);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvailableCounterResponse> getAvailableCounters(LocalDate weekStart, String search, UUID excludeRouteId) {
        LocalDate validatedWeek = requireMonday(weekStart, "weekStart");
        String searchPattern = buildLikePattern(search);

        RoutePlan excludedPlan = null;
        if (excludeRouteId != null) {
            excludedPlan = routePlanRepository.findByRouteIdAndWeekStart(excludeRouteId, validatedWeek).orElse(null);
        }

        Set<String> excludedKeys = new HashSet<>();
        if (excludedPlan != null) {
            List<RoutePlanItem> excludedItems = routePlanItemRepository.findAllByRoutePlanIdOrderByVisitOrderAsc(excludedPlan.getId());
            excludedKeys = excludedItems.stream().map(this::keyOf).collect(Collectors.toSet());
        }

        List<RoutePlanItem> weekItems = routePlanItemRepository.findAllByWeekStart(validatedWeek);
        Map<String, RoutePlanItem> occupiedByKey = weekItems.stream()
                .collect(Collectors.toMap(this::keyOf, Function.identity(), (left, right) -> left));

        Map<UUID, RoutePlan> planById = routePlanRepository.findAllByWeekStart(validatedWeek).stream()
                .collect(Collectors.toMap(RoutePlan::getId, Function.identity()));
        Map<UUID, Route> routeById = routeRepository.findAll().stream()
                .collect(Collectors.toMap(Route::getId, Function.identity()));

        List<AvailableCounterResponse> responses = new ArrayList<>();
        appendAvailableDistributors(searchPattern, excludedKeys, occupiedByKey, planById, routeById, responses);
        appendAvailableCustomers(searchPattern, excludedKeys, occupiedByKey, planById, routeById, responses);
        appendAvailableRetailers(searchPattern, excludedKeys, occupiedByKey, planById, routeById, responses);

        responses.sort(Comparator.comparing(AvailableCounterResponse::getName, String.CASE_INSENSITIVE_ORDER));
        return responses;
    }

    @Override
    @Transactional
    public RoutePlanResponse publish(UUID routeId, LocalDate weekStart) {
        RoutePlan plan = getExistingPlan(routeId, requireEditableWeek(weekStart));
        if (plan.getStaffId() == null) {
            throw new BadRequestException("Cannot publish without assigning a staff member");
        }

        long itemCount = routePlanItemRepository.countByRoutePlanId(plan.getId());
        if (itemCount == 0) {
            throw new BadRequestException("Cannot publish an empty route plan");
        }

        ensureStaffNotDoubleBooked(plan, plan.getStaffId());

        plan.setStatus(RoutePlanStatus.PUBLISHED);
        plan.setPublishedAt(Instant.now());
        routePlanRepository.saveAndFlush(plan);
        return toRoutePlanResponse(plan);
    }

    @Override
    @Transactional
    public RoutePlanResponse copyPlan(UUID routeId, LocalDate weekStart, LocalDate sourceWeekStart) {
        LocalDate targetWeek = requireEditableWeek(weekStart);
        LocalDate sourceWeek = requireMonday(sourceWeekStart, "sourceWeekStart");

        Route route = getRoute(routeId);
        RoutePlan sourcePlan = routePlanRepository.findByRouteIdAndWeekStart(routeId, sourceWeek)
                .orElseThrow(() -> new ResourceNotFoundException("Source route plan not found"));

        RoutePlan targetPlan = getOrCreatePlan(route, targetWeek);
        markPlanDraftIfPublished(targetPlan);

        routePlanItemRepository.deleteByRoutePlanId(targetPlan.getId());
        List<RoutePlanItem> sourceItems = routePlanItemRepository.findAllByRoutePlanIdOrderByVisitOrderAsc(sourcePlan.getId());

        int order = 1;
        for (RoutePlanItem sourceItem : sourceItems) {
            if (routePlanItemRepository.findByWeekStartAndCounterTypeAndCounterId(targetWeek, sourceItem.getCounterType(), sourceItem.getCounterId()).isPresent()) {
                continue;
            }
            RoutePlanItem targetItem = RoutePlanItem.builder()
                    .routePlanId(targetPlan.getId())
                    .routePlan(targetPlan)
                    .weekStart(targetWeek)
                    .counterType(sourceItem.getCounterType())
                    .counterId(sourceItem.getCounterId())
                    .visitOrder(order++)
                    .build();
            routePlanItemRepository.save(targetItem);
        }

        routePlanRepository.saveAndFlush(targetPlan);
        return toRoutePlanResponse(targetPlan);
    }

    private void appendAvailableDistributors(
            String searchPattern,
            Set<String> excludedKeys,
            Map<String, RoutePlanItem> occupiedByKey,
            Map<UUID, RoutePlan> planById,
            Map<UUID, Route> routeById,
            List<AvailableCounterResponse> responses
    ) {
        for (Distributor distributor : distributorRepository.searchActiveDistributors(CustomerStatus.ACTIVE, searchPattern)) {
            String key = keyOf(RouteCounterType.DISTRIBUTOR, distributor.getId());
            if (excludedKeys.contains(key)) {
                continue;
            }
            responses.add(buildAvailableResponse(RouteCounterType.DISTRIBUTOR, distributor.getId(), distributor.getName(), distributor.getZone(), occupiedByKey.get(key), planById, routeById));
        }
    }

    private void appendAvailableCustomers(
            String searchPattern,
            Set<String> excludedKeys,
            Map<String, RoutePlanItem> occupiedByKey,
            Map<UUID, RoutePlan> planById,
            Map<UUID, Route> routeById,
            List<AvailableCounterResponse> responses
    ) {
        for (CustomerCounter counter : customerCounterRepository.searchActiveCustomerCounters(CustomerStatus.ACTIVE, searchPattern)) {
            String key = keyOf(RouteCounterType.CUSTOMER, counter.getId());
            if (excludedKeys.contains(key)) {
                continue;
            }
            responses.add(buildAvailableResponse(RouteCounterType.CUSTOMER, counter.getId(), counter.getName(), counter.getDistributor().getZone(), occupiedByKey.get(key), planById, routeById));
        }
    }

    private void appendAvailableRetailers(
            String searchPattern,
            Set<String> excludedKeys,
            Map<String, RoutePlanItem> occupiedByKey,
            Map<UUID, RoutePlan> planById,
            Map<UUID, Route> routeById,
            List<AvailableCounterResponse> responses
    ) {
        for (Retailer retailer : retailerRepository.searchActiveRetailers(CustomerStatus.ACTIVE, searchPattern)) {
            String key = keyOf(RouteCounterType.RETAILER, retailer.getId());
            if (excludedKeys.contains(key)) {
                continue;
            }
            responses.add(buildAvailableResponse(RouteCounterType.RETAILER, retailer.getId(), retailer.getName(), retailer.getZone(), occupiedByKey.get(key), planById, routeById));
        }
    }

    private AvailableCounterResponse buildAvailableResponse(
            RouteCounterType type,
            UUID counterId,
            String name,
            String zone,
            RoutePlanItem occupiedItem,
            Map<UUID, RoutePlan> planById,
            Map<UUID, Route> routeById
    ) {
        UUID plannedRouteId = null;
        String plannedRouteName = null;
        if (occupiedItem != null) {
            RoutePlan occupiedPlan = planById.get(occupiedItem.getRoutePlanId());
            if (occupiedPlan != null) {
                plannedRouteId = occupiedPlan.getRouteId();
                Route route = routeById.get(plannedRouteId);
                plannedRouteName = route != null ? route.getName() : null;
            }
        }

        return AvailableCounterResponse.builder()
                .counterType(type)
                .counterId(counterId)
                .name(name)
                .zone(zone)
                .plannedRouteId(plannedRouteId)
                .plannedRouteName(plannedRouteName)
                .build();
    }

    private void autoLoadMasterCounters(Route route, RoutePlan plan) {
        LocalDate weekStart = plan.getWeekStart();
        Set<String> occupiedKeys = routePlanItemRepository.findAllByWeekStart(weekStart)
                .stream()
                .map(this::keyOf)
                .collect(Collectors.toSet());

        List<AutoLoadCandidate> candidates = new ArrayList<>();

        for (Distributor distributor : distributorRepository.findAllByMasterRouteIdAndStatusOrderByNameAsc(route.getId(), CustomerStatus.ACTIVE)) {
            String key = keyOf(RouteCounterType.DISTRIBUTOR, distributor.getId());
            if (!occupiedKeys.contains(key)) {
                candidates.add(new AutoLoadCandidate(RouteCounterType.DISTRIBUTOR, distributor.getId(), distributor.getName()));
            }
        }

        for (CustomerCounter counter : customerCounterRepository.findAllByMasterRouteIdAndStatusOrderByNameAsc(route.getId(), CustomerStatus.ACTIVE)) {
            String key = keyOf(RouteCounterType.CUSTOMER, counter.getId());
            if (!occupiedKeys.contains(key)) {
                candidates.add(new AutoLoadCandidate(RouteCounterType.CUSTOMER, counter.getId(), counter.getName()));
            }
        }

        for (Retailer retailer : retailerRepository.findAllByMasterRouteIdAndStatusOrderByNameAsc(route.getId(), CustomerStatus.ACTIVE)) {
            String key = keyOf(RouteCounterType.RETAILER, retailer.getId());
            if (!occupiedKeys.contains(key)) {
                candidates.add(new AutoLoadCandidate(RouteCounterType.RETAILER, retailer.getId(), retailer.getName()));
            }
        }

        candidates.sort(
                Comparator.comparing((AutoLoadCandidate candidate) -> candidate.counterType().ordinal())
                        .thenComparing(AutoLoadCandidate::name, String.CASE_INSENSITIVE_ORDER)
        );

        int visitOrder = 1;
        for (AutoLoadCandidate candidate : candidates) {
            RoutePlanItem item = RoutePlanItem.builder()
                    .routePlanId(plan.getId())
                    .routePlan(plan)
                    .weekStart(weekStart)
                    .counterType(candidate.counterType())
                    .counterId(candidate.counterId())
                    .visitOrder(visitOrder++)
                    .build();
            routePlanItemRepository.save(item);
        }
    }

    private void autoLoadStaffCounters(Route route, RoutePlan plan, UUID staffId) {
        LocalDate weekStart = plan.getWeekStart();
        Set<String> occupiedKeys = routePlanItemRepository.findAllByWeekStart(weekStart)
                .stream()
                .map(this::keyOf)
                .collect(Collectors.toSet());

        List<AutoLoadCandidate> candidates = new ArrayList<>();

        for (Distributor distributor : distributorRepository.findAllByAssignedStaffIdAndStatusOrderByNameAsc(staffId, CustomerStatus.ACTIVE)) {
            String key = keyOf(RouteCounterType.DISTRIBUTOR, distributor.getId());
            if (!occupiedKeys.contains(key)) {
                candidates.add(new AutoLoadCandidate(RouteCounterType.DISTRIBUTOR, distributor.getId(), distributor.getName()));
            }
        }

        for (CustomerCounter counter : customerCounterRepository.findAllByDistributorAssignedStaffIdAndStatusOrderByNameAsc(staffId, CustomerStatus.ACTIVE)) {
            String key = keyOf(RouteCounterType.CUSTOMER, counter.getId());
            if (!occupiedKeys.contains(key)) {
                candidates.add(new AutoLoadCandidate(RouteCounterType.CUSTOMER, counter.getId(), counter.getName()));
            }
        }

        for (Retailer retailer : retailerRepository.findAllByAssignedStaffIdAndStatusOrderByNameAsc(staffId, CustomerStatus.ACTIVE)) {
            String key = keyOf(RouteCounterType.RETAILER, retailer.getId());
            if (!occupiedKeys.contains(key)) {
                candidates.add(new AutoLoadCandidate(RouteCounterType.RETAILER, retailer.getId(), retailer.getName()));
            }
        }

        candidates.sort(
                Comparator.comparing((AutoLoadCandidate candidate) -> candidate.counterType().ordinal())
                        .thenComparing(AutoLoadCandidate::name, String.CASE_INSENSITIVE_ORDER)
        );

        int visitOrder = 1;
        for (AutoLoadCandidate candidate : candidates) {
            RoutePlanItem item = RoutePlanItem.builder()
                    .routePlanId(plan.getId())
                    .routePlan(plan)
                    .weekStart(weekStart)
                    .counterType(candidate.counterType())
                    .counterId(candidate.counterId())
                    .visitOrder(visitOrder++)
                    .build();
            routePlanItemRepository.save(item);
        }
    }

    private RoutePlanResponse toRoutePlanResponse(RoutePlan plan) {
        Route route = plan.getRoute();
        if (route == null) {
            route = routeRepository.findById(plan.getRouteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Route not found"));
        }

        User staff = plan.getStaff();
        if (staff == null && plan.getStaffId() != null) {
            staff = userRepository.findById(plan.getStaffId()).orElse(null);
        }

        List<RoutePlanItem> items = plan.getId() == null
                ? List.of()
                : routePlanItemRepository.findAllByRoutePlanIdOrderByVisitOrderAsc(plan.getId());

        List<RoutePlanItemResponse> itemResponses = mapPlanItems(items);

        return RoutePlanResponse.builder()
                .routeId(route.getId())
                .routeName(route.getName())
                .zone(route.getZone())
                .weekStart(plan.getWeekStart())
                .staffId(plan.getStaffId())
                .staffName(staff != null ? staff.getFullName() : null)
                .status(plan.getStatus())
                .publishedAt(plan.getPublishedAt())
                .version(plan.getVersion() != null ? plan.getVersion() : 0L)
                .items(itemResponses)
                .build();
    }

    private List<RoutePlanItemResponse> mapPlanItems(List<RoutePlanItem> items) {
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

        return items.stream()
                .map(item -> {
                    String name = null;
                    String zone = null;
                    if (item.getCounterType() == RouteCounterType.DISTRIBUTOR) {
                        Distributor d = distributors.get(item.getCounterId());
                        if (d != null) {
                            name = d.getName();
                            zone = d.getZone();
                        }
                    } else if (item.getCounterType() == RouteCounterType.CUSTOMER) {
                        CustomerCounter c = customers.get(item.getCounterId());
                        if (c != null) {
                            name = c.getName();
                            zone = c.getDistributor().getZone();
                        }
                    } else if (item.getCounterType() == RouteCounterType.RETAILER) {
                        Retailer r = retailers.get(item.getCounterId());
                        if (r != null) {
                            name = r.getName();
                            zone = r.getZone();
                        }
                    }

                    return RoutePlanItemResponse.builder()
                            .itemId(item.getId())
                            .counterType(item.getCounterType())
                            .counterId(item.getCounterId())
                            .visitOrder(item.getVisitOrder())
                            .counterName(name)
                            .zone(zone)
                            .build();
                })
                .toList();
    }

    private RouteRowResponse buildRouteRow(Route route, LocalDate weekStart, RoutePlan plan, Map<UUID, Long> countByPlanId) {
        return RouteRowResponse.builder()
                .routeId(route.getId())
                .name(route.getName())
                .zone(route.getZone())
                .active(route.isActive())
                .weekStart(weekStart)
                .staffId(plan != null ? plan.getStaffId() : null)
                .staffName(plan != null && plan.getStaff() != null ? plan.getStaff().getFullName() : null)
                .planStatus(plan != null ? plan.getStatus() : RoutePlanStatus.DRAFT)
                .counterCount(plan != null ? countByPlanId.getOrDefault(plan.getId(), 0L) : 0L)
                .build();
    }

    private RoutePlan getOrCreatePlan(Route route, LocalDate weekStart) {
        return routePlanRepository.findByRouteIdAndWeekStart(route.getId(), weekStart)
                .orElseGet(() -> {
                    RoutePlan plan = RoutePlan.builder()
                            .routeId(route.getId())
                            .route(route)
                            .weekStart(weekStart)
                            .status(RoutePlanStatus.DRAFT)
                            .build();
                    return routePlanRepository.saveAndFlush(plan);
                });
    }

    private RoutePlan getExistingPlan(UUID routeId, LocalDate weekStart) {
        getRoute(routeId);
        return routePlanRepository.findByRouteIdAndWeekStart(routeId, weekStart)
                .orElseThrow(() -> new ResourceNotFoundException("Route plan not found"));
    }

    private void ensureStaffNotDoubleBooked(RoutePlan plan, UUID staffId) {
        boolean conflict = routePlanRepository.existsByWeekStartAndStaffIdAndRouteIdNot(plan.getWeekStart(), staffId, plan.getRouteId());
        if (conflict) {
            throw new DuplicateResourceException("This staff member is already assigned to another route for the selected week");
        }
    }

    private void markPlanDraftIfPublished(RoutePlan plan) {
        if (plan.getStatus() == RoutePlanStatus.PUBLISHED) {
            plan.setStatus(RoutePlanStatus.DRAFT);
            plan.setPublishedAt(null);
        }
    }

    private int nextVisitOrder(UUID routePlanId) {
        return routePlanItemRepository.findAllByRoutePlanIdOrderByVisitOrderAsc(routePlanId).size() + 1;
    }

    private void renumberPlan(UUID routePlanId) {
        List<RoutePlanItem> items = routePlanItemRepository.findAllByRoutePlanIdOrderByVisitOrderAsc(routePlanId);
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setVisitOrder(i + 1);
        }
        applyVisitOrderSafely(items);
    }

    private void applyVisitOrderSafely(List<RoutePlanItem> items) {
        if (items.isEmpty()) {
            return;
        }

        // Snapshot desired final order before moving values out of the unique range.
        Map<UUID, Integer> desiredOrderById = items.stream()
                .collect(Collectors.toMap(RoutePlanItem::getId, RoutePlanItem::getVisitOrder));

        // Two-phase update prevents temporary duplicates on (route_plan_id, visit_order).
        int offset = items.size() + 1000;
        for (RoutePlanItem item : items) {
            item.setVisitOrder(item.getVisitOrder() + offset);
        }
        routePlanItemRepository.saveAllAndFlush(items);

        for (RoutePlanItem item : items) {
            item.setVisitOrder(desiredOrderById.get(item.getId()));
        }
        routePlanItemRepository.saveAllAndFlush(items);
    }

    private LocalDate requireEditableWeek(LocalDate weekStart) {
        LocalDate validatedWeek = requireMonday(weekStart, "weekStart");
        LocalDate thisWeekMonday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        if (validatedWeek.isBefore(thisWeekMonday)) {
            throw new BadRequestException("Past weeks are locked for editing");
        }
        return validatedWeek;
    }

    private LocalDate normalizeListWeek(LocalDate weekStart) {
        if (weekStart != null) {
            return requireMonday(weekStart, "week");
        }
        LocalDate nowMonday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return nowMonday.plusWeeks(1);
    }

    private LocalDate requireMonday(LocalDate value, String field) {
        if (value == null) {
            throw new BadRequestException(field + " is required");
        }
        if (value.getDayOfWeek() != DayOfWeek.MONDAY) {
            throw new BadRequestException(field + " must be a Monday");
        }
        return value;
    }

    private Route getRoute(UUID routeId) {
        return routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found"));
    }

    private User getStaffUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Assigned staff user not found"));
        if (user.getRole() != Role.STAFF) {
            throw new BadRequestException("Assigned user must have STAFF role");
        }
        return user;
    }

    private Sort buildSort(String sortBy, String sortDir) {
        String sortKey = StringUtils.hasText(sortBy) ? sortBy.trim() : "name";
        String property = ROUTE_SORT_MAP.getOrDefault(sortKey, "name");
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, property);
    }

    private String buildLikePattern(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return "%" + value.trim().toLowerCase() + "%";
    }

    private String keyOf(RoutePlanItem item) {
        return keyOf(item.getCounterType(), item.getCounterId());
    }

    private String keyOf(RouteCounterType type, UUID counterId) {
        return type.name() + "::" + counterId;
    }

    private record AutoLoadCandidate(RouteCounterType counterType, UUID counterId, String name) {
    }
}
