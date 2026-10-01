package com.marketingsales.backend.service.impl;

import com.marketingsales.backend.constant.CustomerStatus;
import com.marketingsales.backend.constant.Role;
import com.marketingsales.backend.constant.RouteCounterType;
import com.marketingsales.backend.constant.RoutePlanStatus;
import com.marketingsales.backend.dto.request.AddRoutePlanCounterRequest;
import com.marketingsales.backend.dto.request.AssignRoutePlanStaffRequest;
import com.marketingsales.backend.dto.request.RoutePlanOrderRequest;
import com.marketingsales.backend.dto.response.RoutePlanResponse;
import com.marketingsales.backend.entity.CustomerCounter;
import com.marketingsales.backend.entity.Distributor;
import com.marketingsales.backend.entity.Retailer;
import com.marketingsales.backend.entity.Route;
import com.marketingsales.backend.entity.RoutePlan;
import com.marketingsales.backend.entity.RoutePlanItem;
import com.marketingsales.backend.entity.User;
import com.marketingsales.backend.exception.BadRequestException;
import com.marketingsales.backend.exception.DuplicateResourceException;
import com.marketingsales.backend.repository.CustomerCounterRepository;
import com.marketingsales.backend.repository.DistributorRepository;
import com.marketingsales.backend.repository.RetailerRepository;
import com.marketingsales.backend.repository.RoutePlanItemRepository;
import com.marketingsales.backend.repository.RoutePlanRepository;
import com.marketingsales.backend.repository.RouteRepository;
import com.marketingsales.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RouteManagementServiceImplTest {

    @Mock
    private RouteRepository routeRepository;
    @Mock
    private RoutePlanRepository routePlanRepository;
    @Mock
    private RoutePlanItemRepository routePlanItemRepository;
    @Mock
    private DistributorRepository distributorRepository;
    @Mock
    private CustomerCounterRepository customerCounterRepository;
    @Mock
    private RetailerRepository retailerRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RouteManagementServiceImpl service;

    private Route route;
    private User staff;
    private LocalDate nextWeek;

    @BeforeEach
    void setUp() {
        route = Route.builder().id(UUID.randomUUID()).name("Route A").zone("North").active(true).build();
        staff = User.builder().id(UUID.randomUUID()).fullName("Staff One").role(Role.STAFF).build();
        nextWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).plusWeeks(1);
    }

    @Test
    void assignStaffAutoLoadsMasterCountersWhenPlanIsEmpty() {
        RoutePlan plan = RoutePlan.builder()
                .id(UUID.randomUUID())
                .routeId(route.getId())
                .route(route)
                .weekStart(nextWeek)
                .status(RoutePlanStatus.DRAFT)
                .version(0L)
                .build();

        Distributor distributor = Distributor.builder().id(UUID.randomUUID()).name("D1").zone("North").status(CustomerStatus.ACTIVE).build();
        CustomerCounter customer = CustomerCounter.builder().id(UUID.randomUUID()).name("C1").status(CustomerStatus.ACTIVE).build();
        Retailer retailer = Retailer.builder().id(UUID.randomUUID()).name("R1").zone("North").status(CustomerStatus.ACTIVE).build();

        when(routeRepository.findById(route.getId())).thenReturn(Optional.of(route));
        when(userRepository.findById(staff.getId())).thenReturn(Optional.of(staff));
        when(routePlanRepository.findByRouteIdAndWeekStart(route.getId(), nextWeek)).thenReturn(Optional.of(plan));
        when(routePlanRepository.existsByWeekStartAndStaffIdAndRouteIdNot(nextWeek, staff.getId(), route.getId())).thenReturn(false);
        when(routePlanRepository.saveAndFlush(any(RoutePlan.class))).thenReturn(plan);
        when(routePlanItemRepository.countByRoutePlanId(plan.getId())).thenReturn(0L);
        when(routePlanItemRepository.findAllByWeekStart(nextWeek)).thenReturn(List.of());
        when(distributorRepository.findAllByMasterRouteIdAndStatusOrderByNameAsc(route.getId(), CustomerStatus.ACTIVE)).thenReturn(List.of(distributor));
        when(customerCounterRepository.findAllByMasterRouteIdAndStatusOrderByNameAsc(route.getId(), CustomerStatus.ACTIVE)).thenReturn(List.of(customer));
        when(retailerRepository.findAllByMasterRouteIdAndStatusOrderByNameAsc(route.getId(), CustomerStatus.ACTIVE)).thenReturn(List.of(retailer));
        when(routePlanItemRepository.findAllByRoutePlanIdOrderByVisitOrderAsc(plan.getId())).thenReturn(List.of());

        AssignRoutePlanStaffRequest request = new AssignRoutePlanStaffRequest();
        request.setStaffId(staff.getId());

        RoutePlanResponse response = service.assignStaff(route.getId(), nextWeek, request);

        assertEquals(staff.getId(), response.getStaffId());
        verify(routePlanItemRepository, org.mockito.Mockito.times(3)).save(any(RoutePlanItem.class));
    }

    @Test
    void reorderRenumbersItemsSequentially() {
        RoutePlan plan = RoutePlan.builder()
                .id(UUID.randomUUID())
                .routeId(route.getId())
                .route(route)
                .weekStart(nextWeek)
                .status(RoutePlanStatus.DRAFT)
                .version(0L)
                .build();

        RoutePlanItem item1 = RoutePlanItem.builder().id(UUID.randomUUID()).routePlanId(plan.getId()).weekStart(nextWeek).counterType(RouteCounterType.DISTRIBUTOR).counterId(UUID.randomUUID()).visitOrder(1).build();
        RoutePlanItem item2 = RoutePlanItem.builder().id(UUID.randomUUID()).routePlanId(plan.getId()).weekStart(nextWeek).counterType(RouteCounterType.CUSTOMER).counterId(UUID.randomUUID()).visitOrder(2).build();

        when(routeRepository.findById(route.getId())).thenReturn(Optional.of(route));
        when(routePlanRepository.findByRouteIdAndWeekStart(route.getId(), nextWeek)).thenReturn(Optional.of(plan));
        when(routePlanItemRepository.findAllByRoutePlanIdOrderByVisitOrderAsc(plan.getId())).thenReturn(List.of(item1, item2));
        when(routePlanRepository.saveAndFlush(any(RoutePlan.class))).thenReturn(plan);

        RoutePlanOrderRequest request = new RoutePlanOrderRequest();
        request.setItemIds(List.of(item2.getId(), item1.getId()));

        service.reorderPlanItems(route.getId(), nextWeek, request);

        assertEquals(1, item2.getVisitOrder());
        assertEquals(2, item1.getVisitOrder());
    }

    @Test
    void addCounterMovesFromAnotherPlanAndRenumbersBoth() {
        RoutePlan targetPlan = RoutePlan.builder().id(UUID.randomUUID()).routeId(route.getId()).route(route).weekStart(nextWeek).status(RoutePlanStatus.DRAFT).version(0L).build();
        RoutePlan sourcePlan = RoutePlan.builder().id(UUID.randomUUID()).routeId(UUID.randomUUID()).weekStart(nextWeek).status(RoutePlanStatus.DRAFT).version(0L).build();

        RoutePlanItem moved = RoutePlanItem.builder()
                .id(UUID.randomUUID())
                .routePlanId(sourcePlan.getId())
                .weekStart(nextWeek)
                .counterType(RouteCounterType.RETAILER)
                .counterId(UUID.randomUUID())
                .visitOrder(1)
                .build();

        when(routeRepository.findById(route.getId())).thenReturn(Optional.of(route));
        when(routePlanRepository.findByRouteIdAndWeekStart(route.getId(), nextWeek)).thenReturn(Optional.of(targetPlan));
        when(routePlanItemRepository.findByWeekStartAndCounterTypeAndCounterId(nextWeek, RouteCounterType.RETAILER, moved.getCounterId())).thenReturn(Optional.of(moved));
        when(routePlanRepository.findById(sourcePlan.getId())).thenReturn(Optional.of(sourcePlan));
        when(routePlanItemRepository.findAllByRoutePlanIdOrderByVisitOrderAsc(targetPlan.getId())).thenReturn(List.of());
        when(routePlanItemRepository.findAllByRoutePlanIdOrderByVisitOrderAsc(sourcePlan.getId())).thenReturn(List.of());
        when(routePlanRepository.saveAndFlush(any(RoutePlan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AddRoutePlanCounterRequest request = new AddRoutePlanCounterRequest();
        request.setCounterType(RouteCounterType.RETAILER);
        request.setCounterId(moved.getCounterId());

        service.addCounter(route.getId(), nextWeek, request);

        assertEquals(targetPlan.getId(), moved.getRoutePlanId());
        assertEquals(1, moved.getVisitOrder());
    }

    @Test
    void removeCounterDeletesAndRenumbers() {
        RoutePlan plan = RoutePlan.builder().id(UUID.randomUUID()).routeId(route.getId()).route(route).weekStart(nextWeek).status(RoutePlanStatus.DRAFT).version(0L).build();
        RoutePlanItem item = RoutePlanItem.builder().id(UUID.randomUUID()).routePlanId(plan.getId()).weekStart(nextWeek).counterType(RouteCounterType.DISTRIBUTOR).counterId(UUID.randomUUID()).visitOrder(1).build();

        when(routePlanItemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(routePlanRepository.findById(plan.getId())).thenReturn(Optional.of(plan));
        when(routePlanItemRepository.findAllByRoutePlanIdOrderByVisitOrderAsc(plan.getId())).thenReturn(List.of());
        when(routePlanRepository.saveAndFlush(any(RoutePlan.class))).thenReturn(plan);

        service.removeCounter(route.getId(), item.getId());

        verify(routePlanItemRepository).delete(item);
    }

    @Test
    void publishRequiresStaffAndItems() {
        RoutePlan plan = RoutePlan.builder().id(UUID.randomUUID()).routeId(route.getId()).route(route).weekStart(nextWeek).status(RoutePlanStatus.DRAFT).version(0L).build();

        when(routeRepository.findById(route.getId())).thenReturn(Optional.of(route));
        when(routePlanRepository.findByRouteIdAndWeekStart(route.getId(), nextWeek)).thenReturn(Optional.of(plan));

        assertThrows(BadRequestException.class, () -> service.publish(route.getId(), nextWeek));
    }

    @Test
    void lockedWeekRejectsEdits() {
        LocalDate currentWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        when(routeRepository.findById(route.getId())).thenReturn(Optional.of(route));

        AssignRoutePlanStaffRequest request = new AssignRoutePlanStaffRequest();
        request.setStaffId(staff.getId());

        assertThrows(BadRequestException.class, () -> service.assignStaff(route.getId(), currentWeek, request));
        verify(routePlanRepository, never()).saveAndFlush(any());
    }

    @Test
    void duplicateStaffAssignmentReturnsConflict() {
        RoutePlan plan = RoutePlan.builder().id(UUID.randomUUID()).routeId(route.getId()).route(route).weekStart(nextWeek).status(RoutePlanStatus.DRAFT).version(0L).build();

        when(routeRepository.findById(route.getId())).thenReturn(Optional.of(route));
        when(userRepository.findById(staff.getId())).thenReturn(Optional.of(staff));
        when(routePlanRepository.findByRouteIdAndWeekStart(route.getId(), nextWeek)).thenReturn(Optional.of(plan));
        when(routePlanRepository.existsByWeekStartAndStaffIdAndRouteIdNot(nextWeek, staff.getId(), route.getId())).thenReturn(true);

        AssignRoutePlanStaffRequest request = new AssignRoutePlanStaffRequest();
        request.setStaffId(staff.getId());

        assertThrows(DuplicateResourceException.class, () -> service.assignStaff(route.getId(), nextWeek, request));
    }
}
