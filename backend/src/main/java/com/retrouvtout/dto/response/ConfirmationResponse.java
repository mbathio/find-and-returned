package com.retrouvtout.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * DTO de réponse pour les confirmations de remise
 */
public class ConfirmationResponse {
    
    private String id;
    
    @JsonProperty("thread_id")
    private String threadId;
    
    private String code;
    
    @JsonProperty("expires_at")
    private LocalDateTime expiresAt;
    
    @JsonProperty("used_at")
    private LocalDateTime usedAt;
    
    @JsonProperty("used_by_user")
    private UserResponse usedByUser;
    
    @JsonProperty("is_expired")
    private Boolean isExpired;
    
    @JsonProperty("is_used")
    private Boolean isUsed;
    
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    // Constructeurs
    public ConfirmationResponse() {}
    
    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getThreadId() { return threadId; }
    public void setThreadId(String threadId) { this.threadId = threadId; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }
    
    public UserResponse getUsedByUser() { return usedByUser; }
    public void setUsedByUser(UserResponse usedByUser) { this.usedByUser = usedByUser; }
    
    public Boolean getIsExpired() { return isExpired; }
    public void setIsExpired(Boolean isExpired) { this.isExpired = isExpired; }
    
    public Boolean getIsUsed() { return isUsed; }
    public void setIsUsed(Boolean isUsed) { this.isUsed = isUsed; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}