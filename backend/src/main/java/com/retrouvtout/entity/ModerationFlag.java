// ModerationFlag.java
package com.retrouvtout.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "moderation_flags", indexes = {
    @Index(name = "idx_flags_entity", columnList = "entity_type, entity_id"),
    @Index(name = "idx_flags_status", columnList = "status"),
    @Index(name = "idx_flags_priority", columnList = "priority"),
    @Index(name = "idx_flags_created_at", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
public class ModerationFlag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false)
    private EntityType entityType;

    @NotNull
    @Size(max = 64)
    @Column(name = "entity_id", nullable = false, length = 64)
    private String entityId;

    @NotNull
    @Size(max = 255)
    @Column(name = "reason", nullable = false, length = 255)
    private String reason;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FlagStatus status = FlagStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private FlagPriority priority = FlagPriority.MEDIUM;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_user_id")
    private User reviewedByUser;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    public enum EntityType {
        LISTING("listing"),
        MESSAGE("message"),
        USER("user");

        private final String value;

        EntityType(String value) { this.value = value; }
        public String getValue() { return value; }

        public static EntityType fromValue(String value) {
            for (EntityType type : EntityType.values()) {
                if (type.value.equals(value)) return type;
            }
            throw new IllegalArgumentException("Type d'entité invalide: " + value);
        }
    }

    public enum FlagStatus {
        PENDING("pending"),
        APPROVED("approved"),
        REJECTED("rejected");

        private final String value;

        FlagStatus(String value) { this.value = value; }
        public String getValue() { return value; }

        public static FlagStatus fromValue(String value) {
            for (FlagStatus status : FlagStatus.values()) {
                if (status.value.equals(value)) return status;
            }
            throw new IllegalArgumentException("Statut de signalement invalide: " + value);
        }
    }

    public enum FlagPriority {
        LOW("low"),
        MEDIUM("medium"),
        HIGH("high"),
        URGENT("urgent");

        private final String value;

        FlagPriority(String value) { this.value = value; }
        public String getValue() { return value; }

        public static FlagPriority fromValue(String value) {
            for (FlagPriority priority : FlagPriority.values()) {
                if (priority.value.equals(value)) return priority;
            }
            throw new IllegalArgumentException("Priorité de signalement invalide: " + value);
        }
    }

    // Constructeurs, getters et setters
    public ModerationFlag() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public EntityType getEntityType() { return entityType; }
    public void setEntityType(EntityType entityType) { this.entityType = entityType; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public FlagStatus getStatus() { return status; }
    public void setStatus(FlagStatus status) { this.status = status; }

    public FlagPriority getPriority() { return priority; }
    public void setPriority(FlagPriority priority) { this.priority = priority; }

    public User getCreatedByUser() { return createdByUser; }
    public void setCreatedByUser(User createdByUser) { this.createdByUser = createdByUser; }

    public User getReviewedByUser() { return reviewedByUser; }
    public void setReviewedByUser(User reviewedByUser) { this.reviewedByUser = reviewedByUser; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
}