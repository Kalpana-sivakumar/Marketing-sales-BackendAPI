package com.marketingsales.backend.service;

import com.marketingsales.backend.constant.RoutePlanStatus;
import com.marketingsales.backend.dto.request.AddRoutePlanCounterRequest;
import com.marketingsales.backend.dto.request.AssignRoutePlanStaffRequest;
import com.marketingsales.backend.dto.request.RoutePlanOrderRequest;
import com.marketingsales.backend.dto.request.UpsertRouteRequest;
import com.marketingsales.backend.dto.response.AvailableCounterResponse;
import com.marketingsales.backend.dto.response.RoutePageResponse;
import com.marketingsales.backend.dto.response.RoutePlanResponse;
import com.marketingsales.backend.dto.response.RouteResponse;
import com.marketingsales.backend.dto.response.StaffCounterResponse;
import com.marketingsales.backend.dto.response.StaffOptionResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface RouteManagementService {

    RoutePageResponse listRoutes(LocalDate weekStart, String search, String zone, RoutePlanStatus status, int page, int size, String sortBy, String sortDir);

    RouteResponse createRoute(UpsertRouteRequest request);

    RouteResponse updateRoute(UUID routeId, UpsertRouteRequest request);

    void deleteRoute(UUID routeId);

    RoutePlanResponse getRoutePlan(UUID routeId, LocalDate weekStart);

    RoutePlanResponse assignStaff(UUID routeId, LocalDate weekStart, AssignRoutePlanStaffRequest request);

    RoutePlanResponse reorderPlanItems(UUID routeId, LocalDate weekStart, RoutePlanOrderRequest request);

    RoutePlanResponse addCounter(UUID routeId, LocalDate weekStart, AddRoutePlanCounterRequest request);

    void removeCounter(UUID routeId, UUID itemId);

    List<AvailableCounterResponse> getAvailableCounters(LocalDate weekStart, String search, UUID excludeRouteId);

    List<StaffOptionResponse> getAssignableStaff(LocalDate weekStart, UUID routeId);

    List<StaffCounterResponse> getStaffCounters(UUID staffId, LocalDate weekStart);

    RoutePlanResponse publish(UUID routeId, LocalDate weekStart);

    RoutePlanResponse copyPlan(UUID routeId, LocalDate weekStart, LocalDate sourceWeekStart);
}