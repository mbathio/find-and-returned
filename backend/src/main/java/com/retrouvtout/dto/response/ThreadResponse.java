package com.retrouvtout.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * DTO de réponse pour les threads de conversation
 */
public class ThreadResponse {
    
    private String id;
    
    private ListingResponse listing;
    
    @JsonProperty("owner_user")
    private UserResponse ownerUser;
    
    @JsonProperty("finder_user")
    private UserResponse finderUser;
    
    private String status;
    
    @JsonProperty("approved_by_owner")
    private Boolean approvedByOwner;
    
    @JsonProperty("approved_by_finder")
    private Boolean approvedByFinder;
    
    @JsonProperty("last_message_at")
    private LocalDateTime lastMessageAt;
    
    @JsonProperty("unread_count")
    private Long unreadCount;
    
    @JsonProperty("last_message")
    private MessageResponse lastMessage;
    
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
    
    // Constructeurs
    public ThreadResponse() {}
    
    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public ListingResponse getListing() { return listing; }
    public void setListing(ListingResponse listing) { this.listing = listing; }
    
    public UserResponse getOwnerUser() { return ownerUser; }
    public void setOwnerUser(UserResponse ownerUser) { this.ownerUser = ownerUser; }
    
    public UserResponse getFinderUser() { return finderUser; }
    public void setFinderUser(UserResponse finderUser) { this.finderUser = finderUser; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Boolean getApprovedByOwner() { return approvedByOwner; }
    public void setApprovedByOwner(Boolean approvedByOwner) { this.approvedByOwner = approvedByOwner; }
    
    public Boolean getApprovedByFinder() { return approvedByFinder; }
    public void setApprovedByFinder(Boolean approvedByFinder) { this.approvedByFinder = approvedByFinder; }
    
    public LocalDateTime getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(LocalDateTime lastMessageAt) { this.lastMessageAt = lastMessageAt; }
    
    public Long getUnreadCount() { return unreadCount; }
    public void setUnreadCount(Long unreadCount) { this.unreadCount = unreadCount; }
    
    public MessageResponse getLastMessage() { return lastMessage; }
    public void setLastMessage(MessageResponse lastMessage) { this.lastMessage = lastMessage; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}