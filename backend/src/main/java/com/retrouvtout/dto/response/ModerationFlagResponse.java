package com.retrouvtout.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * DTO de réponse pour les signalements de modération
 */
public class ModerationFlagResponse {
    
    private Long id;
    
    @JsonProperty("entity_type")
    private String entityType;
    
    @JsonProperty("entity_id")
    private String entityId;
    
    private String reason;
    private String description;
    private String status;
    private String priority;
    
    @JsonProperty("created_by_user")
    private UserResponse createdByUser;
    
    @JsonProperty("reviewed_by_user")
    private UserResponse reviewedByUser;
    
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    @JsonProperty("reviewed_at")
    private LocalDateTime reviewedAt;
    
    // Constructeurs
    public ModerationFlagResponse() {}
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    
    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    
    public UserResponse getCreatedByUser() { return createdByUser; }
    public void setCreatedByUser(UserResponse createdByUser) { this.createdByUser = createdByUser; }
    
    public UserResponse getReviewedByUser() { return reviewedByUser; }
    public void setReviewedByUser(UserResponse reviewedByUser) { this.reviewedByUser = reviewedByUser; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
}