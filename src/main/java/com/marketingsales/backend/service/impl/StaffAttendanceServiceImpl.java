package com.marketingsales.backend.service.impl;

import com.marketingsales.backend.dto.request.AttendanceActionRequest;
import com.marketingsales.backend.dto.request.AttendanceRoutePointRequest;
import com.marketingsales.backend.dto.response.AttendanceRoutePointResponse;
import com.marketingsales.backend.dto.response.StaffAttendanceResponse;
import com.marketingsales.backend.entity.AttendanceRoutePoint;
import com.marketingsales.backend.entity.StaffAttendance;
import com.marketingsales.backend.entity.User;
import com.marketingsales.backend.exception.BadRequestException;
import com.marketingsales.backend.exception.ResourceNotFoundException;
import com.marketingsales.backend.repository.AttendanceRoutePointRepository;
import com.marketingsales.backend.repository.StaffAttendanceRepository;
import com.marketingsales.backend.repository.UserRepository;
import com.marketingsales.backend.service.StaffAttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StaffAttendanceServiceImpl implements StaffAttendanceService {

    private final StaffAttendanceRepository staffAttendanceRepository;
    private final AttendanceRoutePointRepository attendanceRoutePointRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public StaffAttendanceResponse checkIn(UUID userId, AttendanceActionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        staffAttendanceRepository.findTopByUserIdAndCheckOutAtIsNullOrderByCheckInAtDesc(userId)
                .ifPresent(record -> {
                    throw new BadRequestException("You are already checked in. Check out before checking in again.");
                });

        StaffAttendance attendance = StaffAttendance.builder()
                .user(user)
                .checkInPlace(request.getPlaceName())
                .build();

        StaffAttendance saved = staffAttendanceRepository.save(attendance);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public StaffAttendanceResponse checkOut(UUID userId, AttendanceActionRequest request) {
        StaffAttendance activeAttendance = staffAttendanceRepository
                .findTopByUserIdAndCheckOutAtIsNullOrderByCheckInAtDesc(userId)
                .orElseThrow(() -> new BadRequestException("No active check-in found to check out"));

        activeAttendance.setCheckOutAt(Instant.now());
        activeAttendance.setCheckOutPlace(request.getPlaceName());

        StaffAttendance saved = staffAttendanceRepository.save(activeAttendance);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffAttendanceResponse> getStaffAttendanceForDashboard() {
        return staffAttendanceRepository.findAllByOrderByCheckInAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffAttendanceResponse> getStaffAttendanceForUser(UUID userId) {
        return staffAttendanceRepository.findAllByUserIdOrderByCheckInAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StaffAttendanceResponse> getActiveAttendanceForUser(UUID userId) {
        return staffAttendanceRepository.findTopByUserIdAndCheckOutAtIsNullOrderByCheckInAtDesc(userId)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public AttendanceRoutePointResponse addRoutePoint(UUID userId, AttendanceRoutePointRequest request) {
        StaffAttendance activeAttendance = staffAttendanceRepository
                .findTopByUserIdAndCheckOutAtIsNullOrderByCheckInAtDesc(userId)
                .orElseThrow(() -> new BadRequestException("No active check-in found to append route data"));

        AttendanceRoutePoint routePoint = AttendanceRoutePoint.builder()
                .attendance(activeAttendance)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .placeName(request.getPlaceName())
                .recordedAt(request.getRecordedAt() != null ? request.getRecordedAt() : Instant.now())
                .build();

        AttendanceRoutePoint saved = attendanceRoutePointRepository.save(routePoint);
        return toRoutePointResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceRoutePointResponse> getActiveRoutePoints(UUID userId) {
        StaffAttendance activeAttendance = staffAttendanceRepository
                .findTopByUserIdAndCheckOutAtIsNullOrderByCheckInAtDesc(userId)
                .orElseThrow(() -> new BadRequestException("No active check-in found"));

        return attendanceRoutePointRepository.findAllByAttendanceIdOrderByRecordedAtAsc(activeAttendance.getId())
                .stream()
                .map(this::toRoutePointResponse)
                .toList();
    }

    private StaffAttendanceResponse toResponse(StaffAttendance attendance) {
        return StaffAttendanceResponse.builder()
                .attendanceId(attendance.getId())
                .employeeId(attendance.getUser().getId())
                .employeeName(attendance.getUser().getFullName())
                .contactNumber(attendance.getUser().getPhone())
                .checkInDateTime(attendance.getCheckInAt())
                .checkInPlace(attendance.getCheckInPlace())
                .checkOutDateTime(attendance.getCheckOutAt())
                .checkOutPlace(attendance.getCheckOutPlace())
                .build();
    }

    private AttendanceRoutePointResponse toRoutePointResponse(AttendanceRoutePoint routePoint) {
        return AttendanceRoutePointResponse.builder()
                .pointId(routePoint.getId())
                .attendanceId(routePoint.getAttendance().getId())
                .latitude(routePoint.getLatitude())
                .longitude(routePoint.getLongitude())
                .placeName(routePoint.getPlaceName())
                .recordedAt(routePoint.getRecordedAt())
                .build();
    }
}
