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
    
    // Constructeurs, getters et setters
}