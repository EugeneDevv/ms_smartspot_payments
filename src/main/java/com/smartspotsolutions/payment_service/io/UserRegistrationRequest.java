package com.smartspotsolutions.payment_service.io;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRegistrationRequest {

    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters") // Added size constraint
    private String firstName;

    @Size(max = 50, message = "Middle name cannot exceed 50 characters") // Added size constraint
    private String middleName;

    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters") // Added size constraint
    private String lastName;

    @NotNull(message = "Email is required")
    @Pattern(
            regexp = "^[a-zA-Z0-9._%+-]+@(?:[A-Za-z0-9-]+\\.)+[A-Za-z]{2,}$",
            message = "Invalid email format"
    )
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    @Pattern(
            regexp = "^(?=.*[a-zA-Z])(?=.*\\d).{6,}$",
            message = "Password must be at least 6 characters and contain letters and numbers"
    )
    private String password;
}
