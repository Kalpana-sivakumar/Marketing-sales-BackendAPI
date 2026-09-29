package com.marketingsales.backend.repository;

import com.marketingsales.backend.constant.AttendanceStatus;
import com.marketingsales.backend.dto.response.AdminLiveStaffLocationResponse;
import com.marketingsales.backend.entity.StaffLocationTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StaffLocationTrackingRepository extends JpaRepository<StaffLocationTracking, UUID> {

    List<StaffLocationTracking> findAllByAttendanceIdOrderByRecordedAtAsc(UUID attendanceId);

    Optional<StaffLocationTracking> findTopByAttendanceIdOrderByRecordedAtDesc(UUID attendanceId);

    @Query("""
            SELECT slt
            FROM StaffLocationTracking slt
            WHERE slt.attendanceId IN :attendanceIds
              AND slt.recordedAt = (
                    SELECT MAX(slt2.recordedAt)
                    FROM StaffLocationTracking slt2
                    WHERE slt2.attendanceId = slt.attendanceId
              )
            """)
    List<StaffLocationTracking> findLatestByAttendanceIds(@Param("attendanceIds") List<UUID> attendanceIds);

    @Query("""
            SELECT DISTINCT new com.marketingsales.backend.dto.response.AdminLiveStaffLocationResponse(
                sa.staffId,
                sa.staff.fullName,
                slt.latitude,
                slt.longitude,
                slt.locationName,
                slt.recordedAt,
                sa.status
            )
            FROM StaffLocationTracking slt
            JOIN slt.attendance sa
            WHERE sa.attendanceDate = :attendanceDate
              AND sa.status = :status
              AND sa.checkOutTime IS NULL
              AND slt.recordedAt = (
                    SELECT MAX(slt2.recordedAt)
                    FROM StaffLocationTracking slt2
                    JOIN slt2.attendance sa2
                    WHERE sa2.staffId = sa.staffId
                      AND sa2.attendanceDate = :attendanceDate
                      AND sa2.status = :status
                      AND sa2.checkOutTime IS NULL
              )
            ORDER BY slt.recordedAt DESC
            """)
    List<AdminLiveStaffLocationResponse> findTodayLatestLocationsForCheckedInStaff(
            @Param("attendanceDate") LocalDate attendanceDate,
            @Param("status") AttendanceStatus status
    );
}
