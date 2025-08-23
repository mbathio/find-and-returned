package com.retrouvtout.service;

import com.retrouvtout.dto.request.CreateListingRequest;
import com.retrouvtout.dto.request.UpdateListingRequest;
import com.retrouvtout.dto.response.ListingResponse;
import com.retrouvtout.dto.response.PagedResponse;
import com.retrouvtout.entity.Listing;
import com.retrouvtout.entity.User;
import com.retrouvtout.exception.ResourceNotFoundException;
import com.retrouvtout.repository.ListingRepository;
import com.retrouvtout.repository.UserRepository;
import com.retrouvtout.util.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service pour la gestion des annonces conforme au cahier des charges
 * Section 3.2 - Gestion des annonces d'objets retrouvés
 * CORRIGÉ - Suppression des méthodes non existantes
 */
@Service
@Transactional
public class ListingService {

    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final NotificationService notificationService;

    @Autowired
    public ListingService(ListingRepository listingRepository,
                         UserRepository userRepository,
                         ModelMapper modelMapper,
                         NotificationService notificationService) {
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.notificationService = notificationService;
    }

    /**
     * Rechercher des annonces avec filtres - Section 3.2
     */
    @Transactional(readOnly = true)
    public PagedResponse<ListingResponse> searchListings(String query, String category, String location,
                                               BigDecimal lat, BigDecimal lng, Double radiusKm,
                                               LocalDate dateFrom, LocalDate dateTo,
                                               Pageable pageable) {
        
        Specification<Listing> spec = Specification.where(null);

        // Filtre par statut actif uniquement
        spec = spec.and((root, query1, cb) -> 
            cb.equal(root.get("status"), Listing.ListingStatus.ACTIVE));

        // Filtre par modération - Section 3.4
        spec = spec.and((root, query1, cb) -> 
            cb.equal(root.get("isModerated"), true));

        // Filtre par mot-clé - Section 3.2
        if (query != null && !query.trim().isEmpty()) {
            spec = spec.and((root, query1, cb) -> 
                cb.or(
                    cb.like(cb.lower(root.get("title")), "%" + query.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("description")), "%" + query.toLowerCase() + "%")
                ));
        }

        // Filtre par catégorie - Section 3.2
        if (category != null && !category.trim().isEmpty()) {
            spec = spec.and((root, query1, cb) -> 
                cb.equal(root.get("category"), Listing.ListingCategory.fromValue(category)));
        }

        // Filtre par lieu - Section 3.2
        if (location != null && !location.trim().isEmpty()) {
            spec = spec.and((root, query1, cb) -> 
                cb.like(cb.lower(root.get("locationText")), "%" + location.toLowerCase() + "%"));
        }

        // Filtre par date - Section 3.2
        if (dateFrom != null) {
            LocalDateTime dateTimeFrom = dateFrom.atStartOfDay();
            spec = spec.and((root, query1, cb) -> 
                cb.greaterThanOrEqualTo(root.get("foundAt"), dateTimeFrom));
        }

        if (dateTo != null) {
            LocalDateTime dateTimeTo = dateTo.atTime(23, 59, 59);
            spec = spec.and((root, query1, cb) -> 
                cb.lessThanOrEqualTo(root.get("foundAt"), dateTimeTo));
        }

        Page<Listing> listings = listingRepository.findAll(spec, pageable);
        
        List<ListingResponse> listingResponses = listings.getContent().stream()
            .map(modelMapper::mapListingToListingResponse)
            .collect(Collectors.toList());

