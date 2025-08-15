package com.retrouvtout.websocket;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TypingMessage {
    @JsonProperty("thread_id")
    private String threadId;
    
    @JsonProperty("user_id")
    private String userId;
    
    @JsonProperty("user_name")
    private String userName;
    
    @JsonProperty("is_typing")
    private boolean isTyping;
    
    // Constructeurs
    public TypingMessage() {}
    
    public TypingMessage(String threadId, String userId, boolean isTyping) {
        this.threadId = threadId;
        this.userId = userId;
        this.isTyping = isTyping;
    }
    
    // Getters et setters
    public String getThreadId() { return threadId; }
    public void setThreadId(String threadId) { this.threadId = threadId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public boolean isTyping() { return isTyping; }
    public void setTyping(boolean typing) { isTyping = typing; }
}