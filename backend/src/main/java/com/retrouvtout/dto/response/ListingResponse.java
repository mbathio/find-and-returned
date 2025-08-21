package com.retrouvtout.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de réponse pour les annonces
 * Structure EXACTEMENT conforme au frontend Listing interface
 */
public class ListingResponse {
    
    private String id;
    private String title;
    private String category;
    
    @JsonProperty("locationText")
    private String locationText;
    
    private BigDecimal latitude;
    private BigDecimal longitude;
    
    @JsonProperty("foundAt")
    private String foundAt; // ISO string format pour le frontend
    
    private String description;
    
    @JsonProperty("imageUrl")
    private String imageUrl;
    
    private String status; // "active" ou "resolved" pour correspondre au frontend
    
    @JsonProperty("finderUserId")
    private String finderUserId; // ID simple pour correspondre au frontend
    
    @JsonProperty("createdAt")
    private String createdAt; // ISO string format
    
    @JsonProperty("updatedAt")
    private String updatedAt; // ISO string format
    
    // Constructeurs
    public ListingResponse() {}
    
    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
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
    
    public String getFoundAt() { return foundAt; }
    public void setFoundAt(String foundAt) { this.foundAt = foundAt; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getFinderUserId() { return finderUserId; }
    public void setFinderUserId(String finderUserId) { this.finderUserId = finderUserId; }
    
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}