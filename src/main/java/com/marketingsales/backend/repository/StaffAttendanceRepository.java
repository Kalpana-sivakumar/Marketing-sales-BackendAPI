package com.marketingsales.backend.repository;

import com.marketingsales.backend.entity.StaffAttendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StaffAttendanceRepository extends JpaRepository<StaffAttendance, UUID> {

    Optional<StaffAttendance> findTopByUserIdAndCheckOutAtIsNullOrderByCheckInAtDesc(UUID userId);

    List<StaffAttendance> findAllByOrderByCheckInAtDesc();
}
