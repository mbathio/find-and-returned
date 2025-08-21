package com.retrouvtout.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entité représentant une annonce d'objet retrouvé
 * Conforme au cahier des charges - Section 3.2
 * Champs requis : type d'objet, lieu de découverte, date, photo, description, catégorie
 */
@Entity
@Table(name = "listings", indexes = {
    @Index(name = "idx_listings_finder", columnList = "finder_user_id"),
    @Index(name = "idx_listings_category", columnList = "category"),
    @Index(name = "idx_listings_status", columnList = "status"),
    @Index(name = "idx_listings_found_at", columnList = "found_at"),
    @Index(name = "idx_listings_location", columnList = "latitude, longitude"),
    @Index(name = "idx_listings_moderated", columnList = "is_moderated")
})
@EntityListeners(AuditingEntityListener.class)
public class Listing {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @NotNull(message = "L'utilisateur retrouveur est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "finder_user_id", nullable = false)
    private User finderUser;

    /**
     * Type d'objet - Cahier des charges 3.2
     */
    @NotNull(message = "Le type d'objet est obligatoire")
    @Size(max = 180, message = "Le type d'objet ne peut pas dépasser 180 caractères")
    @Column(name = "title", nullable = false, length = 180)
    private String title;

    /**
     * Catégorie - Cahier des charges 3.2
     * Exemples : électronique, clés, vêtements
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private ListingCategory category;

    /**
     * Lieu de découverte - Cahier des charges 3.2
     */
    @NotNull(message = "Le lieu de découverte est obligatoire")
    @Size(max = 255, message = "Le lieu ne peut pas dépasser 255 caractères")
    @Column(name = "location_text", nullable = false, length = 255)
    private String locationText;

    /**
     * Coordonnées géographiques pour la recherche par lieu
     */
    @Column(name = "latitude", precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 9, scale = 6)
    private BigDecimal longitude;

    /**
     * Date de découverte - Cahier des charges 3.2
     */
    @NotNull(message = "La date de découverte est obligatoire")
    @Column(name = "found_at", nullable = false)
    private LocalDateTime foundAt;

    /**
     * Description - Cahier des charges 3.2
     */
    @NotNull(message = "La description est obligatoire")
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    /**
     * Photo - Cahier des charges 3.2
     */
    @Size(max = 512, message = "L'URL de l'image ne peut pas dépasser 512 caractères")
    @Column(name = "image_url", length = 512)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ListingStatus status = ListingStatus.ACTIVE;

    @Column(name = "views_count", nullable = false)
    private Long viewsCount = 0L;

    /**
     * Modération - Cahier des charges 3.4
     */
    @Column(name = "is_moderated", nullable = false)
    private Boolean isModerated = false;

    @Column(name = "moderated_at")
    private LocalDateTime moderatedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Relations
    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ListingImage> images;

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Thread> threads;

    /**
     * Catégories d'objets conformes au cahier des charges
     * Section 3.2 : électronique, clés, vêtements
     */
    public enum ListingCategory {
        ELECTRONIQUE("electronique"),
        CLES("cles"),
        VETEMENTS("vetements"),
        DOCUMENTS("documents"),
        BAGAGERIE("bagagerie"),
        AUTRE("autre");

        private final String value;

        ListingCategory(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static ListingCategory fromValue(String value) {
            for (ListingCategory category : ListingCategory.values()) {
                if (category.value.equals(value)) {
                    return category;
                }
            }
            throw new IllegalArgumentException("Catégorie invalide: " + value);
        }
    }

    /**
     * Statuts d'annonce
     */
    public enum ListingStatus {
        ACTIVE("active"),
        RESOLU("resolu"),
        SUSPENDU("suspendu"),
        SUPPRIME("supprime");

        private final String value;

        ListingStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static ListingStatus fromValue(String value) {
            for (ListingStatus status : ListingStatus.values()) {
                if (status.value.equals(value)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Statut invalide: " + value);
        }
    }

    // Constructeurs
    public Listing() {}

    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public User getFinderUser() { return finderUser; }
    public void setFinderUser(User finderUser) { this.finderUser = finderUser; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public ListingCategory getCategory() { return category; }
    public void setCategory(ListingCategory category) { this.category = category; }

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

    public ListingStatus getStatus() { return status; }
    public void setStatus(ListingStatus status) { this.status = status; }

    public Long getViewsCount() { return viewsCount; }
    public void setViewsCount(Long viewsCount) { this.viewsCount = viewsCount; }

    public Boolean getIsModerated() { return isModerated; }
    public void setIsModerated(Boolean isModerated) { this.isModerated = isModerated; }

    public LocalDateTime getModeratedAt() { return moderatedAt; }
    public void setModeratedAt(LocalDateTime moderatedAt) { this.moderatedAt = moderatedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<ListingImage> getImages() { return images; }
    public void setImages(List<ListingImage> images) { this.images = images; }

    public List<Thread> getThreads() { return threads; }
    public void setThreads(List<Thread> threads) { this.threads = threads; }

    // Méthodes utilitaires
    public void incrementViewCount() {
        this.viewsCount = (this.viewsCount == null ? 0L : this.viewsCount) + 1L;
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
        if (viewsCount == null) {
            viewsCount = 0L;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}