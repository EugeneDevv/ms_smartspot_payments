package com.smartspotsolutions.payment_service.entity;

import com.smartspotsolutions.payment_service.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "smp_users")
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
// --- Hibernate-specific soft delete configuration ---
// This annotation tells Hibernate to update 'deleted' to true instead of true deleting the row
@SQLDelete(sql = "UPDATE spt_users SET deleted = true, deleted_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP WHERE id=?")
public class UserEntity extends BaseAuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String userId;

    @Column(unique = true)
    private String email;

    private String firstName;
    private String middleName;
    private String lastName;
    private String password;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.ADMIN;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true; // For account activation/deactivation. If false, user cannot log in.

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false; // For soft deletion. If true, the user is considered "deleted" but still in DB.

    private LocalDateTime deletedAt; // Timestamp of when the user was soft-deleted.

    private String deletedBy; // User ID (or name) of the admin who performed the soft-delete.

    @Builder.Default
    private Boolean emailVerified = false;

    private String emailVerificationToken;
    private LocalDateTime emailVerificationTokenExpiry;

    private String passwordResetToken;
    private LocalDateTime passwordResetTokenExpiry;

    @PrePersist
    public void generateUuid() {
        if (this.userId == null) {
            this.userId = UUID.randomUUID().toString();
        }
    }

}
