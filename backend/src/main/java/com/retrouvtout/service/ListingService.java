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
     * Créer une annonce
     */
    public ListingResponse createListing(CreateListingRequest request, String userId) {
        try {
            User finderUser = userRepository.findByIdAndActiveTrue(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", userId));

            Listing.ListingCategory category = Listing.ListingCategory.fromValue(request.getCategory());

            LocalDateTime foundAt;
            if (request.getFoundAt() != null && !request.getFoundAt().trim().isEmpty()) {
                foundAt = LocalDateTime.parse(request.getFoundAt().trim(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } else {
                foundAt = LocalDateTime.now();
            }

            Listing listing = new Listing();
            listing.setId(java.util.UUID.randomUUID().toString());
            listing.setFinderUser(finderUser);
            listing.setTitle(request.getTitle().trim());
            listing.setCategory(category);
            listing.setLocationText(request.getLocationText().trim());
            listing.setFoundAt(foundAt);
            listing.setDescription(request.getDescription().trim());

            if (request.getLatitude() != null) {
                if (request.getLatitude() instanceof BigDecimal) {
                    listing.setLatitude((BigDecimal) request.getLatitude());
                } else {
                    listing.setLatitude(BigDecimal.valueOf(((Number) request.getLatitude()).doubleValue()));
                }
            }

            if (request.getLongitude() != null) {
                if (request.getLongitude() instanceof BigDecimal) {
                    listing.setLongitude((BigDecimal) request.getLongitude());
                } else {
                    listing.setLongitude(BigDecimal.valueOf(((Number) request.getLongitude()).doubleValue()));
                }
            }

            listing.setImageUrl(request.getImageUrl());
            listing.setStatus(Listing.ListingStatus.ACTIVE);
            listing.setViewsCount(0L);
            listing.setIsModerated(false);

            LocalDateTime now = LocalDateTime.now();
            listing.setCreatedAt(now);
            listing.setUpdatedAt(now);

            Listing savedListing = listingRepository.save(listing);

            ListingResponse response = modelMapper.mapListingToListingResponse(savedListing);

            try {
                triggerNotificationsForNewListing(savedListing);
            } catch (Exception ignored) {}

            return response;

        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Recherche d'annonces actives
     */
    @Transactional(readOnly = true)
    public PagedResponse<ListingResponse> searchListings(String query, String category, String location,
                                               BigDecimal lat, BigDecimal lng, Double radiusKm,
                                               LocalDate dateFrom, LocalDate dateTo,
                                               Pageable pageable) {
        Specification<Listing> spec = Specification.where(null);

        // ✅ Filtre minimal : uniquement les annonces actives
        spec = spec.and((root, q, cb) ->
            cb.equal(root.get("status"), Listing.ListingStatus.ACTIVE));

        // ❌ Supprimé : pas de filtre sur isModerated

        // Mot-clé
        if (query != null && !query.trim().isEmpty()) {
            spec = spec.and((root, q, cb) ->
                cb.or(
                    cb.like(cb.lower(root.get("title")), "%" + query.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("description")), "%" + query.toLowerCase() + "%")
                ));
        }

        // Catégorie
        if (category != null && !category.trim().isEmpty()) {
            spec = spec.and((root, q, cb) ->
                cb.equal(root.get("category"), Listing.ListingCategory.fromValue(category)));
        }

        // Lieu
        if (location != null && !location.trim().isEmpty()) {
            spec = spec.and((root, q, cb) ->
                cb.like(cb.lower(root.get("locationText")), "%" + location.toLowerCase() + "%"));
        }

        // Dates
        if (dateFrom != null) {
            LocalDateTime dateTimeFrom = dateFrom.atStartOfDay();
            spec = spec.and((root, q, cb) ->
                cb.greaterThanOrEqualTo(root.get("foundAt"), dateTimeFrom));
        }

        if (dateTo != null) {
            LocalDateTime dateTimeTo = dateTo.atTime(23, 59, 59);
            spec = spec.and((root, q, cb) ->
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

    @Transactional(readOnly = true)
    public ListingResponse getListingById(String id) {
        Listing listing = listingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Annonce", "id", id));
        return modelMapper.mapListingToListingResponse(listing);
    }

    public ListingResponse updateListing(String id, UpdateListingRequest request, String userId) {
        Listing listing = listingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Annonce", "id", id));

        if (!listing.getFinderUser().getId().equals(userId)) {
            throw new SecurityException("Non autorisé");
        }

        if (request.getTitle() != null) listing.setTitle(request.getTitle());
        if (request.getCategory() != null) listing.setCategory(Listing.ListingCategory.fromValue(request.getCategory()));
        if (request.getLocationText() != null) listing.setLocationText(request.getLocationText());
        if (request.getLatitude() != null) listing.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) listing.setLongitude(request.getLongitude());
        if (request.getFoundAt() != null) listing.setFoundAt(request.getFoundAt());
        if (request.getDescription() != null) listing.setDescription(request.getDescription());
        if (request.getImageUrl() != null) listing.setImageUrl(request.getImageUrl());

        Listing updatedListing = listingRepository.save(listing);
        return modelMapper.mapListingToListingResponse(updatedListing);
    }

    public void deleteListing(String id, String userId) {
        Listing listing = listingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Annonce", "id", id));

        if (!listing.getFinderUser().getId().equals(userId)) {
            throw new SecurityException("Non autorisé");
        }

        listing.setStatus(Listing.ListingStatus.SUPPRIME);
        listingRepository.save(listing);
    }

    @Transactional
    public void incrementViewCount(String id) {
        Listing listing = listingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Annonce", "id", id));
        listing.incrementViewCount();
        listingRepository.save(listing);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ListingResponse> getUserListings(String userId, String status, Pageable pageable) {
        User user = userRepository.findByIdAndActiveTrue(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", userId));

        Page<Listing> listings;
        if (status != null && !status.trim().isEmpty()) {
            Listing.ListingStatus listingStatus = Listing.ListingStatus.fromValue(status);
            listings = listingRepository.findByFinderUserAndStatusOrderByCreatedAtDesc(user, listingStatus, pageable);
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
        } catch (Exception ignored) {}
    }
}
