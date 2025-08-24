// backend/src/main/java/com/retrouvtout/service/ListingService.java - VERSION CORRIGÉE COMPILATION

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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

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
     * ✅ CORRECTION COMPILATION : Créer une annonce avec gestion complète des données
     */
    public ListingResponse createListing(CreateListingRequest request, String userId) {
        System.out.println("🔧 CREATE LISTING - DÉBUT");
        System.out.println("  - UserId: " + userId);
        System.out.println("  - Request data: " + request);
        
        try {
            // 1. ✅ Récupérer l'utilisateur
            User finderUser = userRepository.findByIdAndActiveTrue(userId)
                .orElseThrow(() -> {
                    System.err.println("❌ Utilisateur non trouvé: " + userId);
                    return new ResourceNotFoundException("Utilisateur", "id", userId);
                });
            System.out.println("✅ Utilisateur trouvé: " + finderUser.getName());

            // 2. ✅ Conversion et validation de la catégorie
            Listing.ListingCategory category;
            try {
                category = Listing.ListingCategory.fromValue(request.getCategory());
                System.out.println("✅ Catégorie convertie: " + category.name() + " (" + category.getValue() + ")");
            } catch (Exception e) {
                System.err.println("❌ Erreur conversion catégorie: " + e.getMessage());
                throw new IllegalArgumentException("Catégorie invalide: " + request.getCategory());
            }

            // 3. ✅ CORRECTION : Parsing de la date foundAt depuis le frontend (String -> LocalDateTime)
            LocalDateTime foundAt;
            try {
                if (request.getFoundAt() != null && !request.getFoundAt().trim().isEmpty()) {
                    // ✅ CORRECTION : request.getFoundAt() est un String, pas un LocalDateTime
                    String foundAtStr = request.getFoundAt().trim();
                    foundAt = LocalDateTime.parse(foundAtStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    System.out.println("✅ Date parsée: " + foundAt);
                } else {
                    // Fallback si pas de date fournie
                    foundAt = LocalDateTime.now();
                    System.out.println("⚠️ Date non fournie, utilisation de maintenant: " + foundAt);
                }
            } catch (Exception e) {
                System.err.println("❌ Erreur parsing date: " + e.getMessage());
                System.err.println("📍 Date reçue: '" + request.getFoundAt() + "'");
                throw new IllegalArgumentException("Format de date invalide. Utilisez le format YYYY-MM-DDTHH:mm:ss");
            }

            // 4. ✅ Création de l'entité Listing avec TOUS les champs
            Listing listing = new Listing();
            listing.setId(java.util.UUID.randomUUID().toString()); // ID unique
            listing.setFinderUser(finderUser); // ✅ Utilisateur obligatoire
            listing.setTitle(request.getTitle().trim()); // ✅ Titre obligatoire (String.trim())
            listing.setCategory(category); // ✅ Catégorie convertie
            listing.setLocationText(request.getLocationText().trim()); // ✅ Lieu obligatoire (String.trim())
            listing.setFoundAt(foundAt); // ✅ Date obligatoire (JAMAIS NULL)
            listing.setDescription(request.getDescription().trim()); // ✅ Description obligatoire (String.trim())
            
            // ✅ CORRECTION : Champs optionnels avec conversion correcte
            if (request.getLatitude() != null) {
                // ✅ CORRECTION : request.getLatitude() est déjà un BigDecimal ou Number
                if (request.getLatitude() instanceof BigDecimal) {
                    listing.setLatitude((BigDecimal) request.getLatitude());
                } else {
                    // Si c'est un Number (Double, Float, etc.)
                    listing.setLatitude(BigDecimal.valueOf(((Number) request.getLatitude()).doubleValue()));
                }
            }
            
            if (request.getLongitude() != null) {
                // ✅ CORRECTION : request.getLongitude() est déjà un BigDecimal ou Number
                if (request.getLongitude() instanceof BigDecimal) {
                    listing.setLongitude((BigDecimal) request.getLongitude());
                } else {
                    // Si c'est un Number (Double, Float, etc.)
                    listing.setLongitude(BigDecimal.valueOf(((Number) request.getLongitude()).doubleValue()));
                }
            }
            
            listing.setImageUrl(request.getImageUrl());
            
            // Champs par défaut
            listing.setStatus(Listing.ListingStatus.ACTIVE);
            listing.setViewsCount(0L);
            listing.setIsModerated(false); // Modération manuelle
            
            // Dates d'audit (normalement gérées par @PrePersist mais on s'assure)
            LocalDateTime now = LocalDateTime.now();
            listing.setCreatedAt(now);
            listing.setUpdatedAt(now);

            System.out.println("✅ Entité Listing créée avec tous les champs:");
            System.out.println("  - ID: " + listing.getId());
            System.out.println("  - Title: " + listing.getTitle());
            System.out.println("  - Category: " + listing.getCategory());
            System.out.println("  - FoundAt: " + listing.getFoundAt());
            System.out.println("  - Location: " + listing.getLocationText());

            // 5. ✅ Sauvegarde en base
            Listing savedListing = listingRepository.save(listing);
            System.out.println("✅ Annonce sauvée avec ID: " + savedListing.getId());

            // 6. ✅ Vérification post-sauvegarde
            if (savedListing.getFoundAt() == null) {
                System.err.println("❌ ATTENTION: foundAt est NULL après sauvegarde !");
                throw new RuntimeException("Erreur de sauvegarde: foundAt ne doit pas être null");
            }

            // 7. ✅ Conversion en DTO avec vérification
            ListingResponse response = modelMapper.mapListingToListingResponse(savedListing);
            System.out.println("✅ Mapping réussi - Response ID: " + response.getId());

            // 8. ✅ Déclencher les notifications (optionnel, ne pas faire échouer)
            try {
                triggerNotificationsForNewListing(savedListing);
            } catch (Exception notifError) {
                System.err.println("⚠️ Erreur notifications (non bloquante): " + notifError.getMessage());
            }

            System.out.println("✅ CREATE LISTING - TERMINÉ AVEC SUCCÈS");
            return response;

        } catch (Exception e) {
            System.err.println("❌ Erreur dans createListing: " + e.getMessage());
            e.printStackTrace();
            throw e; // Relancer l'exception pour qu'elle soit traitée par le contrôleur
        }
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
            .map(listing -> {
                try {
                    return modelMapper.mapListingToListingResponse(listing);
                } catch (Exception e) {
                    System.err.println("❌ Erreur mapping listing ID " + listing.getId() + ": " + e.getMessage());
                    return null; // Ou créer un DTO minimal
                }
            })
            .filter(response -> response != null) // Filtrer les nulls
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
     */
    private void triggerNotificationsForNewListing(Listing listing) {
        try {
            List<User> allUsers = userRepository.findAll();
            List<User> interestedUsers = allUsers.stream()
                .filter(user -> user.getRole() == User.UserRole.PROPRIETAIRE && user.getActive())
                .collect(Collectors.toList());
            
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