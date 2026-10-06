package com.marketingsales.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "staff_location_tracking")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffLocationTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "staff_id", nullable = false)
    private UUID staffId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "staff_id", nullable = false, insertable = false, updatable = false)
    private User staff;

    @Column(name = "attendance_id", nullable = false)
    private UUID attendanceId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attendance_id", nullable = false, insertable = false, updatable = false)
    private StaffAttendance attendance;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "location_name", length = 255)
    private String locationName;

    @Column(name = "google_place_id", length = 255)
    private String googlePlaceId;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;
}
