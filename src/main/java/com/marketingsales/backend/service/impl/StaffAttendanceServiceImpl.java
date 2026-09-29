package com.marketingsales.backend.service.impl;

import com.marketingsales.backend.constant.AttendanceStatus;
import com.marketingsales.backend.dto.request.AttendanceActionRequest;
import com.marketingsales.backend.dto.request.AttendanceRoutePointRequest;
import com.marketingsales.backend.dto.request.StaffLiveLocationRequest;
import com.marketingsales.backend.dto.response.AdminLiveStaffLocationResponse;
import com.marketingsales.backend.dto.response.AdminTodayAttendanceResponse;
import com.marketingsales.backend.dto.response.AttendanceRoutePointResponse;
import com.marketingsales.backend.dto.response.StaffAttendanceResponse;
import com.marketingsales.backend.dto.response.StaffLiveLocationResponse;
import com.marketingsales.backend.entity.StaffAttendance;
import com.marketingsales.backend.entity.StaffLocationTracking;
import com.marketingsales.backend.entity.User;
import com.marketingsales.backend.exception.BadRequestException;
import com.marketingsales.backend.exception.ResourceNotFoundException;
import com.marketingsales.backend.repository.StaffAttendanceRepository;
import com.marketingsales.backend.repository.StaffLocationTrackingRepository;
import com.marketingsales.backend.repository.UserRepository;
import com.marketingsales.backend.service.StaffAttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StaffAttendanceServiceImpl implements StaffAttendanceService {

    private final StaffAttendanceRepository staffAttendanceRepository;
    private final StaffLocationTrackingRepository staffLocationTrackingRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public StaffAttendanceResponse checkIn(UUID userId, AttendanceActionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        staffAttendanceRepository.findTopByStaffIdAndCheckOutTimeIsNullOrderByCheckInTimeDesc(userId)
                .ifPresent(record -> {
                    throw new BadRequestException("You are already checked in. Check out before checking in again.");
                });

        Instant now = Instant.now();
        StaffAttendance attendance = StaffAttendance.builder()
                .staffId(user.getId())
                .staff(user)
                .attendanceDate(LocalDate.now())
                .checkInTime(now)
                .checkInLatitude(request.getLatitude())
                .checkInLongitude(request.getLongitude())
                .checkInLocationName(request.getPlaceName())
                .status(AttendanceStatus.CHECKED_IN)
                .build();

        StaffAttendance saved = staffAttendanceRepository.save(attendance);

        StaffLocationTracking initialLocation = StaffLocationTracking.builder()
                .staffId(user.getId())
                .attendanceId(saved.getId())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .locationName(request.getPlaceName())
                .recordedAt(now)
                .build();
        staffLocationTrackingRepository.save(initialLocation);

        return toResponse(saved);
    }

    @Override
    @Transactional
    public StaffAttendanceResponse checkOut(UUID userId, AttendanceActionRequest request) {
        LocalDate today = LocalDate.now();
        StaffAttendance activeAttendance = staffAttendanceRepository
                .findTopByStaffIdAndAttendanceDateAndStatusAndCheckOutTimeIsNullOrderByCheckInTimeDesc(
                        userId,
                        today,
                        AttendanceStatus.CHECKED_IN
                )
                .orElseThrow(() -> new BadRequestException("No active check-in found for today"));

        activeAttendance.setCheckOutTime(Instant.now());
        activeAttendance.setCheckOutLatitude(request.getLatitude());
        activeAttendance.setCheckOutLongitude(request.getLongitude());
        activeAttendance.setCheckOutLocationName(request.getPlaceName());
        activeAttendance.setStatus(AttendanceStatus.CHECKED_OUT);

        StaffAttendance saved = staffAttendanceRepository.save(activeAttendance);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffAttendanceResponse> getStaffAttendanceForDashboard() {
        return staffAttendanceRepository.findAllByOrderByCheckInTimeDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffAttendanceResponse> getStaffAttendanceForUser(UUID userId) {
        return staffAttendanceRepository.findAllByStaffIdOrderByCheckInTimeDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StaffAttendanceResponse> getActiveAttendanceForUser(UUID userId) {
        return staffAttendanceRepository.findTopByStaffIdAndCheckOutTimeIsNullOrderByCheckInTimeDesc(userId)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public AttendanceRoutePointResponse addRoutePoint(UUID userId, AttendanceRoutePointRequest request) {
        StaffAttendance activeAttendance = staffAttendanceRepository
                .findTopByStaffIdAndCheckOutTimeIsNullOrderByCheckInTimeDesc(userId)
                .orElseThrow(() -> new BadRequestException("No active check-in found to append route data"));

        StaffLocationTracking routePoint = StaffLocationTracking.builder()
                .staffId(userId)
                .attendanceId(activeAttendance.getId())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .locationName(request.getPlaceName())
                .recordedAt(request.getRecordedAt() != null ? request.getRecordedAt() : Instant.now())
                .build();

        StaffLocationTracking saved = staffLocationTrackingRepository.save(routePoint);
        return toRoutePointResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceRoutePointResponse> getActiveRoutePoints(UUID userId) {
        StaffAttendance activeAttendance = staffAttendanceRepository
                .findTopByStaffIdAndCheckOutTimeIsNullOrderByCheckInTimeDesc(userId)
                .orElseThrow(() -> new BadRequestException("No active check-in found"));

        return staffLocationTrackingRepository.findAllByAttendanceIdOrderByRecordedAtAsc(activeAttendance.getId())
                .stream()
                .map(this::toRoutePointResponse)
                .toList();
    }

    @Override
    @Transactional
    public StaffLiveLocationResponse updateLiveLocation(UUID staffId, StaffLiveLocationRequest request) {
        LocalDate today = LocalDate.now();
        StaffAttendance attendance = staffAttendanceRepository
                .findTopByStaffIdAndAttendanceDateAndStatusAndCheckOutTimeIsNullOrderByCheckInTimeDesc(
                        staffId,
                        today,
                        AttendanceStatus.CHECKED_IN
                )
                .orElseThrow(() -> new BadRequestException("You must check in before updating live location"));

        StaffLocationTracking location = StaffLocationTracking.builder()
                .staffId(staffId)
                .attendanceId(attendance.getId())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .locationName(request.getLocationName())
                .recordedAt(Instant.now())
                .build();

        StaffLocationTracking saved = staffLocationTrackingRepository.save(location);
        return StaffLiveLocationResponse.builder()
                .locationId(saved.getId())
                .staffId(saved.getStaffId())
                .attendanceId(saved.getAttendanceId())
                .latitude(saved.getLatitude())
                .longitude(saved.getLongitude())
                .locationName(saved.getLocationName())
                .recordedAt(saved.getRecordedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminTodayAttendanceResponse> getTodayAttendanceForAdmin() {
        LocalDate today = LocalDate.now();
        return staffAttendanceRepository.findAllByAttendanceDateOrderByCheckInTimeDesc(today)
                .stream()
                .map(this::toAdminTodayResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminLiveStaffLocationResponse> getTodayLiveLocationsForAdmin() {
        LocalDate today = LocalDate.now();
        List<StaffAttendance> activeAttendances = staffAttendanceRepository
                .findAllByAttendanceDateAndStatusAndCheckOutTimeIsNullOrderByCheckInTimeDesc(
                        today,
                        AttendanceStatus.CHECKED_IN
                );
        if (activeAttendances.isEmpty()) {
            return List.of();
        }

        List<UUID> attendanceIds = activeAttendances.stream()
                .map(StaffAttendance::getId)
                .toList();
        List<StaffLocationTracking> latestTrackings = staffLocationTrackingRepository.findLatestByAttendanceIds(attendanceIds);

        Map<UUID, StaffLocationTracking> trackingByAttendanceId = new HashMap<>();
        for (StaffLocationTracking tracking : latestTrackings) {
            StaffLocationTracking existing = trackingByAttendanceId.get(tracking.getAttendanceId());
            if (existing == null || tracking.getRecordedAt().isAfter(existing.getRecordedAt())) {
                trackingByAttendanceId.put(tracking.getAttendanceId(), tracking);
            }
        }

        LinkedHashMap<UUID, AdminLiveStaffLocationResponse> deduplicated = new LinkedHashMap<>();
        for (StaffAttendance attendance : activeAttendances) {
            StaffLocationTracking tracking = trackingByAttendanceId.get(attendance.getId());
            AdminLiveStaffLocationResponse row = new AdminLiveStaffLocationResponse(
                    attendance.getStaffId(),
                    attendance.getStaff().getFullName(),
                    tracking != null ? tracking.getLatitude() : attendance.getCheckInLatitude(),
                    tracking != null ? tracking.getLongitude() : attendance.getCheckInLongitude(),
                    tracking != null ? tracking.getLocationName() : attendance.getCheckInLocationName(),
                    tracking != null ? tracking.getRecordedAt() : attendance.getUpdatedAt(),
                    AttendanceStatus.CHECKED_IN
            );

            deduplicated.merge(
                    row.getStaffId(),
                    row,
                    (oldRow, newRow) -> newRow.getLastUpdated().isAfter(oldRow.getLastUpdated()) ? newRow : oldRow
            );
        }
        return deduplicated.values().stream().toList();
    }

    private StaffAttendanceResponse toResponse(StaffAttendance attendance) {
        return StaffAttendanceResponse.builder()
                .attendanceId(attendance.getId())
                .employeeId(attendance.getStaffId())
                .employeeName(attendance.getStaff().getFullName())
                .contactNumber(attendance.getStaff().getPhone())
                .checkInDateTime(attendance.getCheckInTime())
                .checkInPlace(attendance.getCheckInLocationName())
                .checkOutDateTime(attendance.getCheckOutTime())
                .checkOutPlace(attendance.getCheckOutLocationName())
                .build();
    }

    private AttendanceRoutePointResponse toRoutePointResponse(StaffLocationTracking routePoint) {
        return AttendanceRoutePointResponse.builder()
                .pointId(routePoint.getId())
                .attendanceId(routePoint.getAttendanceId())
                .latitude(routePoint.getLatitude())
                .longitude(routePoint.getLongitude())
                .placeName(routePoint.getLocationName())
                .recordedAt(routePoint.getRecordedAt())
                .build();
    }

    private AdminTodayAttendanceResponse toAdminTodayResponse(StaffAttendance attendance) {
        Optional<StaffLocationTracking> latestTracking = attendance.getStatus() == AttendanceStatus.CHECKED_IN
                ? staffLocationTrackingRepository.findTopByAttendanceIdOrderByRecordedAtDesc(attendance.getId())
                : Optional.empty();

        String currentLocationName;
        Double currentLatitude;
        Double currentLongitude;
        Instant lastUpdated;

        if (latestTracking.isPresent()) {
            StaffLocationTracking tracking = latestTracking.get();
            currentLocationName = tracking.getLocationName();
            currentLatitude = tracking.getLatitude();
            currentLongitude = tracking.getLongitude();
            lastUpdated = tracking.getRecordedAt();
        } else if (attendance.getStatus() == AttendanceStatus.CHECKED_OUT) {
            currentLocationName = attendance.getCheckOutLocationName();
            currentLatitude = attendance.getCheckOutLatitude();
            currentLongitude = attendance.getCheckOutLongitude();
            lastUpdated = attendance.getUpdatedAt();
        } else {
            currentLocationName = attendance.getCheckInLocationName();
            currentLatitude = attendance.getCheckInLatitude();
            currentLongitude = attendance.getCheckInLongitude();
            lastUpdated = attendance.getUpdatedAt();
        }

        return AdminTodayAttendanceResponse.builder()
                .staffId(attendance.getStaffId())
                .staffName(attendance.getStaff().getFullName())
                .status(attendance.getStatus())
                .checkInTime(attendance.getCheckInTime())
                .checkOutTime(attendance.getCheckOutTime())
                .currentLocationName(currentLocationName)
                .currentLatitude(currentLatitude)
                .currentLongitude(currentLongitude)
                .lastUpdated(lastUpdated)
                .build();
    }
}
