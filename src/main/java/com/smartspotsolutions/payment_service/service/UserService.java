package com.smartspotsolutions.payment_service.service;

import com.smartspotsolutions.payment_service.entity.UserEntity;
import com.smartspotsolutions.payment_service.exception.ResourceNotFoundException;
import com.smartspotsolutions.payment_service.io.*;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Service interface for managing user-related operations, including registration,
 * authentication, profile management, and administrative actions.
 */
public interface UserService {
    /**
     * Registers a new user with standard credentials (e.g., email and password).
     *
     * @param request The {@link UserRegistrationRequest} containing the necessary details for user creation.
     * @return The {@link UserResponse} DTO representing the newly registered user.
     */
    UserResponse createUser(UserRegistrationRequest request);

    /**
     * Updates an existing user's profile information.
     *
     * @param request The {@link UpdateUserRequest} containing the fields to be updated.
     * @return The {@link UserResponse} DTO representing the user after the update.
     * @throws ResourceNotFoundException if the user to be updated is not found.
     */
    UserResponse updateUser(UpdateUserRequest request);

    /**
     * Retrieves the profile details of the currently authenticated user based on the security context.
     *
     * @return The {@link UserResponse} DTO for the authenticated user's profile.
     * @throws ResourceNotFoundException if the authenticated user's entity cannot be found in the database.
     */
    UserResponse getUserProfile();

    /**
     * Retrieves a user by their unique email address.
     *
     * @param email The email address of the user to retrieve.
     * @return An {@link Optional} containing the {@link UserResponse} DTO if a user with the given email is found,
     * otherwise an empty Optional.
     */
    Optional<UserResponse> getUserByEmail(String email);

    /**
     * Retrieves a user by their unique database ID.
     *
     * @param id The database ID of the user to retrieve.
     * @return An {@link Optional} containing the {@link UserResponse} DTO if a user with the given ID is found,
     * otherwise an empty Optional.
     */
    Optional<UserResponse> getUserById(Long id);

    /**
     * Retrieves a paginated and filtered list of users. This method supports comprehensive filtering
     * by name, creation date range, activation status, inclusion of soft-deleted users, and user role.
     *
     * @param nameLike          Optional: A string to filter users by first or last name (case-insensitive, partial match).
     * @param startCreationDate Optional: The start date for filtering users by their creation date (inclusive).
     * @param endCreationDate   Optional: The end date for filtering users by their creation date (inclusive).
     * @param enabled           Optional: Filter by user activation status. {@code true} for active users, {@code false} for deactivated users. If {@code null}, both active and deactivated users are included.
     * @param includeDeleted    Optional: If {@code true}, soft-deleted users will be included in the results. If {@code false} or {@code null}, soft-deleted users are excluded by default.
     * @param role              Optional: Filter by user role (e.g., "ADMIN", "USER"). Case-insensitive.
     * @param pageable          The {@link Pageable} object containing pagination (page number, size) and sorting criteria.
     * @return A {@link PagedResponse} containing a page of matching {@link UserResponse} DTOs.
     */
    PagedResponse<UserResponse> getUsers(String nameLike,
                                         LocalDate startCreationDate,
                                         LocalDate endCreationDate,
                                         Boolean enabled,
                                         Boolean includeDeleted,
                                         String role,
                                         Pageable pageable);


    /**
     * Deactivates a user account, preventing them from logging in. This is a soft deactivation,
     * meaning the user's data remains in the system but their account is marked as inactive.
     *
     * @param id The unique database ID of the user to deactivate.
     * @return The {@link UserResponse} DTO of the deactivated user.
     * @throws ResourceNotFoundException if the user with the given ID is not found.
     */
    UserResponse deactivateUser(Long id);

    /**
     * Activates a user account that was previously deactivated, allowing them to log in again.
     *
     * @param id The unique database ID of the user to startSubscription.
     * @return The {@link UserResponse} DTO of the activated user.
     * @throws ResourceNotFoundException if the user with the given ID is not found.
     */
    UserResponse activateUser(Long id);

    /**
     * Performs a soft delete on a user account. The user record remains in the database
     * but is marked as 'deleted' (e.g., via a flag) and is typically excluded from
     * standard application queries. This operation is reversible.
     *
     * @param id        The unique identifier (String) of the user to softDelete.
     * @param deletedBy The identifier (e.g., username or ID) of the administrator or system performing the deletion, for auditing purposes.
     * @throws ResourceNotFoundException if the user with the given ID is not found.
     */
    void softDeleteUser(String id, String deletedBy);

    /**
     * Permanently deletes a user record from the database. This operation is **irreversible**
     * and removes all associated user data. It should be used with extreme caution,
     * strong authorization, and in compliance with data retention policies.
     *
     * @param id The unique database ID of the user to permanently delete.
     * @throws ResourceNotFoundException if the user with the given ID is not found.
     */
    void hardDeleteUser(Long id);
//
//    void sendPasswordResetToken(String email);
//
//    void resetPassword(ResetPasswordRequest request);

    /**
     * Retrieves the complete {@link UserEntity} of the currently authenticated user from
     * the security context and the database. This method is typically used by internal
     * service operations that require full access to the user's entity properties.
     *
     * @return The {@link UserEntity} of the authenticated user.
     * @throws ResourceNotFoundException if the authenticated user's entity cannot be found in the database.
     */
    UserEntity getAuthenticatedUserEntity();
}