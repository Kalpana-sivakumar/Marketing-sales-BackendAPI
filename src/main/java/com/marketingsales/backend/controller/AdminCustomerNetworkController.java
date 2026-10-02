package com.marketingsales.backend.controller;

import com.marketingsales.backend.constant.CustomerStatus;
import com.marketingsales.backend.constant.RouteCounterType;
import com.marketingsales.backend.dto.request.BulkCounterCoordinateUpdateRequest;
import com.marketingsales.backend.dto.request.CounterCoordinateUpdateRequest;
import com.marketingsales.backend.dto.request.CustomerStatusRequest;
import com.marketingsales.backend.dto.request.UpsertCustomerCounterRequest;
import com.marketingsales.backend.dto.request.UpsertDistributorRequest;
import com.marketingsales.backend.dto.request.UpsertRetailerRequest;
import com.marketingsales.backend.dto.response.ApiResponse;
import com.marketingsales.backend.dto.response.BulkCounterCoordinateUpdateResponse;
import com.marketingsales.backend.dto.response.CounterLocationPickerResponse;
import com.marketingsales.backend.dto.response.CustomerCounterRowResponse;
import com.marketingsales.backend.dto.response.DistributorRowResponse;
import com.marketingsales.backend.dto.response.RetailerRowResponse;
import com.marketingsales.backend.service.CustomerNetworkService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/customer-network")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','MARKETING_MANAGER')")
@Tag(name = "Admin Customer Network", description = "Distributor, customer counter and retailer master APIs")
public class AdminCustomerNetworkController {

    private final CustomerNetworkService customerNetworkService;

    @GetMapping("/distributors")
    public ApiResponse<List<DistributorRowResponse>> listDistributors(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CustomerStatus status,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        return ApiResponse.success(customerNetworkService.listDistributors(search, status, sortBy, sortDir));
    }

    @PostMapping("/distributors")
    public ResponseEntity<ApiResponse<DistributorRowResponse>> createDistributor(
            @Valid @RequestBody UpsertDistributorRequest request
    ) {
        DistributorRowResponse response = customerNetworkService.createDistributor(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Distributor created successfully", response));
    }

    @PutMapping("/distributors/{id}")
    public ApiResponse<DistributorRowResponse> updateDistributor(
            @PathVariable UUID id,
            @Valid @RequestBody UpsertDistributorRequest request
    ) {
        return ApiResponse.success("Distributor updated successfully", customerNetworkService.updateDistributor(id, request));
    }

    @DeleteMapping("/distributors/{id}")
    public ApiResponse<Void> deleteDistributor(@PathVariable UUID id) {
        customerNetworkService.deleteDistributor(id);
        return ApiResponse.success("Distributor deleted successfully", null);
    }

    @PatchMapping("/distributors/{id}/status")
    public ApiResponse<DistributorRowResponse> updateDistributorStatus(
            @PathVariable UUID id,
            @Valid @RequestBody CustomerStatusRequest request
    ) {
        String message = request.getActive() ? "Distributor activated successfully" : "Distributor deactivated successfully";
        return ApiResponse.success(message, customerNetworkService.updateDistributorStatus(id, request.getActive()));
    }

    @GetMapping("/counters")
    public ApiResponse<List<CustomerCounterRowResponse>> listCounters(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CustomerStatus status,
            @RequestParam(required = false) UUID distributorId,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        return ApiResponse.success(customerNetworkService.listCounters(search, status, distributorId, sortBy, sortDir));
    }

    @PostMapping("/counters")
    public ResponseEntity<ApiResponse<CustomerCounterRowResponse>> createCounter(
            @Valid @RequestBody UpsertCustomerCounterRequest request
    ) {
        CustomerCounterRowResponse response = customerNetworkService.createCounter(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer counter created successfully", response));
    }

    @PutMapping("/counters/{id}")
    public ApiResponse<CustomerCounterRowResponse> updateCounter(
            @PathVariable UUID id,
            @Valid @RequestBody UpsertCustomerCounterRequest request
    ) {
        return ApiResponse.success("Customer counter updated successfully", customerNetworkService.updateCounter(id, request));
    }

    @DeleteMapping("/counters/{id}")
    public ApiResponse<Void> deleteCounter(@PathVariable UUID id) {
        customerNetworkService.deleteCounter(id);
        return ApiResponse.success("Customer counter deleted successfully", null);
    }

    @PatchMapping("/counters/{id}/status")
    public ApiResponse<CustomerCounterRowResponse> updateCounterStatus(
            @PathVariable UUID id,
            @Valid @RequestBody CustomerStatusRequest request
    ) {
        String message = request.getActive() ? "Customer counter activated successfully" : "Customer counter deactivated successfully";
        return ApiResponse.success(message, customerNetworkService.updateCounterStatus(id, request.getActive()));
    }

    @GetMapping("/retailers")
    public ApiResponse<List<RetailerRowResponse>> listRetailers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CustomerStatus status,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        return ApiResponse.success(customerNetworkService.listRetailers(search, status, sortBy, sortDir));
    }

    @PostMapping("/retailers")
    public ResponseEntity<ApiResponse<RetailerRowResponse>> createRetailer(
            @Valid @RequestBody UpsertRetailerRequest request
    ) {
        RetailerRowResponse response = customerNetworkService.createRetailer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Retailer created successfully", response));
    }

    @PutMapping("/retailers/{id}")
    public ApiResponse<RetailerRowResponse> updateRetailer(
            @PathVariable UUID id,
            @Valid @RequestBody UpsertRetailerRequest request
    ) {
        return ApiResponse.success("Retailer updated successfully", customerNetworkService.updateRetailer(id, request));
    }

    @DeleteMapping("/retailers/{id}")
    public ApiResponse<Void> deleteRetailer(@PathVariable UUID id) {
        customerNetworkService.deleteRetailer(id);
        return ApiResponse.success("Retailer deleted successfully", null);
    }

    @PatchMapping("/retailers/{id}/status")
    public ApiResponse<RetailerRowResponse> updateRetailerStatus(
            @PathVariable UUID id,
            @Valid @RequestBody CustomerStatusRequest request
    ) {
        String message = request.getActive() ? "Retailer activated successfully" : "Retailer deactivated successfully";
        return ApiResponse.success(message, customerNetworkService.updateRetailerStatus(id, request.getActive()));
    }

    @PatchMapping("/coordinates/bulk")
    public ApiResponse<BulkCounterCoordinateUpdateResponse> bulkUpdateCoordinates(
            @Valid @RequestBody BulkCounterCoordinateUpdateRequest request
    ) {
        return ApiResponse.success(
                "Coordinates updated successfully",
                customerNetworkService.bulkUpdateCoordinates(request)
        );
    }

    @GetMapping("/coordinates/picker")
    public ApiResponse<CounterLocationPickerResponse> getCounterLocationPicker(
            @RequestParam RouteCounterType counterType,
            @RequestParam UUID counterId
    ) {
        return ApiResponse.success(customerNetworkService.getCounterLocationPicker(counterType, counterId));
    }

    @PatchMapping("/coordinates")
    public ApiResponse<CounterLocationPickerResponse> updateCoordinate(
            @Valid @RequestBody CounterCoordinateUpdateRequest request
    ) {
        return ApiResponse.success(
                "Location pinned successfully",
                customerNetworkService.updateCoordinate(request)
        );
    }
}