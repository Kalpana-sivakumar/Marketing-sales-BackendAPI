package com.marketingsales.backend.controller;

import com.marketingsales.backend.constant.RoutePlanStatus;
import com.marketingsales.backend.dto.request.AddRoutePlanCounterRequest;
import com.marketingsales.backend.dto.request.AssignRoutePlanStaffRequest;
import com.marketingsales.backend.dto.request.RoutePlanOrderRequest;
import com.marketingsales.backend.dto.request.UpsertRouteRequest;
import com.marketingsales.backend.dto.response.ApiResponse;
import com.marketingsales.backend.dto.response.AvailableCounterResponse;
import com.marketingsales.backend.dto.response.RoutePageResponse;
import com.marketingsales.backend.dto.response.RoutePlanResponse;
import com.marketingsales.backend.dto.response.RouteResponse;
import com.marketingsales.backend.service.RouteManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasAnyRole('ADMIN','MARKETING_MANAGER')")
@Tag(name = "Route Management", description = "Route and weekly route planning APIs for managers")
public class RouteManagementController {

    private final RouteManagementService routeManagementService;

    @Operation(summary = "List routes for a week")
    @GetMapping
    public ApiResponse<RoutePageResponse> listRoutes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate week,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String zone,
            @RequestParam(required = false) RoutePlanStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        return ApiResponse.success(routeManagementService.listRoutes(week, search, zone, status, page, size, sortBy, sortDir));
    }

    @Operation(summary = "Create route")
    @PostMapping
    public ResponseEntity<ApiResponse<RouteResponse>> createRoute(@Valid @RequestBody UpsertRouteRequest request) {
        RouteResponse response = routeManagementService.createRoute(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Route created successfully", response));
    }

    @Operation(summary = "Update route")
    @PutMapping("/{id}")
    public ApiResponse<RouteResponse> updateRoute(@PathVariable UUID id, @Valid @RequestBody UpsertRouteRequest request) {
        return ApiResponse.success("Route updated successfully", routeManagementService.updateRoute(id, request));
    }

    @Operation(summary = "Get route plan details")
    @GetMapping("/{routeId}/plan")
    public ApiResponse<RoutePlanResponse> getRoutePlan(
            @PathVariable UUID routeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart
    ) {
        return ApiResponse.success(routeManagementService.getRoutePlan(routeId, weekStart));
    }

    @Operation(summary = "Assign or change route plan staff")
    @PutMapping("/{routeId}/plan/staff")
    public ApiResponse<RoutePlanResponse> assignStaff(
            @PathVariable UUID routeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            @Valid @RequestBody AssignRoutePlanStaffRequest request
    ) {
        return ApiResponse.success("Staff assigned successfully", routeManagementService.assignStaff(routeId, weekStart, request));
    }

    @Operation(summary = "Save full plan order")
    @PutMapping("/{routeId}/plan/order")
    public ApiResponse<RoutePlanResponse> reorderPlanItems(
            @PathVariable UUID routeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            @Valid @RequestBody RoutePlanOrderRequest request
    ) {
        return ApiResponse.success("Plan order updated successfully", routeManagementService.reorderPlanItems(routeId, weekStart, request));
    }

    @Operation(summary = "Add a counter to route plan")
    @PostMapping("/{routeId}/plan/counters")
    public ApiResponse<RoutePlanResponse> addCounter(
            @PathVariable UUID routeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            @Valid @RequestBody AddRoutePlanCounterRequest request
    ) {
        return ApiResponse.success("Counter added successfully", routeManagementService.addCounter(routeId, weekStart, request));
    }

    @Operation(summary = "Remove route plan counter")
    @DeleteMapping("/{routeId}/plan/counters/{itemId}")
    public ApiResponse<Void> removeCounter(@PathVariable UUID routeId, @PathVariable UUID itemId) {
        routeManagementService.removeCounter(routeId, itemId);
        return ApiResponse.success("Counter removed successfully", null);
    }

    @Operation(summary = "List available counters for week")
    @GetMapping("/counters/available")
    public ApiResponse<List<AvailableCounterResponse>> getAvailableCounters(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID excludeRouteId
    ) {
        return ApiResponse.success(routeManagementService.getAvailableCounters(weekStart, search, excludeRouteId));
    }

    @Operation(summary = "Publish route plan")
    @PostMapping("/{routeId}/plan/publish")
    public ApiResponse<RoutePlanResponse> publish(
            @PathVariable UUID routeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart
    ) {
        return ApiResponse.success("Plan published successfully", routeManagementService.publish(routeId, weekStart));
    }

    @Operation(summary = "Copy route plan from previous week")
    @PostMapping("/{routeId}/plan/copy")
    public ApiResponse<RoutePlanResponse> copyPlan(
            @PathVariable UUID routeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sourceWeekStart
    ) {
        return ApiResponse.success("Plan copied successfully", routeManagementService.copyPlan(routeId, weekStart, sourceWeekStart));
    }
}
