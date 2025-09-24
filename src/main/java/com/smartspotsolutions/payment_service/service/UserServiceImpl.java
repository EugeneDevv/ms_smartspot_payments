package com.smartspotsolutions.payment_service.service;

import com.smartspotsolutions.payment_service.entity.UserEntity;
import com.smartspotsolutions.payment_service.enums.Role;
import com.smartspotsolutions.payment_service.exception.AlreadyExistsException;
import com.smartspotsolutions.payment_service.exception.ResourceNotFoundException;
import com.smartspotsolutions.payment_service.io.PagedResponse;
import com.smartspotsolutions.payment_service.io.request.UserUpdateRequest;
import com.smartspotsolutions.payment_service.io.request.UserRegisterRequest;
import com.smartspotsolutions.payment_service.io.response.UserResponse;
import com.smartspotsolutions.payment_service.repository.UserRepository;
import com.smartspotsolutions.payment_service.util.TokenGenerator;
import com.smartspotsolutions.payment_service.util.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

import static com.smartspotsolutions.payment_service.util.ServiceUtils.and;
import static com.smartspotsolutions.payment_service.util.ServiceUtils.updateIfDifferent;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final TokenGenerator tokenGenerator;
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final MessageSource messageSource;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    @Value("${app.reset.password.expiry-minutes:15}")
    private int passwordResetVerificationTokenExpiryMinutes;

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
    private Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }

    // --- User Creation ---
    @Override
    @Transactional
    public UserResponse createUser(UserRegisterRequest request) {
        Locale locale = getCurrentLocale();

        if (userRepository.existsByEmail(request.getEmail())) {
            String message = messageSource.getMessage("user.already_exists.email", new Object[]{request.getEmail()}, locale);
            log.warn("User creation failed: {}", message);
            throw new AlreadyExistsException(message);
        }

        UserEntity newUser = userMapper.toUserEntity(request);
        var savedUser = userRepository.save(newUser);

        String successMessage = messageSource.getMessage("common.action.create.success", new Object[]{"User"}, locale);
        log.info("{} with ID: {}", successMessage, savedUser.getUserId());
        return userMapper.toUserResponse(savedUser);
    }

    @Override
    public UserResponse updateUser(UserUpdateRequest request) {
        Locale locale = getCurrentLocale();
        UserEntity user = userRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> {
                    String message = messageSource.getMessage("user.not_found.id", new Object[]{request.getUserId()}, locale);
                    log.warn("User update failed: {}", message);
                    return new ResourceNotFoundException(message);
                });

        log.info("Attempting to update user with ID: {}", request.getUserId());

        updateIfDifferent(user::getFirstName, user::setFirstName, request.getFirstName(), "First name");
        updateIfDifferent(user::getMiddleName, user::setMiddleName, request.getMiddleName(), "Middle name");
        updateIfDifferent(user::getLastName, user::setLastName, request.getLastName(), "Last name");

        UserEntity updatedUser = userRepository.save(user);
        String successMessage = messageSource.getMessage("common.action.update.success", new Object[]{user.getFirstName() + user.getLastName()}, locale);
        log.info("{} with ID: {}", successMessage, updatedUser.getUserId());

        return userMapper.toUserResponse(updatedUser);
    }

    @Override
    public UserResponse getUserProfile() {
        UserEntity user = getAuthenticatedUserEntity();
        return userMapper.toUserResponse(user);
    }

    @Override
    public Optional<UserResponse> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userMapper::toUserResponse);
    }

    @Override
    public Optional<UserResponse> getUserById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toUserResponse);
    }

    // --- User Listing and Filtering ---
    @Override
    public PagedResponse<UserResponse> getUsers(String nameLike,
                                                LocalDate startCreationDate,
                                                LocalDate endCreationDate,
                                                Boolean enabled,
                                                Boolean includeDeleted,
                                                String role,
                                                Pageable pageable) {
        Specification<UserEntity> spec = null;
        if (nameLike != null && !nameLike.isBlank()) {
            String pattern = "%" + nameLike.toLowerCase() + "%";
            spec = and(spec, (root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("firstName")), pattern),
                    cb.like(cb.lower(root.get("middleName")), pattern),
                    cb.like(cb.lower(root.get("lastName")), pattern)
            ));
        }

        if (startCreationDate != null) {
            LocalDateTime startOfDay = startCreationDate.atStartOfDay();
            spec = and(spec, (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), startOfDay));
        }

        if (endCreationDate != null) {
            LocalDateTime endOfDay = endCreationDate.plusDays(1).atStartOfDay();
            spec = and(spec, (root, query, cb) -> cb.lessThan(root.get("createdAt"), endOfDay));
        }

        if (enabled != null) {
            spec = and(spec, (root, query, cb) -> cb.equal(root.get("enabled"), enabled));
        }

        if (includeDeleted != null) {
            if (!includeDeleted) {
                spec = and(spec, (root, query, cb) -> cb.isFalse(root.get("deleted")));
            }
        } else {
            spec = and(spec, (root, query, cb) -> cb.isFalse(root.get("deleted")));
        }

        if (role != null && !role.isBlank()) {
            try {
                Role userRole = Role.valueOf(role.toUpperCase());
                spec = and(spec, (root, query, cb) -> cb.equal(root.get("role"), userRole));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid role '{}' provided for user filter. Ignoring role filter.", role);
            }
        }

        Page<UserEntity> userPage = (spec == null) ? userRepository.findAll(pageable) : userRepository.findAll(spec, pageable);

        return userMapper.toPagedResponse(userPage);
    }


    // --- User Lifecycle Management ---
    @Override
    @Transactional
    public UserResponse deactivateUser(Long id) {
        Locale locale = getCurrentLocale();
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> {
                    String message = messageSource.getMessage("user.not_found.id", new Object[]{id}, locale);
                    log.warn("Deactivation failed: {}", message);
                    return new ResourceNotFoundException(message);
                });

        if (!Boolean.TRUE.equals(user.getEnabled())) {
            String message = messageSource.getMessage("common.action.deactivate.success", new Object[]{"User"}, locale);
            log.info("{} with ID: {}", message, id);
            return userMapper.toUserResponse(user);
        }

        user.setEnabled(false);
        user = userRepository.save(user);
        String successMessage = messageSource.getMessage("common.action.deactivate.success", new Object[]{"User"}, locale);
        log.info("{} with ID: {}", successMessage, id);
        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse activateUser(Long id) {
        Locale locale = getCurrentLocale();
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> {
                    String message = messageSource.getMessage("user.not_found.id", new Object[]{id}, locale);
                    log.warn("Activation failed: {}", message);
                    return new ResourceNotFoundException(message);
                });

        if (Boolean.TRUE.equals(user.getEnabled())) {
            String message = messageSource.getMessage("common.action.activate.success", new Object[]{"User"}, locale);
            log.info("{} with ID: {}", message, id);
            return userMapper.toUserResponse(user);
        }

        user.setEnabled(true);
        user = userRepository.save(user);
        String successMessage = messageSource.getMessage("common.action.activate.success", new Object[]{"User"}, locale);
        log.info("{} with ID: {}", successMessage, id);
        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public void softDeleteUser(String userId, String deletedBy) {
        Locale locale = getCurrentLocale();
        UserEntity user = userRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    String message = messageSource.getMessage("user.not_found.id", new Object[]{userId}, locale);
                    log.warn("Soft delete failed: {}", message);
                    return new ResourceNotFoundException(message);
                });

        if (Boolean.TRUE.equals(user.getDeleted())) {
            String message = messageSource.getMessage("common.action.soft_delete.success", new Object[]{"User"}, locale);
            log.info("{} with ID: {}", message, userId);
            return;
        }

        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        user.setDeletedBy(deletedBy);
        userRepository.save(user);
        String successMessage = messageSource.getMessage("common.action.soft_delete.success", new Object[]{"User"}, locale);
        log.info("{} with ID: {}", successMessage, userId);
    }

    @Override
    @Transactional
    public void hardDeleteUser(Long id) {
        Locale locale = getCurrentLocale();
        userRepository.findById(id)
                .orElseThrow(() -> {
                    String message = messageSource.getMessage("user.not_found.id", new Object[]{id}, locale);
                    log.warn("Hard delete failed: {}", message);
                    return new ResourceNotFoundException(message);
                });

        userRepository.deleteById(id);
        String successMessage = messageSource.getMessage("common.action.hard_delete.success", new Object[]{"User"}, locale);
        log.warn("User with ID {} {}. This action is irreversible.", id, successMessage);
    }

    // --- Authenticated User Retrieval ---
    @Override
    public UserEntity getAuthenticatedUserEntity() {
        Locale locale = getCurrentLocale();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        String message = messageSource.getMessage("user.profile.not_found", new Object[]{null}, locale);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Authenticated user entity not found in DB for email: {}", email);
                    return new UsernameNotFoundException(message);
                });
    }
}