// Confirmation.java
package com.retrouvtout.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "confirmations", indexes = {
    @Index(name = "ux_confirmations_thread", columnList = "thread_id", unique = true),
    @Index(name = "idx_confirmations_code", columnList = "code"),
    @Index(name = "idx_confirmations_expires", columnList = "expires_at")
})
@EntityListeners(AuditingEntityListener.class)
public class Confirmation {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thread_id", nullable = false, unique = true)
    private Thread thread;

    @NotNull
    @Size(max = 12)
    @Column(name = "code", nullable = false, length = 12)
    private String code;

    @NotNull
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "used_by_user_id")
    private User usedByUser;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructeurs, getters et setters
    public Confirmation() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Thread getThread() { return thread; }
    public void setThread(Thread thread) { this.thread = thread; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }

    public User getUsedByUser() { return usedByUser; }
    public void setUsedByUser(User usedByUser) { this.usedByUser = usedByUser; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isUsed() {
        return usedAt != null;
    }
}