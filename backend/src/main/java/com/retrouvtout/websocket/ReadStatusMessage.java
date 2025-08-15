package com.retrouvtout.websocket;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ReadStatusMessage {
    @JsonProperty("thread_id")
    private String threadId;
    
    @JsonProperty("user_id")
    private String userId;
    
    // Constructeurs
    public ReadStatusMessage() {}
    
    public ReadStatusMessage(String threadId, String userId) {
        this.threadId = threadId;
        this.userId = userId;
    }
    
    // Getters et setters
    public String getThreadId() { return threadId; }
    public void setThreadId(String threadId) { this.threadId = threadId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}