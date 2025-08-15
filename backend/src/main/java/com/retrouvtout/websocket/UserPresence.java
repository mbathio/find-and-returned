// UserPresence.java
package com.retrouvtout.websocket;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * Présence utilisateur WebSocket
 */
public class UserPresence {
    
    @JsonProperty("user_id")
    private String userId;
    
    @JsonProperty("user_name")
    private String userName;
    
    @JsonProperty("status")
    private String status; // online, away, offline
    
    @JsonProperty("last_seen")
    private LocalDateTime lastSeen;
    
    @JsonProperty("current_thread")
    private String currentThread;
    
    // Constructeurs
    public UserPresence() {}
    
    public UserPresence(String userId, String userName, String status) {
        this.userId = userId;
        this.userName = userName;
        this.status = status;
        this.lastSeen = LocalDateTime.now();
    }
    
    // Getters et setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getLastSeen() { return lastSeen; }
    public void setLastSeen(LocalDateTime lastSeen) { this.lastSeen = lastSeen; }
    
    public String getCurrentThread() { return currentThread; }
    public void setCurrentThread(String currentThread) { this.currentThread = currentThread; }
}
