package com.marketingsales.backend.repository;

import com.marketingsales.backend.entity.AttendanceRoutePoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AttendanceRoutePointRepository extends JpaRepository<AttendanceRoutePoint, UUID> {

    List<AttendanceRoutePoint> findAllByAttendanceIdOrderByRecordedAtAsc(UUID attendanceId);
}
