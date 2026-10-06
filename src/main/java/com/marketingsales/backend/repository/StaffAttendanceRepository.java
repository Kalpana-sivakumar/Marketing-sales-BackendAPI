package com.marketingsales.backend.repository;

import com.marketingsales.backend.constant.AttendanceStatus;
import com.marketingsales.backend.entity.StaffAttendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StaffAttendanceRepository extends JpaRepository<StaffAttendance, UUID> {

    Optional<StaffAttendance> findTopByStaffIdAndCheckOutTimeIsNullOrderByCheckInTimeDesc(UUID staffId);

    Optional<StaffAttendance> findTopByStaffIdOrderByCheckInTimeDesc(UUID staffId);

    List<StaffAttendance> findAllByStaffIdOrderByCheckInTimeDesc(UUID staffId);

    List<StaffAttendance> findAllByOrderByCheckInTimeDesc();

    List<StaffAttendance> findAllByAttendanceDateOrderByCheckInTimeDesc(LocalDate attendanceDate);

    Optional<StaffAttendance> findTopByStaffIdAndAttendanceDateAndStatusAndCheckOutTimeIsNullOrderByCheckInTimeDesc(
            UUID staffId,
            LocalDate attendanceDate,
            AttendanceStatus status
    );

    List<StaffAttendance> findAllByAttendanceDateAndStatusAndCheckOutTimeIsNullOrderByCheckInTimeDesc(
            LocalDate attendanceDate,
            AttendanceStatus status
    );
}
