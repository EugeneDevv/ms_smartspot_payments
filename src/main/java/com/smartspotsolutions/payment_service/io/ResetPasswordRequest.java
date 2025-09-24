package com.smartspotsolutions.payment_service.io;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordRequest {
    @NotBlank(message = "New password is required")
    @Pattern(
            regexp = "^(?=.*[a-zA-Z])(?=.*\\d).{6,}$",
            message = "Password must be at least 6 characters and contain letters and numbers"
    )
    private String newPassword;
    @NotBlank(message = "OTP is required")
    private String otp;
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
}
