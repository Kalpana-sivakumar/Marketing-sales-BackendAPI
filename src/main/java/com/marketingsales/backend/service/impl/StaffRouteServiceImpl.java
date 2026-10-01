package com.marketingsales.backend.service.impl;

import com.marketingsales.backend.constant.RouteCounterType;
import com.marketingsales.backend.constant.RoutePlanStatus;
import com.marketingsales.backend.dto.response.StaffRouteResponse;
import com.marketingsales.backend.dto.response.StaffRouteStopResponse;
import com.marketingsales.backend.entity.CustomerCounter;
import com.marketingsales.backend.entity.Distributor;
import com.marketingsales.backend.entity.Retailer;
import com.marketingsales.backend.entity.RoutePlan;
import com.marketingsales.backend.entity.RoutePlanItem;
import com.marketingsales.backend.exception.ResourceNotFoundException;
import com.marketingsales.backend.repository.CustomerCounterRepository;
import com.marketingsales.backend.repository.DistributorRepository;
import com.marketingsales.backend.repository.RetailerRepository;
import com.marketingsales.backend.repository.RoutePlanItemRepository;
import com.marketingsales.backend.repository.RoutePlanRepository;
import com.marketingsales.backend.repository.RouteRepository;
import com.marketingsales.backend.service.StaffRouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
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

    private final RoutePlanRepository routePlanRepository;
    private final RoutePlanItemRepository routePlanItemRepository;
    private final RouteRepository routeRepository;
    private final DistributorRepository distributorRepository;
    private final CustomerCounterRepository customerCounterRepository;
    private final RetailerRepository retailerRepository;

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
            if (item.getCounterType() == RouteCounterType.DISTRIBUTOR) {
                Distributor d = distributors.get(item.getCounterId());
                return StaffRouteStopResponse.builder()
                        .itemId(item.getId())
                        .visitOrder(item.getVisitOrder())
                        .counterType(item.getCounterType())
                        .counterId(item.getCounterId())
                        .counterName(d != null ? d.getName() : null)
                        .address(d != null ? d.getAddress() : null)
                        .phone(d != null ? d.getMobile() : null)
                        .latitude(null)
                        .longitude(null)
                        .outstandingAmount(d != null ? d.getOutstandingAmount() : null)
                        .build();
            }

            if (item.getCounterType() == RouteCounterType.CUSTOMER) {
                CustomerCounter c = customers.get(item.getCounterId());
                return StaffRouteStopResponse.builder()
                        .itemId(item.getId())
                        .visitOrder(item.getVisitOrder())
                        .counterType(item.getCounterType())
                        .counterId(item.getCounterId())
                        .counterName(c != null ? c.getName() : null)
                        .address(null)
                        .phone(c != null ? c.getMobile() : null)
                        .latitude(null)
                        .longitude(null)
                        .outstandingAmount(c != null ? c.getOutstandingAmount() : null)
                        .build();
            }

            Retailer r = retailers.get(item.getCounterId());
            return StaffRouteStopResponse.builder()
                    .itemId(item.getId())
                    .visitOrder(item.getVisitOrder())
                    .counterType(item.getCounterType())
                    .counterId(item.getCounterId())
                    .counterName(r != null ? r.getName() : null)
                    .address(null)
                    .phone(r != null ? r.getMobile() : null)
                    .latitude(null)
                    .longitude(null)
                    .outstandingAmount(r != null ? r.getOutstandingAmount() : null)
                    .build();
        }).toList();

        com.marketingsales.backend.entity.Route route = routeRepository.findById(plan.getRouteId())
                .orElseThrow(() -> new ResourceNotFoundException("Route not found"));

        return Optional.of(StaffRouteResponse.builder()
                .routeId(route.getId())
                .routeName(route.getName())
                .zone(route.getZone())
                .weekStart(plan.getWeekStart())
                .stops(stops)
                .build());
    }
}
