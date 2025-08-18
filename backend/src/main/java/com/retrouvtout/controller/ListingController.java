package com.retrouvtout.controller;

import com.retrouvtout.dto.request.CreateListingRequest;
import com.retrouvtout.dto.request.UpdateListingRequest;
import com.retrouvtout.dto.response.ApiResponse;
import com.retrouvtout.dto.response.ListingResponse;
import com.retrouvtout.dto.response.PagedResponse;
import com.retrouvtout.entity.Listing;
import com.retrouvtout.security.UserPrincipal;
import com.retrouvtout.service.ListingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import com.retrouvtout.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Contrôleur pour la gestion des annonces d'objets retrouvés
 */
@RestController
@RequestMapping("/listings")
@Tag(name = "Listings", description = "API de gestion des annonces d'objets retrouvés")
@CrossOrigin(origins = {"${app.cors.allowed-origins}"})
public class ListingController {

    private final ListingService listingService;

    @Autowired
    public ListingController(ListingService listingService) {
        this.listingService = listingService;
    }

    /**
     * Recherche d'annonces avec filtres et pagination
     */
    @GetMapping
    @Operation(summary = "Rechercher des annonces d'objets retrouvés")
    @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Liste des annonces trouvées")
    })
    public ResponseEntity<ApiResponse<PagedResponse<ListingResponse>>> searchListings(
            @Parameter(description = "Mot-clé de recherche")
            @RequestParam(required = false) String q,
            
            @Parameter(description = "Catégorie d'objet")
            @RequestParam(required = false) String category,
            
            @Parameter(description = "Texte de localisation")
            @RequestParam(required = false) String location,
            
            @Parameter(description = "Latitude pour recherche géographique")
            @RequestParam(required = false) BigDecimal lat,
            
            @Parameter(description = "Longitude pour recherche géographique")
            @RequestParam(required = false) BigDecimal lng,
            
            @Parameter(description = "Rayon de recherche en kilomètres")
            @RequestParam(required = false, defaultValue = "10") Double radiusKm,
            
            @Parameter(description = "Date de début pour la recherche")
            @RequestParam(required = false) LocalDate dateFrom,
            
            @Parameter(description = "Date de fin pour la recherche")
            @RequestParam(required = false) LocalDate dateTo,
            
            @Parameter(description = "Numéro de page (commence à 1)")
            @RequestParam(defaultValue = "1") int page,
            
            @Parameter(description = "Taille de la page")
            @RequestParam(defaultValue = "20") int pageSize,
            
            @Parameter(description = "Critère de tri")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            
            @Parameter(description = "Direction du tri (ASC/DESC)")
            @RequestParam(defaultValue = "DESC") String sortDir) {

        // Validation des paramètres
        if (page < 1) page = 1;
        if (pageSize < 1 || pageSize > 100) pageSize = 20;

        // Création du Pageable
        Sort.Direction direction = sortDir.equalsIgnoreCase("ASC") ? 
            Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(direction, sortBy));

        // Recherche des annonces
        Page<ListingResponse> listings = listingService.searchListings(
            q, category, location, lat, lng, radiusKm, 
            dateFrom, dateTo, pageable
        );

        PagedResponse<ListingResponse> pagedResponse = new PagedResponse<>(
            listings.getContent(),
            listings.getNumber() + 1,
            listings.getSize(),
            listings.getTotalElements(),
            listings.getTotalPages(),
            listings.isLast()
        );

        return ResponseEntity.ok(new ApiResponse<>(
            true,
            "Recherche effectuée avec succès",
            pagedResponse
        ));
    }

    /**
     * Obtenir une annonce par son ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtenir une annonce par son ID")
    @ApiResponses(value = {
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description =
"Annonce trouvée"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Annonce non trouvée")
    })
    public ResponseEntity<ApiResponse<ListingResponse>> getListing(
            @Parameter(description = "ID de l'annonce")
            @PathVariable String id) {

        try {
            ListingResponse listing = listingService.getListingById(id);
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Annonce trouvée",
                listing
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Créer une nouvelle annonce
     */
    @PostMapping
    @Operation(summary = "Créer une nouvelle annonce d'objet retrouvé")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Annonce créée avec succès"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Données invalides"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ListingResponse>> createListing(
            @Valid @RequestBody CreateListingRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        try {
            ListingResponse listing = listingService.createListing(request, userPrincipal.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                    true,
                    "Annonce créée avec succès",
                    listing
                ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(
                    false,
                    e.getMessage(),
                    null
                ));
        }
    }

    /**
     * Mettre à jour une annonce existante
     */
    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une annonce existante")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description =
