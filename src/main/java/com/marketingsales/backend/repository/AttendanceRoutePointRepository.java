package com.marketingsales.backend.repository;

import com.marketingsales.backend.entity.StaffLocationTracking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AttendanceRoutePointRepository extends JpaRepository<StaffLocationTracking, UUID> {

    List<StaffLocationTracking> findAllByAttendanceIdOrderByRecordedAtAsc(UUID attendanceId);
}
