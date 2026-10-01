package com.marketingsales.backend.controller;

import com.marketingsales.backend.dto.response.StaffRouteResponse;
import com.marketingsales.backend.service.StaffRouteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StaffRouteControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StaffRouteService staffRouteService;

    @Test
    @WithMockUser(roles = "STAFF")
    void getMyRouteReturnsEmptyWhenNoPublishedPlan() throws Exception {
        when(staffRouteService.getPublishedRouteForDate(any(), any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/staff/me/route"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("No published route found"));
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void getMyRouteReturnsPublishedRoute() throws Exception {
        StaffRouteResponse response = StaffRouteResponse.builder()
                .routeId(UUID.randomUUID())
                .routeName("Route A")
                .zone("North")
                .weekStart(LocalDate.now())
                .stops(List.of())
                .build();

        when(staffRouteService.getPublishedRouteForDate(any(), any())).thenReturn(Optional.of(response));

        mockMvc.perform(get("/api/staff/me/route"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.routeName").value("Route A"));
    }
}
