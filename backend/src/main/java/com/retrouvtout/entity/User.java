package com.retrouvtout.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entité représentant un utilisateur
 * Conforme au cahier des charges - Section 3.1
 * ✅ CORRECTION FINALE: Enum avec @JsonValue pour correspondre exactement au frontend
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_email", columnList = "email"),
    @Index(name = "idx_users_role", columnList = "role"),
    @Index(name = "idx_users_active", columnList = "active")
})
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @NotNull(message = "Le nom est obligatoire")
    @Size(max = 120, message = "Le nom ne peut pas dépasser 120 caractères")
    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @NotNull(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    @Size(max = 190, message = "L'email ne peut pas dépasser 190 caractères")
    @Column(name = "email", nullable = false, unique = true, length = 190)
    private String email;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Size(max = 40, message = "Le numéro de téléphone ne peut pas dépasser 40 caractères")
    @Column(name = "phone", length = 40)
    private String phone;

    /**
     * ✅ CORRECTION FINALE: Rôles avec @JsonValue pour sérialisation correcte
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role = UserRole.MIXTE;

    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Relations
    @OneToMany(mappedBy = "finderUser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Listing> listings;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OAuthAccount> oauthAccounts;

    /**
     * ✅ CORRECTION FINALE: Énumération des rôles avec @JsonValue
     * Garantit que la sérialisation JSON utilise exactement les valeurs attendues par le frontend
     */
    public enum UserRole {
        RETROUVEUR("retrouveur"),
        PROPRIETAIRE("proprietaire"), 
        MIXTE("mixte");

        private final String value;

        UserRole(String value) {
            this.value = value;
        }

        /**
         * ✅ ANNOTATION CRITIQUE pour la cohérence frontend ↔ backend
         * Assure que Jackson sérialise avec les bonnes valeurs string
         */
        @JsonValue
        public String getValue() {
            return value;
        }

        public static UserRole fromValue(String value) {
            if (value == null) {
                return MIXTE; // Valeur par défaut
            }
            
            for (UserRole role : UserRole.values()) {
                if (role.value.equals(value)) {
                    return role;
                }
            }
            
            // Si la valeur n'est pas reconnue, retourner mixte par défaut
            return MIXTE;
        }
    }

    // Constructeurs
    public User() {}

    public User(String name, String email, String passwordHash, UserRole role) {
        this.id = java.util.UUID.randomUUID().toString();
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role != null ? role : UserRole.MIXTE;
    }

    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role != null ? role : UserRole.MIXTE; }

    public Boolean getEmailVerified() { return emailVerified; }
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<Listing> getListings() { return listings; }
    public void setListings(List<Listing> listings) { this.listings = listings; }

    public List<OAuthAccount> getOauthAccounts() { return oauthAccounts; }
    public void setOauthAccounts(List<OAuthAccount> oauthAccounts) { this.oauthAccounts = oauthAccounts; }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (role == null) {
            role = UserRole.MIXTE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}