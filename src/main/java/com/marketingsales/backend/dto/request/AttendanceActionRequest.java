package com.marketingsales.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttendanceActionRequest {

    // Resolved on the mobile device from GPS (e.g. via the platform geocoder)
    // and sent with the check-in/check-out tap.
    @NotBlank(message = "Place name is required")
    @Size(max = 255)
    private String placeName;
}
