package com.marketingsales.backend.service.impl;

import com.marketingsales.backend.constant.CustomerStatus;
import com.marketingsales.backend.constant.Role;
import com.marketingsales.backend.constant.RouteCounterType;
import com.marketingsales.backend.dto.request.BulkCounterCoordinateItemRequest;
import com.marketingsales.backend.dto.request.BulkCounterCoordinateUpdateRequest;
import com.marketingsales.backend.dto.request.UpsertCustomerCounterRequest;
import com.marketingsales.backend.dto.request.UpsertDistributorRequest;
import com.marketingsales.backend.dto.request.UpsertRetailerRequest;
import com.marketingsales.backend.dto.response.BulkCounterCoordinateUpdateResponse;
import com.marketingsales.backend.dto.response.CustomerCounterRowResponse;
import com.marketingsales.backend.dto.response.DistributorRowResponse;
import com.marketingsales.backend.dto.response.RetailerRowResponse;
import com.marketingsales.backend.entity.CustomerCounter;
import com.marketingsales.backend.entity.Distributor;
import com.marketingsales.backend.entity.Retailer;
import com.marketingsales.backend.entity.User;
import com.marketingsales.backend.exception.BadRequestException;
import com.marketingsales.backend.exception.DuplicateResourceException;
import com.marketingsales.backend.exception.ResourceNotFoundException;
import com.marketingsales.backend.repository.CustomerCounterRepository;
import com.marketingsales.backend.repository.DistributorRepository;
import com.marketingsales.backend.repository.RetailerRepository;
import com.marketingsales.backend.repository.UserRepository;
import com.marketingsales.backend.service.CustomerNetworkService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerNetworkServiceImpl implements CustomerNetworkService {

    private static final Map<String, String> DISTRIBUTOR_SORT_MAP = Map.of(
            "code", "code",
            "name", "name",
            "contact", "contactPerson",
            "mobile", "mobile",
            "zone", "zone",
            "route", "route",
            "assignedStaff", "assignedStaff.fullName",
            "status", "status",
            "outstanding", "outstandingAmount",
            "lastOrder", "lastOrderAt"
    );

    private static final Map<String, String> COUNTER_SORT_MAP = Map.of(
            "code", "code",
            "name", "name",
            "contact", "contactPerson",
            "mobile", "mobile",
            "zone", "distributor.zone",
            "route", "distributor.route",
            "assignedStaff", "distributor.assignedStaff.fullName",
            "status", "status",
            "outstanding", "outstandingAmount",
            "lastOrder", "lastOrderAt"
    );

    private static final Map<String, String> RETAILER_SORT_MAP = Map.of(
            "code", "code",
            "name", "name",
            "contact", "contactPerson",
            "mobile", "mobile",
            "zone", "zone",
            "route", "route",
            "assignedStaff", "assignedStaff.fullName",
            "status", "status",
            "outstanding", "outstandingAmount",
            "lastOrder", "lastOrderAt"
    );

    private final DistributorRepository distributorRepository;
    private final CustomerCounterRepository customerCounterRepository;
    private final RetailerRepository retailerRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DistributorRowResponse> listDistributors(String search, CustomerStatus status, String sortBy, String sortDir) {
        return distributorRepository.searchDistributors(buildLikePattern(search), status, buildSort(sortBy, sortDir, DISTRIBUTOR_SORT_MAP))
                .stream()
                .map(DistributorRowResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public DistributorRowResponse createDistributor(UpsertDistributorRequest request) {
        String code = request.getCode().trim().toUpperCase();
        if (distributorRepository.existsByCodeIgnoreCase(code)) {
            throw new DuplicateResourceException("A distributor with this code already exists");
        }

        User assignedStaff = getStaffUser(request.getAssignedStaffId());
        Distributor distributor = Distributor.builder()
                .code(code)
                .name(request.getName().trim())
                .address(trimToNull(request.getAddress()))
                .latitude(normalizeLatitude(request.getLatitude()))
                .longitude(normalizeLongitude(request.getLongitude()))
                .googlePlaceId(trimToNull(request.getGooglePlaceId()))
                .contactPerson(request.getContactPerson().trim())
                .mobile(request.getMobile().trim())
                .zone(request.getZone().trim())
                .route(request.getRoute().trim())
                .assignedStaffId(assignedStaff.getId())
                .assignedStaff(assignedStaff)
                .status(request.getStatus())
                .outstandingAmount(normalizeOutstanding(request.getOutstandingAmount()))
                .lastOrderAt(request.getLastOrderAt())
                .build();

        return DistributorRowResponse.from(distributorRepository.save(distributor));
    }

    @Override
    @Transactional
    public DistributorRowResponse updateDistributor(UUID id, UpsertDistributorRequest request) {
        Distributor distributor = distributorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Distributor not found"));

        String code = request.getCode().trim().toUpperCase();
        if (distributorRepository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
            throw new DuplicateResourceException("A distributor with this code already exists");
        }

        User assignedStaff = getStaffUser(request.getAssignedStaffId());
        distributor.setCode(code);
        distributor.setName(request.getName().trim());
        distributor.setAddress(trimToNull(request.getAddress()));
        distributor.setLatitude(normalizeLatitude(request.getLatitude()));
        distributor.setLongitude(normalizeLongitude(request.getLongitude()));
        distributor.setGooglePlaceId(trimToNull(request.getGooglePlaceId()));
        distributor.setContactPerson(request.getContactPerson().trim());
        distributor.setMobile(request.getMobile().trim());
        distributor.setZone(request.getZone().trim());
        distributor.setRoute(request.getRoute().trim());
        distributor.setAssignedStaffId(assignedStaff.getId());
        distributor.setAssignedStaff(assignedStaff);
        distributor.setStatus(request.getStatus());
        distributor.setOutstandingAmount(normalizeOutstanding(request.getOutstandingAmount()));
        distributor.setLastOrderAt(request.getLastOrderAt());

        return DistributorRowResponse.from(distributorRepository.saveAndFlush(distributor));
    }

    @Override
    @Transactional
    public void deleteDistributor(UUID id) {
        Distributor distributor = getDistributor(id);
        distributorRepository.delete(distributor);
    }

    @Override
    @Transactional
    public DistributorRowResponse updateDistributorStatus(UUID id, boolean active) {
        Distributor distributor = getDistributor(id);
        distributor.setStatus(active ? CustomerStatus.ACTIVE : CustomerStatus.INACTIVE);
        return DistributorRowResponse.from(distributorRepository.saveAndFlush(distributor));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerCounterRowResponse> listCounters(
            String search,
            CustomerStatus status,
            UUID distributorId,
            String sortBy,
            String sortDir
    ) {
        return customerCounterRepository.searchCounters(
                        buildLikePattern(search),
                        status,
                        distributorId,
                        buildSort(sortBy, sortDir, COUNTER_SORT_MAP)
                ).stream()
                .map(CustomerCounterRowResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public CustomerCounterRowResponse createCounter(UpsertCustomerCounterRequest request) {
        String code = request.getCode().trim().toUpperCase();
        if (customerCounterRepository.existsByCodeIgnoreCase(code)) {
            throw new DuplicateResourceException("A customer counter with this code already exists");
        }

        Distributor distributor = getDistributor(request.getDistributorId());
        CustomerCounter counter = CustomerCounter.builder()
                .distributorId(distributor.getId())
                .distributor(distributor)
                .code(code)
                .name(request.getName().trim())
                .contactPerson(request.getContactPerson().trim())
                .address(trimToNull(request.getAddress()))
                .mobile(request.getMobile().trim())
                .latitude(normalizeLatitude(request.getLatitude()))
                .longitude(normalizeLongitude(request.getLongitude()))
                .googlePlaceId(trimToNull(request.getGooglePlaceId()))
                .status(request.getStatus())
                .outstandingAmount(normalizeOutstanding(request.getOutstandingAmount()))
                .lastOrderAt(request.getLastOrderAt())
                .build();

        return CustomerCounterRowResponse.from(customerCounterRepository.save(counter));
    }

    @Override
    @Transactional
    public CustomerCounterRowResponse updateCounter(UUID id, UpsertCustomerCounterRequest request) {
        CustomerCounter counter = customerCounterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer counter not found"));

        String code = request.getCode().trim().toUpperCase();
        if (customerCounterRepository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
            throw new DuplicateResourceException("A customer counter with this code already exists");
        }

        Distributor distributor = getDistributor(request.getDistributorId());
        counter.setDistributorId(distributor.getId());
        counter.setDistributor(distributor);
        counter.setCode(code);
        counter.setName(request.getName().trim());
        counter.setContactPerson(request.getContactPerson().trim());
        counter.setAddress(trimToNull(request.getAddress()));
        counter.setMobile(request.getMobile().trim());
        counter.setLatitude(normalizeLatitude(request.getLatitude()));
        counter.setLongitude(normalizeLongitude(request.getLongitude()));
        counter.setGooglePlaceId(trimToNull(request.getGooglePlaceId()));
        counter.setStatus(request.getStatus());
        counter.setOutstandingAmount(normalizeOutstanding(request.getOutstandingAmount()));
        counter.setLastOrderAt(request.getLastOrderAt());

        return CustomerCounterRowResponse.from(customerCounterRepository.saveAndFlush(counter));
    }

    @Override
    @Transactional
    public void deleteCounter(UUID id) {
        CustomerCounter counter = customerCounterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer counter not found"));
        customerCounterRepository.delete(counter);
    }

    @Override
    @Transactional
    public CustomerCounterRowResponse updateCounterStatus(UUID id, boolean active) {
        CustomerCounter counter = customerCounterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer counter not found"));
        counter.setStatus(active ? CustomerStatus.ACTIVE : CustomerStatus.INACTIVE);
        return CustomerCounterRowResponse.from(customerCounterRepository.saveAndFlush(counter));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RetailerRowResponse> listRetailers(String search, CustomerStatus status, String sortBy, String sortDir) {
        return retailerRepository.searchRetailers(buildLikePattern(search), status, buildSort(sortBy, sortDir, RETAILER_SORT_MAP))
                .stream()
                .map(RetailerRowResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public RetailerRowResponse createRetailer(UpsertRetailerRequest request) {
        String code = request.getCode().trim().toUpperCase();
        if (retailerRepository.existsByCodeIgnoreCase(code)) {
            throw new DuplicateResourceException("A retailer with this code already exists");
        }

        User assignedStaff = getStaffUser(request.getAssignedStaffId());
        Retailer retailer = Retailer.builder()
                .code(code)
                .name(request.getName().trim())
                .contactPerson(request.getContactPerson().trim())
                .address(trimToNull(request.getAddress()))
                .mobile(request.getMobile().trim())
                .latitude(normalizeLatitude(request.getLatitude()))
                .longitude(normalizeLongitude(request.getLongitude()))
                .googlePlaceId(trimToNull(request.getGooglePlaceId()))
                .zone(request.getZone().trim())
                .route(request.getRoute().trim())
                .assignedStaffId(assignedStaff.getId())
                .assignedStaff(assignedStaff)
                .status(request.getStatus())
                .outstandingAmount(normalizeOutstanding(request.getOutstandingAmount()))
                .lastOrderAt(request.getLastOrderAt())
                .directUnderGen1(true)
                .build();

        return RetailerRowResponse.from(retailerRepository.save(retailer));
    }

    @Override
    @Transactional
    public RetailerRowResponse updateRetailer(UUID id, UpsertRetailerRequest request) {
        Retailer retailer = retailerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Retailer not found"));

        String code = request.getCode().trim().toUpperCase();
        if (retailerRepository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
            throw new DuplicateResourceException("A retailer with this code already exists");
        }

        User assignedStaff = getStaffUser(request.getAssignedStaffId());
        retailer.setCode(code);
        retailer.setName(request.getName().trim());
        retailer.setContactPerson(request.getContactPerson().trim());
        retailer.setAddress(trimToNull(request.getAddress()));
        retailer.setMobile(request.getMobile().trim());
        retailer.setLatitude(normalizeLatitude(request.getLatitude()));
        retailer.setLongitude(normalizeLongitude(request.getLongitude()));
        retailer.setGooglePlaceId(trimToNull(request.getGooglePlaceId()));
        retailer.setZone(request.getZone().trim());
        retailer.setRoute(request.getRoute().trim());
        retailer.setAssignedStaffId(assignedStaff.getId());
        retailer.setAssignedStaff(assignedStaff);
        retailer.setStatus(request.getStatus());
        retailer.setOutstandingAmount(normalizeOutstanding(request.getOutstandingAmount()));
        retailer.setLastOrderAt(request.getLastOrderAt());
        retailer.setDirectUnderGen1(true);

        return RetailerRowResponse.from(retailerRepository.saveAndFlush(retailer));
    }

    @Override
    @Transactional
    public void deleteRetailer(UUID id) {
        Retailer retailer = retailerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Retailer not found"));
        retailerRepository.delete(retailer);
    }

    @Override
    @Transactional
    public RetailerRowResponse updateRetailerStatus(UUID id, boolean active) {
        Retailer retailer = retailerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Retailer not found"));
        retailer.setStatus(active ? CustomerStatus.ACTIVE : CustomerStatus.INACTIVE);
        retailer.setDirectUnderGen1(true);
        return RetailerRowResponse.from(retailerRepository.saveAndFlush(retailer));
    }

    @Override
    @Transactional
    public BulkCounterCoordinateUpdateResponse bulkUpdateCoordinates(BulkCounterCoordinateUpdateRequest request) {
        int updated = 0;
        for (BulkCounterCoordinateItemRequest item : request.getItems()) {
            if (item.getCounterType() == RouteCounterType.DISTRIBUTOR) {
                Distributor distributor = distributorRepository.findById(item.getCounterId())
                        .orElseThrow(() -> new ResourceNotFoundException("Distributor not found"));
                distributor.setAddress(trimToNull(item.getAddress()));
                distributor.setLatitude(normalizeLatitude(item.getLatitude()));
                distributor.setLongitude(normalizeLongitude(item.getLongitude()));
                distributor.setGooglePlaceId(trimToNull(item.getGooglePlaceId()));
                distributorRepository.save(distributor);
                updated++;
                continue;
            }

            if (item.getCounterType() == RouteCounterType.CUSTOMER) {
                CustomerCounter counter = customerCounterRepository.findById(item.getCounterId())
                        .orElseThrow(() -> new ResourceNotFoundException("Customer counter not found"));
                counter.setAddress(trimToNull(item.getAddress()));
                counter.setLatitude(normalizeLatitude(item.getLatitude()));
                counter.setLongitude(normalizeLongitude(item.getLongitude()));
                counter.setGooglePlaceId(trimToNull(item.getGooglePlaceId()));
                customerCounterRepository.save(counter);
                updated++;
                continue;
            }

            Retailer retailer = retailerRepository.findById(item.getCounterId())
                    .orElseThrow(() -> new ResourceNotFoundException("Retailer not found"));
            retailer.setAddress(trimToNull(item.getAddress()));
            retailer.setLatitude(normalizeLatitude(item.getLatitude()));
            retailer.setLongitude(normalizeLongitude(item.getLongitude()));
            retailer.setGooglePlaceId(trimToNull(item.getGooglePlaceId()));
            retailerRepository.save(retailer);
            updated++;
        }

        return BulkCounterCoordinateUpdateResponse.builder()
                .updatedCount(updated)
                .build();
    }

    private Distributor getDistributor(UUID id) {
        return distributorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Distributor not found"));
    }

    private User getStaffUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Assigned staff user not found"));
        if (user.getRole() != Role.STAFF) {
            throw new BadRequestException("Assigned user must have STAFF role");
        }
        return user;
    }

    private Sort buildSort(String sortBy, String sortDir, Map<String, String> fieldMap) {
        String sortKey = StringUtils.hasText(sortBy) ? sortBy.trim() : "name";
        String property = fieldMap.getOrDefault(sortKey, "name");
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, property);
    }

    private BigDecimal normalizeOutstanding(BigDecimal outstandingAmount) {
        BigDecimal value = outstandingAmount == null ? BigDecimal.ZERO : outstandingAmount;
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Outstanding amount cannot be negative");
        }
        return value;
    }

    private String buildLikePattern(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return "%" + value.trim().toLowerCase() + "%";
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private Double normalizeLatitude(Double value) {
        if (value == null) {
            return null;
        }
        if (value < -90.0 || value > 90.0) {
            throw new BadRequestException("Latitude must be between -90 and 90");
        }
        return value;
    }

    private Double normalizeLongitude(Double value) {
        if (value == null) {
            return null;
        }
        if (value < -180.0 || value > 180.0) {
            throw new BadRequestException("Longitude must be between -180 and 180");
        }
        return value;
    }
}
