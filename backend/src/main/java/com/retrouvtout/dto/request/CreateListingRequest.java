// CreateListingRequest.java
package com.retrouvtout.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CreateListingRequest {
    
    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 180, message = "Le titre ne peut pas dépasser 180 caractères")
    private String title;
    
    @NotBlank(message = "La catégorie est obligatoire")
    private String category;
    
    @NotBlank(message = "Le lieu est obligatoire")
    @Size(max = 255, message = "Le lieu ne peut pas dépasser 255 caractères")
    private String locationText;
    
    private BigDecimal latitude;
    private BigDecimal longitude;
    
    @NotNull(message = "La date de découverte est obligatoire")
    private LocalDateTime foundAt;
    
    @NotBlank(message = "La description est obligatoire")
    private String description;
    
    private String imageUrl;
    
    // Constructeurs
    public CreateListingRequest() {}
    
    // Getters et Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getLocationText() { return locationText; }
    public void setLocationText(String locationText) { this.locationText = locationText; }
    
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    
    public LocalDateTime getFoundAt() { return foundAt; }
    public void setFoundAt(LocalDateTime foundAt) { this.foundAt = foundAt; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
