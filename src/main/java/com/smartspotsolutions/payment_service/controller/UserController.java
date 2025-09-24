package com.smartspotsolutions.payment_service.controller;

import com.smartspotsolutions.payment_service.entity.UserEntity;
import com.smartspotsolutions.payment_service.enums.Role;
import com.smartspotsolutions.payment_service.io.*;
import com.smartspotsolutions.payment_service.io.request.AuthRequest;
import com.smartspotsolutions.payment_service.io.request.UserUpdateRequest;
import com.smartspotsolutions.payment_service.io.request.UserRegisterRequest;
import com.smartspotsolutions.payment_service.io.response.UserResponse;
import com.smartspotsolutions.payment_service.service.UserService;
import com.smartspotsolutions.payment_service.util.AppUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final AppUtil appUtil;
    private final AuthController authController;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegisterRequest request) {
        var userResponse = userService.createUser(request);
        var authRequest = new AuthRequest(request.getEmail(), request.getPassword());
        // Automatically log in the user after registration
        return authController.login(authRequest);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/internal/profile")
    public ResponseEntity<UserResponse> updateProfile(@Valid @RequestBody UserUpdateRequest request) {
        UserResponse userResponse = userService.updateUser(request);
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile() {
        UserResponse userProfile = userService.getUserProfile();
        return ResponseEntity.ok(userProfile);
    }

    @DeleteMapping("/{id}/soft")
    public ResponseEntity<?> softDeleteUser(@PathVariable String id) {
        UserEntity loggedInUser = userService.getAuthenticatedUserEntity();
        String userId = loggedInUser.getUserId();
        Role role = loggedInUser.getRole();
        if(!role.equals(Role.ADMIN) && userId.equals(id)){
            throw new AccessDeniedException("You do not have permission to perform this action");
        }
        userService.softDeleteUser(id, loggedInUser.getUserId());
        return ResponseEntity.ok("User soft deleted successfully");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/internal/hard")
    public ResponseEntity<?> hardDeleteUser(@RequestParam Long id) {
        userService.hardDeleteUser(id);
        return ResponseEntity.ok("User hard deleted successfully");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/internal/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        Optional<UserResponse> userProfileOptional = userService.getUserById(id);
        UserResponse userProfile = userProfileOptional.orElseThrow(
                () -> new UsernameNotFoundException("User not found with id " + id)
        );
        return ResponseEntity.ok(userProfile);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/internal/deactivate/{id}")
    public ResponseEntity<UserResponse> deactivateUser(@PathVariable Long id){
        UserResponse userProfile = userService.deactivateUser(id);
        return ResponseEntity.ok(userProfile);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/internal/activate/{id}")
    public ResponseEntity<UserResponse> activateUser(@PathVariable Long id){
        UserResponse userProfile = userService.activateUser(id);
        return ResponseEntity.ok(userProfile);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/internal")
    public ResponseEntity<PagedResponse<UserResponse>> getAllUsers(@RequestParam(required = false) String nameLike,
                                                                   @RequestParam(required = false) LocalDate startCreationDate,
                                                                   @RequestParam(required = false) LocalDate endCreationDate,
                                                                   @RequestParam(required = false) Boolean enabled,
                                                                   @RequestParam(required = false) Boolean includeDeleted,
                                                                   @RequestParam(required = false) String role,
                                                                   @RequestParam(defaultValue = "1") @Min(value = 1, message = "Page must not be less than one") Integer page,
                                                                   @RequestParam(defaultValue = "10") @Min(value = 1, message = "Size must not be less than one") Integer size,
                                                                   @RequestParam(defaultValue = "id") String sortBy,
                                                                   @RequestParam(defaultValue = "false") boolean ascending) {
        return ResponseEntity.ok(userService.getUsers(
                nameLike,
                startCreationDate,
                endCreationDate,
                enabled,
                includeDeleted,
                role,
                appUtil.makePageable(page, size, sortBy, ascending)
        ));
    }
}
