package com.retrouvtout.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant une alerte d'objet perdu
 */
@Entity
@Table(name = "alerts", indexes = {
    @Index(name = "idx_alerts_owner", columnList = "owner_user_id"),
    @Index(name = "idx_alerts_active", columnList = "active"),
    @Index(name = "idx_alerts_category", columnList = "category"),
    @Index(name = "idx_alerts_location", columnList = "latitude, longitude")
})
@EntityListeners(AuditingEntityListener.class)
public class Alert {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @NotNull(message = "L'utilisateur propriétaire est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User ownerUser;

    @NotNull(message = "Le titre est obligatoire")
    @Size(max = 180, message = "Le titre ne peut pas dépasser 180 caractères")
    @Column(name = "title", nullable = false, length = 180)
    private String title;

    @Size(max = 255, message = "Le texte de recherche ne peut pas dépasser 255 caractères")
    @Column(name = "query_text", length = 255)
    private String queryText;

    @Size(max = 40, message = "La catégorie ne peut pas dépasser 40 caractères")
    @Column(name = "category", length = 40)
    private String category;

    @Size(max = 255, message = "Le lieu ne peut pas dépasser 255 caractères")
    @Column(name = "location_text", length = 255)
    private String locationText;

    @Column(name = "latitude", precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "radius_km", precision = 6, scale = 2)
    private BigDecimal radiusKm = BigDecimal.valueOf(10.0);

    @Column(name = "date_from")
    private LocalDate dateFrom;

    @Column(name = "date_to")
    private LocalDate dateTo;

    @Column(name = "channels", columnDefinition = "JSON")
    private String channelsJson;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "last_triggered_at")
    private LocalDateTime lastTriggeredAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructeurs
    public Alert() {}

    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public User getOwnerUser() { return ownerUser; }
    public void setOwnerUser(User ownerUser) { this.ownerUser = ownerUser; }

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

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public LocalDateTime getLastTriggeredAt() { return lastTriggeredAt; }
    public void setLastTriggeredAt(LocalDateTime lastTriggeredAt) { this.lastTriggeredAt = lastTriggeredAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Gestion des canaux JSON
    public List<String> getChannels() {
        if (channelsJson == null || channelsJson.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(channelsJson, mapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }

    public void setChannels(List<String> channels) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.channelsJson = mapper.writeValueAsString(channels);
        } catch (JsonProcessingException e) {
            this.channelsJson = "[]";
        }
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