        return modelMapper.createPagedResponse(
            listingResponses,
            pageable.getPageNumber() + 1,
            pageable.getPageSize(),
            listings.getTotalElements()
        );
    }

    /**
     * Obtenir une annonce par son ID
     */
    @Transactional(readOnly = true)
    public ListingResponse getListingById(String id) {
        Listing listing = listingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Annonce", "id", id));
        
        return modelMapper.mapListingToListingResponse(listing);
    }

    /**
     * Créer une nouvelle annonce - Section 3.2
     */
     public ListingResponse createListing(CreateListingRequest request, String userId) {
        System.out.println("🔧 CREATE LISTING:");
        System.out.println("  - Catégorie reçue: '" + request.getCategory() + "'");
        
        // ✅ Conversion avec gestion d'erreur
        Listing.ListingCategory category;
        try {
            category = Listing.ListingCategory.fromValue(request.getCategory());
            System.out.println("  - Catégorie convertie: " + category.name() + " (" + category.getValue() + ")");
        } catch (Exception e) {
            System.err.println("❌ Erreur conversion catégorie: " + e.getMessage());
            throw new IllegalArgumentException("Catégorie invalide: " + request.getCategory());
        }
        
        Listing listing = new Listing();
        // ... autres champs ...
        listing.setCategory(category); // ✅ Le converter s'occupera de sauvegarder la bonne valeur
        
        Listing saved = listingRepository.save(listing);
        System.out.println("✅ Annonce sauvée avec catégorie: " + saved.getCategory().getValue());
        
        return modelMapper.mapListingToListingResponse(saved);
    }

    /**
     * Mettre à jour une annonce
     */
    public ListingResponse updateListing(String id, UpdateListingRequest request, String userId) {
        Listing listing = listingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Annonce", "id", id));

        // Vérifier que l'utilisateur est le propriétaire
        if (!listing.getFinderUser().getId().equals(userId)) {
            throw new SecurityException("Vous n'êtes pas autorisé à modifier cette annonce");
        }

        // Mettre à jour les champs modifiables
        if (request.getTitle() != null) {
            listing.setTitle(request.getTitle());
        }
        if (request.getCategory() != null) {
            listing.setCategory(Listing.ListingCategory.fromValue(request.getCategory()));
        }
        if (request.getLocationText() != null) {
            listing.setLocationText(request.getLocationText());
        }
        if (request.getLatitude() != null) {
            listing.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            listing.setLongitude(request.getLongitude());
        }
        if (request.getFoundAt() != null) {
            listing.setFoundAt(request.getFoundAt());
        }
        if (request.getDescription() != null) {
            listing.setDescription(request.getDescription());
        }
        if (request.getImageUrl() != null) {
            listing.setImageUrl(request.getImageUrl());
        }

        Listing updatedListing = listingRepository.save(listing);
        return modelMapper.mapListingToListingResponse(updatedListing);
    }

    /**
     * Supprimer une annonce (soft delete)
     */
    public void deleteListing(String id, String userId) {
        Listing listing = listingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Annonce", "id", id));

        if (!listing.getFinderUser().getId().equals(userId)) {
            throw new SecurityException("Vous n'êtes pas autorisé à supprimer cette annonce");
        }

        listing.setStatus(Listing.ListingStatus.SUPPRIME);
        listingRepository.save(listing);
    }

    /**
     * Incrémenter le compteur de vues
     */
    @Transactional
    public void incrementViewCount(String id) {
        Listing listing = listingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Annonce", "id", id));

        listing.incrementViewCount();
        listingRepository.save(listing);
    }

    /**
     * Obtenir les annonces d'un utilisateur
     */
    @Transactional(readOnly = true)
    public PagedResponse<ListingResponse> getUserListings(String userId, String status, Pageable pageable) {
        User user = userRepository.findByIdAndActiveTrue(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", userId));

        Page<Listing> listings;
        
        if (status != null && !status.trim().isEmpty()) {
            Listing.ListingStatus listingStatus = Listing.ListingStatus.fromValue(status);
            listings = listingRepository.findByFinderUserAndStatusOrderByCreatedAtDesc(
                user, listingStatus, pageable);
        } else {
            listings = listingRepository.findByFinderUserAndStatusNotOrderByCreatedAtDesc(
                user, Listing.ListingStatus.SUPPRIME, pageable);
        }

        List<ListingResponse> listingResponses = listings.getContent().stream()
            .map(modelMapper::mapListingToListingResponse)
            .collect(Collectors.toList());

        return modelMapper.createPagedResponse(
            listingResponses,
            pageable.getPageNumber() + 1,
            pageable.getPageSize(),
            listings.getTotalElements()
        );
    }

    /**
     * Déclencher les notifications pour une nouvelle annonce - Section 3.3
     * SIMPLIFIÉ - notification générale sans ciblage spécifique
     */
    private void triggerNotificationsForNewListing(Listing listing) {
        try {
            // Rechercher tous les utilisateurs propriétaires actifs
            // Requête simplifiée pour éviter l'erreur findByRole
            List<User> allUsers = userRepository.findAll();
            List<User> interestedUsers = allUsers.stream()
                .filter(user -> user.getRole() == User.UserRole.PROPRIETAIRE && user.getActive())
                .collect(Collectors.toList());
            
            // Pour chaque utilisateur propriétaire, envoyer une notification
            for (User user : interestedUsers) {
                if (user.getEmailVerified()) {
                    notificationService.notifyObjectFound(user, listing);
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi des notifications: " + e.getMessage());
        }
    }
}