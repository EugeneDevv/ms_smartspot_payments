package com.smartspotsolutions.payment_service.io;

import com.smartspotsolutions.payment_service.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class UserResponse {
    private String userId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private Role role;
    private Boolean emailVerified;
    private String emailVerificationToken;
    private Boolean enabled;
    private Boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;
}
