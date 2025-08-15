// NotificationMessage.java - Déjà défini dans MessageService, mais version améliorée
package com.retrouvtout.websocket;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Message de notification WebSocket
 */
public class NotificationMessage {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("body")
    private String body;
    
    @JsonProperty("data")
    private Map<String, Object> data;
    
    @JsonProperty("action_url")
    private String actionUrl;
    
    @JsonProperty("priority")
    private String priority;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    @JsonProperty("expires_at")
    private LocalDateTime expiresAt;
    
    // Constructeurs
    public NotificationMessage() {
        this.id = java.util.UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
        this.priority = "normal";
    }
    
    public NotificationMessage(String type, String title, String body) {
        this();
        this.type = type;
        this.title = title;
        this.body = body;
    }
    
    public NotificationMessage(String type, String title, String body, String actionUrl) {
        this(type, title, body);
        this.actionUrl = actionUrl;
    }
    
    // Factory methods pour différents types de notifications
    public static NotificationMessage newMessage(String senderName, String threadSubject) {
        return new NotificationMessage(
            "new_message",
            "Nouveau message",
            senderName + " vous a envoyé un message concernant: " + threadSubject
        );
    }
    
    public static NotificationMessage newListing(String title, String category) {
        return new NotificationMessage(
            "new_listing",
            "Nouvel objet trouvé",
            "Un " + category + " a été trouvé: " + title
        );
    }
    
    public static NotificationMessage alertTriggered(String alertTitle, String listingTitle) {
        return new NotificationMessage(
            "alert_triggered",
            "Alerte déclenchée",
            "Votre alerte '" + alertTitle + "' correspond à: " + listingTitle
        );
    }
    
    public static NotificationMessage confirmationGenerated(String code) {
        return new NotificationMessage(
            "confirmation_code",
            "Code de remise généré",
            "Code: " + code + " (valable 24h)"
        );
    }
    
    // Getters et setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    
    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }
    
    public String getActionUrl() { return actionUrl; }
    public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }
    
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
