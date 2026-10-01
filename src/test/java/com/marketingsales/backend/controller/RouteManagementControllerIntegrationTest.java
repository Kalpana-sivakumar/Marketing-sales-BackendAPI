package com.marketingsales.backend.controller;

import com.marketingsales.backend.constant.RoutePlanStatus;
import com.marketingsales.backend.dto.request.UpsertRouteRequest;
import com.marketingsales.backend.dto.response.RoutePageResponse;
import com.marketingsales.backend.dto.response.RoutePlanResponse;
import com.marketingsales.backend.dto.response.RouteResponse;
import com.marketingsales.backend.service.RouteManagementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RouteManagementControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RouteManagementService routeManagementService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void listRoutesReturnsPage() throws Exception {
        RoutePageResponse page = RoutePageResponse.builder()
                .content(List.of())
                .page(0)
                .size(20)
                .totalElements(0)
                .totalPages(0)
                .build();

        when(routeManagementService.listRoutes(any(), any(), any(), any(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(page);

        mockMvc.perform(get("/api/routes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.page").value(0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createRouteReturnsCreated() throws Exception {
        RouteResponse response = RouteResponse.builder()
                .id(UUID.randomUUID())
                .name("Route A")
                .zone("North")
                .active(true)
                .build();

        when(routeManagementService.createRoute(any(UpsertRouteRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/routes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Route A\",\"zone\":\"North\",\"active\":true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Route A"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getPlanReturnsPlan() throws Exception {
        UUID routeId = UUID.randomUUID();
        LocalDate nextWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).plusWeeks(1);

        RoutePlanResponse response = RoutePlanResponse.builder()
                .routeId(routeId)
                .routeName("Route A")
                .zone("North")
                .weekStart(nextWeek)
                .status(RoutePlanStatus.DRAFT)
                .version(0L)
                .items(List.of())
                .build();

        when(routeManagementService.getRoutePlan(eq(routeId), eq(nextWeek))).thenReturn(response);

        mockMvc.perform(get("/api/routes/{routeId}/plan", routeId)
                        .param("weekStart", nextWeek.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.routeId").value(routeId.toString()));
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void staffCannotAccessManagerRoutes() throws Exception {
        mockMvc.perform(get("/api/routes"))
                .andExpect(status().isForbidden());
    }
}
