package com.marketingsales.backend.service;

import com.marketingsales.backend.constant.CustomerStatus;
import com.marketingsales.backend.dto.request.BulkCounterCoordinateUpdateRequest;
import com.marketingsales.backend.dto.request.UpsertCustomerCounterRequest;
import com.marketingsales.backend.dto.request.UpsertDistributorRequest;
import com.marketingsales.backend.dto.request.UpsertRetailerRequest;
import com.marketingsales.backend.dto.response.BulkCounterCoordinateUpdateResponse;
import com.marketingsales.backend.dto.response.CustomerCounterRowResponse;
import com.marketingsales.backend.dto.response.DistributorRowResponse;
import com.marketingsales.backend.dto.response.RetailerRowResponse;

import java.util.List;
import java.util.UUID;

public interface CustomerNetworkService {

    List<DistributorRowResponse> listDistributors(String search, CustomerStatus status, String sortBy, String sortDir);

    DistributorRowResponse createDistributor(UpsertDistributorRequest request);

    DistributorRowResponse updateDistributor(UUID id, UpsertDistributorRequest request);

    void deleteDistributor(UUID id);

    DistributorRowResponse updateDistributorStatus(UUID id, boolean active);

    List<CustomerCounterRowResponse> listCounters(
            String search,
            CustomerStatus status,
            UUID distributorId,
            String sortBy,
            String sortDir
    );

    CustomerCounterRowResponse createCounter(UpsertCustomerCounterRequest request);

    CustomerCounterRowResponse updateCounter(UUID id, UpsertCustomerCounterRequest request);

    void deleteCounter(UUID id);

    CustomerCounterRowResponse updateCounterStatus(UUID id, boolean active);

    List<RetailerRowResponse> listRetailers(String search, CustomerStatus status, String sortBy, String sortDir);

    RetailerRowResponse createRetailer(UpsertRetailerRequest request);

    RetailerRowResponse updateRetailer(UUID id, UpsertRetailerRequest request);

    void deleteRetailer(UUID id);

    RetailerRowResponse updateRetailerStatus(UUID id, boolean active);

    BulkCounterCoordinateUpdateResponse bulkUpdateCoordinates(BulkCounterCoordinateUpdateRequest request);
}