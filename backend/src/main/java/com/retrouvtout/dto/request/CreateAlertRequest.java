package com.retrouvtout.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO pour la création d'une alerte d'objet perdu
 */
public class CreateAlertRequest {
    
    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 180, message = "Le titre ne peut pas dépasser 180 caractères")
    private String title;
    
    @Size(max = 255, message = "Le texte de recherche ne peut pas dépasser 255 caractères")
    private String queryText;
    
    @Size(max = 40, message = "La catégorie ne peut pas dépasser 40 caractères")
    private String category;
    
    @Size(max = 255, message = "Le lieu ne peut pas dépasser 255 caractères")
    private String locationText;
    
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal radiusKm = BigDecimal.valueOf(10.0);
    
    private LocalDate dateFrom;
    private LocalDate dateTo;
    
    private List<String> channels;
    
    // Constructeurs
    public CreateAlertRequest() {}
    
    // Getters et Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getQueryText() { return queryText; }
    public void setQueryText(String queryText) { this.queryText = queryText; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getLocationText() { return locationText; }
    public void setLocationText(String locationText) { this.locationText = locationText; }
    
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    
    public BigDecimal getRadiusKm() { return radiusKm; }
    public void setRadiusKm(BigDecimal radiusKm) { this.radiusKm = radiusKm; }
    
    public LocalDate getDateFrom() { return dateFrom; }
    public void setDateFrom(LocalDate dateFrom) { this.dateFrom = dateFrom; }
    
    public LocalDate getDateTo() { return dateTo; }
    public void setDateTo(LocalDate dateTo) { this.dateTo = dateTo; }
    
    public List<String> getChannels() { return channels; }
    public void setChannels(List<String> channels) { this.channels = channels; }
}