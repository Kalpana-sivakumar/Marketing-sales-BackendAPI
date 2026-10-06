package com.marketingsales.backend.dto.request;

import com.marketingsales.backend.constant.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {

    @Size(min = 1, max = 150)
    private String fullName;

    @Email(message = "Email must be valid")
    @Size(max = 180)
    private String email;

    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @Size(max = 20)
    private String phone;

    @Size(min = 1, max = 150)
    private String region;

    private Role role;
}
