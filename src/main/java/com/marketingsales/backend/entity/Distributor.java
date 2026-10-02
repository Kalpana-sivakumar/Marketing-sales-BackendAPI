package com.marketingsales.backend.entity;

import com.marketingsales.backend.constant.CustomerStatus;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "distributors", uniqueConstraints = {
        @UniqueConstraint(name = "uk_distributors_code", columnNames = "code")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Distributor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 180)
    private String name;

    @Column(length = 500)
    private String address;

    @Column(name = "pinned_location_name", length = 180)
    private String pinnedLocationName;

    @Column(name = "contact_person", nullable = false, length = 150)
    private String contactPerson;

    @Column(nullable = false, length = 20)
    private String mobile;

    @Column(nullable = false, length = 150)
    private String zone;

    @Column(nullable = false, length = 150)
    private String route;

    @Column(name = "assigned_staff_id", nullable = false)
    private UUID assignedStaffId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assigned_staff_id", nullable = false, insertable = false, updatable = false)
    private User assignedStaff;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CustomerStatus status;

    @Builder.Default
    @Column(name = "outstanding_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal outstandingAmount = BigDecimal.ZERO;

    @Column(name = "last_order_at")
    private Instant lastOrderAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "master_route_id")
    private UUID masterRouteId;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "master_route_id", insertable = false, updatable = false)
    private Route masterRoute;
}