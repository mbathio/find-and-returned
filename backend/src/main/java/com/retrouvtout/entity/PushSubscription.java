// PushSubscription.java
package com.retrouvtout.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "push_subscriptions", indexes = {
    @Index(name = "ux_push_endpoint", columnList = "endpoint", unique = true),
    @Index(name = "idx_push_user", columnList = "user_id")
})
@EntityListeners(AuditingEntityListener.class)
public class PushSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Size(max = 512)
    @Column(name = "endpoint", nullable = false, unique = true, length = 512)
    private String endpoint;

    @NotNull
    @Size(max = 255)
    @Column(name = "p256dh_key", nullable = false, length = 255)
    private String p256dhKey;

    @NotNull
    @Size(max = 255)
    @Column(name = "auth_key", nullable = false, length = 255)
    private String authKey;

    @Size(max = 512)
    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    // Constructeurs, getters et setters
    public PushSubscription() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public String getP256dhKey() { return p256dhKey; }
    public void setP256dhKey(String p256dhKey) { this.p256dhKey = p256dhKey; }

    public String getAuthKey() { return authKey; }
    public void setAuthKey(String authKey) { this.authKey = authKey; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(LocalDateTime lastUsedAt) { this.lastUsedAt = lastUsedAt; }

    public void updateLastUsed() {
        this.lastUsedAt = LocalDateTime.now();
    }
}