"Annonce mise à jour avec succès"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Données invalides"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Non authentifié"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Non autorisé"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Annonce non trouvée")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ListingResponse>> updateListing(
            @Parameter(description = "ID de l'annonce")
            @PathVariable String id,
            @Valid @RequestBody UpdateListingRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        try {
            ListingResponse listing = listingService.updateListing(id, request, userPrincipal.getId());
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Annonce mise à jour avec succès",
                listing
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>(
                    false,
                    "Vous n'êtes pas autorisé à modifier cette annonce",
                    null
                ));
        }
    }

    /**
     * Supprimer une annonce
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une annonce")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description =
 "Annonce supprimée avec succès"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Non authentifié"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Non autorisé"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Annonce non trouvée")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> deleteListing(
            @Parameter(description = "ID de l'annonce")
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        try {
            listingService.deleteListing(id, userPrincipal.getId());
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Annonce supprimée avec succès",
                null
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>(
                    false,
                    "Vous n'êtes pas autorisé à supprimer cette annonce",
                    null
                ));
        }
    }

    /**
     * Marquer une annonce comme résolue
     */
    @PatchMapping("/{id}/resolve")
    @Operation(summary = "Marquer une annonce comme résolue")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description =
"Annonce marquée comme résolue"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Non authentifié"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Non autorisé"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Annonce non trouvée")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ListingResponse>> resolveListing(
            @Parameter(description = "ID de l'annonce")
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        try {
            ListingResponse listing = listingService.resolveListing(id, userPrincipal.getId());
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Annonce marquée comme résolue",
                listing
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>(
                    false,
                    "Vous n'êtes pas autorisé à modifier cette annonce",
                    null
                ));
        }
    }

    /**
     * Obtenir les annonces d'un utilisateur
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Obtenir les annonces d'un utilisateur")
    @ApiResponses(value = {
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = 
 "Liste des annonces de l'utilisateur")
    })
    public ResponseEntity<ApiResponse<PagedResponse<ListingResponse>>> getUserListings(
            @Parameter(description = "ID de l'utilisateur")
            @PathVariable String userId,
            
            @Parameter(description = "Statut des annonces (active, resolu)")
            @RequestParam(required = false) String status,
            
            @Parameter(description = "Numéro de page")
            @RequestParam(defaultValue = "1") int page,
            
            @Parameter(description = "Taille de la page")
            @RequestParam(defaultValue = "20") int pageSize) {

        if (page < 1) page = 1;
        if (pageSize < 1 || pageSize > 100) pageSize = 20;

        Pageable pageable = PageRequest.of(page - 1, pageSize, 
            Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<ListingResponse> listings = listingService.getUserListings(
            userId, status, pageable);

        PagedResponse<ListingResponse> pagedResponse = new PagedResponse<>(
            listings.getContent(),
            listings.getNumber() + 1,
            listings.getSize(),
            listings.getTotalElements(),
            listings.getTotalPages(),
            listings.isLast()
        );

        return ResponseEntity.ok(new ApiResponse<>(
            true,
            "Annonces de l'utilisateur récupérées",
            pagedResponse
        ));
    }

    /**
     * Obtenir les annonces similaires
     */
    @GetMapping("/{id}/similar")
    @Operation(summary = "Obtenir des annonces similaires")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Liste des annonces similaires"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Annonce non trouvée")
    })
    public ResponseEntity<ApiResponse<List<ListingResponse>>> getSimilarListings(
            @Parameter(description = "ID de l'annonce de référence")
            @PathVariable String id,
            
            @Parameter(description = "Nombre maximum d'annonces similaires")
            @RequestParam(defaultValue = "5") int limit) {

        try {
            List<ListingResponse> similarListings = listingService.getSimilarListings(id, limit);
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Annonces similaires trouvées",
                similarListings
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Incrémenter le compteur de vues d'une annonce
     */
    @PostMapping("/{id}/view")
    @Operation(summary = "Incrémenter le compteur de vues")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Vue comptabilisée"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Annonce non trouvée")
    })
    public ResponseEntity<ApiResponse<Void>> incrementViewCount(
            @Parameter(description = "ID de l'annonce")
            @PathVariable String id) {

        try {
            listingService.incrementViewCount(id);
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Vue comptabilisée",
                null
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}