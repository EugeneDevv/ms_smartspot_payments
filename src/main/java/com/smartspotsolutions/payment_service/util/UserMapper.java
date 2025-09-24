package com.smartspotsolutions.payment_service.util;

import com.smartspotsolutions.payment_service.entity.UserEntity;
import com.smartspotsolutions.payment_service.io.PagedResponse;
import com.smartspotsolutions.payment_service.io.request.UserRegisterRequest;
import com.smartspotsolutions.payment_service.io.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserMapper {
    private final PasswordEncoder passwordEncoder;

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    public UserEntity toUserEntity(UserRegisterRequest request) {
        return UserEntity.builder()
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .middleName(request.getMiddleName())
                .lastName(request.getLastName())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .deleted(false)
                .build();
    }

    public UserResponse toUserResponse(UserEntity userEntity) {
        if (userEntity == null) {
            return null;
        }
        return UserResponse.builder()
                .userId(userEntity.getUserId())
                .firstName(userEntity.getFirstName())
                .middleName(userEntity.getMiddleName())
                .lastName(userEntity.getLastName())
                .email(userEntity.getEmail())
                .role(userEntity.getRole())
                .createdAt(userEntity.getCreatedAt())
                .emailVerified(userEntity.getEmailVerified())
                .emailVerificationToken(!activeProfile.equals("prod") ? userEntity.getEmailVerificationToken() : null)
                .enabled(userEntity.getEnabled())
                .deleted(userEntity.getDeleted())
                .deletedAt(userEntity.getDeletedAt())
                .build();
    }

    public List<UserResponse> toUserResponseList(List<UserEntity> userEntities) {
        if (userEntities == null) {
            return List.of();
        }
        return userEntities.stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }

    public PagedResponse<UserResponse> toPagedResponse(Page<UserEntity> userPage) {
        List<UserResponse> content = toUserResponseList(userPage.getContent());
        return new PagedResponse<>(
                content,
                userPage.getNumber(),
                userPage.getSize(),
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.isLast()
        );
    }
}
