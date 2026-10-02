package com.marketingsales.backend.entity;

import com.marketingsales.backend.constant.RouteVisitStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "staff_route_stop_visits", uniqueConstraints = {
        @UniqueConstraint(name = "uk_stop_visit_staff_item_date", columnNames = {"staff_id", "route_plan_item_id", "visit_date"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffRouteStopVisit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "staff_id", nullable = false)
    private UUID staffId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "staff_id", nullable = false, insertable = false, updatable = false)
    private User staff;

    @Column(name = "route_plan_item_id", nullable = false)
    private UUID routePlanItemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "route_plan_item_id", nullable = false, insertable = false, updatable = false)
    private RoutePlanItem routePlanItem;

    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    @Column(name = "check_in_time", nullable = false)
    private Instant checkInTime;

    @Column(name = "check_out_time")
    private Instant checkOutTime;

    @Column(name = "check_in_latitude", nullable = false)
    private Double checkInLatitude;

    @Column(name = "check_in_longitude", nullable = false)
    private Double checkInLongitude;

    @Column(name = "check_in_location_name", length = 255)
    private String checkInLocationName;

    @Column(name = "check_out_latitude")
    private Double checkOutLatitude;

    @Column(name = "check_out_longitude")
    private Double checkOutLongitude;

    @Column(name = "check_out_location_name", length = 255)
    private String checkOutLocationName;

    @Column(name = "check_in_distance_meters")
    private Double checkInDistanceMeters;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RouteVisitStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
