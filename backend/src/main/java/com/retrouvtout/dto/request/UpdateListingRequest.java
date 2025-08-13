// UpdateListingRequest.java
package com.retrouvtout.dto.request;

import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UpdateListingRequest {
    
    @Size(max = 180, message = "Le titre ne peut pas dépasser 180 caractères")
    private String title;
    
    private String category;
    
    @Size(max = 255, message = "Le lieu ne peut pas dépasser 255 caractères")
    private String locationText;
    
    private BigDecimal latitude;
    private BigDecimal longitude;
    private LocalDateTime foundAt;
    private String description;
    private String imageUrl;
    private String status;
    
    // Constructeurs
    public UpdateListingRequest() {}
    
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
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}