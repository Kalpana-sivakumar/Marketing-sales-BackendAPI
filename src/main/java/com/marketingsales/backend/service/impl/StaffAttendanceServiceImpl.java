package com.marketingsales.backend.service.impl;

import com.marketingsales.backend.dto.request.AttendanceActionRequest;
import com.marketingsales.backend.dto.response.StaffAttendanceResponse;
import com.marketingsales.backend.entity.StaffAttendance;
import com.marketingsales.backend.entity.User;
import com.marketingsales.backend.exception.BadRequestException;
import com.marketingsales.backend.exception.ResourceNotFoundException;
import com.marketingsales.backend.repository.StaffAttendanceRepository;
import com.marketingsales.backend.repository.UserRepository;
import com.marketingsales.backend.service.StaffAttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StaffAttendanceServiceImpl implements StaffAttendanceService {

    private final StaffAttendanceRepository staffAttendanceRepository;
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
}
