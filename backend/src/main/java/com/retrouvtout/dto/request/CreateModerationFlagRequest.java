package com.retrouvtout.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateModerationFlagRequest {
    @NotNull(message = "Le type d'entité est obligatoire")
    private String entityType;
    
    @NotBlank(message = "L'ID de l'entité est obligatoire")
    private String entityId;
    
    @NotBlank(message = "La raison est obligatoire")
    @Size(max = 255, message = "La raison ne peut pas dépasser 255 caractères")
    private String reason;
    
    @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
    private String description;
    
    private String priority = "medium";
    
    // Constructeurs
    public CreateModerationFlagRequest() {}
    
    public CreateModerationFlagRequest(String entityType, String entityId, String reason) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.reason = reason;
    }
    
    // Getters et setters
    public String getEntityType() {
        return entityType;
    }
    
    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }
    
    public String getEntityId() {
        return entityId;
    }
    
    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getPriority() {
        return priority;
    }
    
    public void setPriority(String priority) {
        this.priority = priority;
    }
}