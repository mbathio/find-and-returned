package com.retrouvtout.controller;

import com.retrouvtout.dto.request.CreateListingRequest;
import com.retrouvtout.dto.request.UpdateListingRequest;
import com.retrouvtout.dto.response.ApiResponse;
import com.retrouvtout.dto.response.ListingResponse;
import com.retrouvtout.dto.response.PagedResponse;
import com.retrouvtout.security.UserPrincipal;
import com.retrouvtout.service.ListingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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

/**
 * Contrôleur pour la gestion des annonces d'objets retrouvés
 * Conforme au cahier des charges - Section 3.2
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
     * Poster une annonce - Cahier des charges 3.2
     * Formulaire avec : type d'objet, lieu de découverte, date, photo, description, catégorie
     */
    @PostMapping
    @Operation(summary = "Publier un objet retrouvé")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Annonce créée"),
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
                    "Annonce publiée avec succès",
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
     * Rechercher des annonces - Cahier des charges 3.2
     * Moteur de recherche avec filtres : catégorie, date, lieu
     */
    @GetMapping
    @Operation(summary = "Rechercher des annonces d'objets retrouvés")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Annonces trouvées")
    })
    public ResponseEntity<ApiResponse<PagedResponse<ListingResponse>>> searchListings(
            @Parameter(description = "Mot-clé de recherche")
            @RequestParam(required = false) String q,
            
            @Parameter(description = "Catégorie (electronique, cles, vetements, etc.)")
            @RequestParam(required = false) String category,
            
            @Parameter(description = "Lieu de recherche")
            @RequestParam(required = false) String location,
            
            @Parameter(description = "Latitude pour recherche géographique")
            @RequestParam(required = false) BigDecimal lat,
            
            @Parameter(description = "Longitude pour recherche géographique")
            @RequestParam(required = false) BigDecimal lng,
            
            @Parameter(description = "Rayon de recherche en km")
            @RequestParam(required = false) Double radiusKm,
            
            @Parameter(description = "Date de début (format YYYY-MM-DD)")
            @RequestParam(required = false) LocalDate dateFrom,
            
            @Parameter(description = "Date de fin (format YYYY-MM-DD)")
            @RequestParam(required = false) LocalDate dateTo,
            
            @Parameter(description = "Numéro de page")
            @RequestParam(defaultValue = "1") int page,
            
            @Parameter(description = "Taille de la page")
            @RequestParam(defaultValue = "20") int pageSize) {

        if (page < 1) page = 1;
        if (pageSize < 1 || pageSize > 100) pageSize = 20;

        Pageable pageable = PageRequest.of(page - 1, pageSize, 
            Sort.by(Sort.Direction.DESC, "createdAt"));

        PagedResponse<ListingResponse> listings = listingService.searchListings(
            q, category, location, lat, lng, radiusKm, dateFrom, dateTo, pageable);

        return ResponseEntity.ok(new ApiResponse<>(
            true,
            "Annonces récupérées avec succès",
            listings
        ));
    }

    /**
     * Obtenir une annonce par son ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtenir les détails d'une annonce")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Annonce trouvée"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Annonce non trouvée")
    })
    public ResponseEntity<ApiResponse<ListingResponse>> getListing(
            @Parameter(description = "ID de l'annonce")
            @PathVariable String id) {

        try {
            // Incrémenter le compteur de vues
            listingService.incrementViewCount(id);
            
            ListingResponse listing = listingService.getListingById(id);
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Annonce trouvée",
                listing
            ));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Mettre à jour une annonce (pour le retrouveur uniquement)
     */
    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une annonce")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Annonce mise à jour"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Annonce non trouvée"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Non autorisé")
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
     * Supprimer une annonce (soft delete)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une annonce")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Annonce supprimée"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Annonce non trouvée"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Non autorisé")
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
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Annonce résolue"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Annonce non trouvée"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Non autorisé")
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
    @GetMapping("/my")
    @Operation(summary = "Obtenir mes annonces")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Annonces récupérées")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<PagedResponse<ListingResponse>>> getUserListings(
            @Parameter(description = "Statut des annonces (active, resolu, supprime)")
            @RequestParam(required = false) String status,
            
            @Parameter(description = "Numéro de page")
            @RequestParam(defaultValue = "1") int page,
            
            @Parameter(description = "Taille de la page")
            @RequestParam(defaultValue = "20") int pageSize,
            
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        if (page < 1) page = 1;
        if (pageSize < 1 || pageSize > 100) pageSize = 20;

        Pageable pageable = PageRequest.of(page - 1, pageSize, 
            Sort.by(Sort.Direction.DESC, "createdAt"));

        PagedResponse<ListingResponse> listings = listingService.getUserListings(
            userPrincipal.getId(), status, pageable);

        return ResponseEntity.ok(new ApiResponse<>(
            true,
            "Vos annonces récupérées avec succès",
            listings
        ));
    }
}