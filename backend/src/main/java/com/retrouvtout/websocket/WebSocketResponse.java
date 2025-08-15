// WebSocketResponse.java
package com.retrouvtout.websocket;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * Réponse WebSocket générique
 */
public class WebSocketResponse<T> {
    
    @JsonProperty("success")
    private boolean success;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("data")
    private T data;
    
    @JsonProperty("error_code")
    private String errorCode;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    // Constructeurs
    public WebSocketResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    public WebSocketResponse(boolean success, String message, T data) {
        this();
        this.success = success;
        this.message = message;
        this.data = data;
    }
    
    // Factory methods
    public static <T> WebSocketResponse<T> success(String message, T data) {
        return new WebSocketResponse<>(true, message, data);
    }
    
    public static <T> WebSocketResponse<T> error(String message, String errorCode) {
        WebSocketResponse<T> response = new WebSocketResponse<>(false, message, null);
        response.setErrorCode(errorCode);
        return response;
    }
    
    // Getters et setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}