package com.retrouvtout.service;

import com.retrouvtout.dto.request.CreateListingRequest;
import com.retrouvtout.dto.request.UpdateListingRequest;
import com.retrouvtout.dto.response.ListingResponse;
import com.retrouvtout.entity.Listing;
import com.retrouvtout.entity.User;
import com.retrouvtout.exception.ResourceNotFoundException;
import com.retrouvtout.repository.ListingRepository;
import com.retrouvtout.repository.UserRepository;
import com.retrouvtout.util.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
 * Service pour la gestion des annonces d'objets retrouvés
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
     * Rechercher des annonces avec filtres
     */
    @Transactional(readOnly = true)
    public Page<ListingResponse> searchListings(String query, String category, String location,
                                               BigDecimal lat, BigDecimal lng, Double radiusKm,
                                               LocalDate dateFrom, LocalDate dateTo,
                                               Pageable pageable) {
        
        Specification<Listing> spec = Specification.where(null);

        // Filtre par statut actif uniquement
        spec = spec.and((root, query1, cb) -> 
            cb.equal(root.get("status"), Listing.ListingStatus.ACTIVE));

        // Filtre par modération (afficher seulement les annonces modérées)
        spec = spec.and((root, query1, cb) -> 
            cb.equal(root.get("isModerated"), true));

        // Filtre par mot-clé
        if (query != null && !query.trim().isEmpty()) {
            spec = spec.and((root, query1, cb) -> 
                cb.or(
                    cb.like(cb.lower(root.get("title")), "%" + query.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("description")), "%" + query.toLowerCase() + "%")
                ));
        }

        // Filtre par catégorie
        if (category != null && !category.trim().isEmpty()) {
            spec = spec.and((root, query1, cb) -> 
                cb.equal(root.get("category"), Listing.ListingCategory.fromValue(category)));
        }

        // Filtre par lieu
        if (location != null && !location.trim().isEmpty()) {
            spec = spec.and((root, query1, cb) -> 
                cb.like(cb.lower(root.get("locationText")), "%" + location.toLowerCase() + "%"));
        }

        // Filtre géographique
        if (lat != null && lng != null && radiusKm != null) {
            spec = spec.and((root, query1, cb) -> {
                // Utilisation de la formule de Haversine pour calculer la distance
                return cb.lessThanOrEqualTo(
                    cb.function("ST_Distance_Sphere",
                        Double.class,
                        cb.function("POINT", Object.class, root.get("longitude"), root.get("latitude")),
                        cb.function("POINT", Object.class, cb.literal(lng), cb.literal(lat))
                    ),
                    radiusKm * 1000 // Conversion en mètres
                );
            });
        }

        // Filtre par date
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
        return listings.map(modelMapper::mapListingToListingResponse);
    }

    /**
     * Obtenir une annonce par son ID
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "listings", key = "#id")
    public ListingResponse getListingById(String id) {
        Listing listing = listingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Annonce", "id", id));
        
        return modelMapper.mapListingToListingResponse(listing);
    }

    /**
     * Créer une nouvelle annonce
     */
    @CacheEvict(value = "listings", allEntries = true)
    public ListingResponse createListing(CreateListingRequest request, String userId) {
        User user = userRepository.findByIdAndActiveTrue(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", userId));

        Listing listing = new Listing();
        listing.setId(java.util.UUID.randomUUID().toString());
        listing.setFinderUser(user);
        listing.setTitle(request.getTitle());
        listing.setCategory(Listing.ListingCategory.fromValue(request.getCategory()));
        listing.setLocationText(request.getLocationText());
        listing.setLatitude(request.getLatitude());
        listing.setLongitude(request.getLongitude());
        listing.setFoundAt(request.getFoundAt());
        listing.setDescription(request.getDescription());
        listing.setImageUrl(request.getImageUrl());
        listing.setStatus(Listing.ListingStatus.ACTIVE);
        listing.setIsModerated(true); // Auto-approuvé pour l'instant

        Listing savedListing = listingRepository.save(listing);
        return modelMapper.mapListingToListingResponse(savedListing);
    }

 

    /**
     * Mettre à jour une annonce
     */
    @CacheEvict(value = "listings", key = "#id")
    public ListingResponse updateListing(String id, UpdateListingRequest request, String userId) {
        Listing listing = listingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Annonce", "id", id));

        // Vérifier que l'utilisateur est le propriétaire de l'annonce
        if (!listing.getFinderUser().getId().equals(userId)) {
            throw new SecurityException("Vous n'êtes pas autorisé à modifier cette annonce");
        }

        // Mettre à jour les champs
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
        if (request.getStatus() != null) {
            listing.setStatus(Listing.ListingStatus.fromValue(request.getStatus()));
        }

        Listing updatedListing = listingRepository.save(listing);
        return modelMapper.mapListingToListingResponse(updatedListing);
    }

    /**
     * Supprimer une annonce
     */
    @CacheEvict(value = "listings", key = "#id")
    public void deleteListing(String id, String userId) {
        Listing listing = listingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Annonce", "id", id));

        // Vérifier que l'utilisateur est le propriétaire de l'annonce
        if (!listing.getFinderUser().getId().equals(userId)) {
            throw new SecurityException("Vous n'êtes pas autorisé à supprimer cette annonce");
        }

        // Soft delete en changeant le statut
        listing.setStatus(Listing.ListingStatus.SUPPRIME);
        listingRepository.save(listing);
    }

    /**
     * Marquer une annonce comme résolue
     */
    @CacheEvict(value = "listings", key = "#id")
    public ListingResponse resolveListing(String id, String userId) {
        Listing listing = listingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Annonce", "id", id));

        // Vérifier que l'utilisateur est le propriétaire de l'annonce
        if (!listing.getFinderUser().getId().equals(userId)) {
            throw new SecurityException("Vous n'êtes pas autorisé à modifier cette annonce");
        }

        listing.setStatus(Listing.ListingStatus.RESOLU);
        Listing resolvedListing = listingRepository.save(listing);

        return modelMapper.mapListingToListingResponse(resolvedListing);
    }

    /**
     * Obtenir les annonces d'un utilisateur
     */
    @Transactional(readOnly = true)
    public Page<ListingResponse> getUserListings(String userId, String status, Pageable pageable) {
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

        return listings.map(modelMapper::mapListingToListingResponse);
    }

    /**
     * Obtenir des annonces similaires
     */
    @Transactional(readOnly = true)
    public List<ListingResponse> getSimilarListings(String id, int limit) {
        Listing referenceListing = listingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Annonce", "id", id));

        List<Listing> similarListings;
        
        // Si on a des coordonnées géographiques, utiliser la recherche avec géolocalisation
        if (referenceListing.getLatitude() != null && referenceListing.getLongitude() != null) {
            similarListings = listingRepository.findSimilarListingsWithLocation(
                id, 
                referenceListing.getCategory().getValue(),
                referenceListing.getLatitude(),
                referenceListing.getLongitude(),
                limit
            );
        } else {
            // Sinon, recherche simple par catégorie
            similarListings = listingRepository.findSimilarListingsByCategory(
                id, 
                referenceListing.getCategory(),
                PageRequest.of(0, limit)
            );
        }

        return similarListings.stream()
            .map(modelMapper::mapListingToListingResponse)
            .collect(Collectors.toList());
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
     * Obtenir les statistiques des annonces
     */
    @Transactional(readOnly = true)
    public long getTotalActiveListings() {
        return listingRepository.countByStatusAndIsModerated(
            Listing.ListingStatus.ACTIVE, true);
    }

    @Transactional(readOnly = true)
    public long getTodayListings() {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);
        
        return listingRepository.countByCreatedAtBetween(startOfDay, endOfDay);
    }
}