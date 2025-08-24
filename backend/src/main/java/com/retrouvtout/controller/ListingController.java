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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * ✅ CONTRÔLEUR LISTINGS CORRIGÉ - VERSION AVEC JSON FORCÉ ET DEBUG
 * Correction du mapping : /api/listings au lieu de /listings
 * Contrôleur pour la gestion des annonces conforme au cahier des charges
 * Section 3.2 - Gestion des annonces d'objets retrouvés
 * API EXACTEMENT conforme au frontend (services/listings.ts)
 */
@RestController
@RequestMapping("/api/listings")
@Tag(name = "Listings", description = "API de gestion des annonces d'objets retrouvés")
@CrossOrigin(origins = {"*"}) // ✅ CORS permissif en dev
public class ListingController {

    private final ListingService listingService;

    @Autowired
    public ListingController(ListingService listingService) {
        this.listingService = listingService;
    }

    /**
     * Poster une annonce - Section 3.2
     * Formulaire avec : type d'objet, lieu de découverte, date, photo, description, catégorie
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE) // ✅ FORCER JSON
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
            // ✅ VALIDATION : Vérifier l'authentification
            if (userPrincipal == null || userPrincipal.getId() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Utilisateur non authentifié", null));
            }

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
        } catch (Exception e) {
            System.err.println("❌ Erreur dans createListing: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Erreur lors de la création de l'annonce", null));
        }
    }

    /**
     * Rechercher des annonces - Section 3.2
     * API EXACTEMENT conforme aux paramètres du frontend ListingsSearchParams
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE) // ✅ FORCER JSON
    @Operation(summary = "Rechercher des annonces d'objets retrouvés")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Annonces trouvées")
    })
    public ResponseEntity<ApiResponse<PagedResponse<ListingResponse>>> getListings(
            @Parameter(description = "Mot-clé de recherche")
            @RequestParam(required = false) String q,
            
            @Parameter(description = "Catégorie (cles, electronique, bagagerie, documents, vetements, autre)")
            @RequestParam(required = false) String category,
            
            @Parameter(description = "Lieu de recherche")
            @RequestParam(required = false) String location,
            
            @Parameter(description = "Latitude pour recherche géographique")
            @RequestParam(required = false) BigDecimal lat,
            
            @Parameter(description = "Longitude pour recherche géographique")
            @RequestParam(required = false) BigDecimal lng,
            
            @Parameter(description = "Rayon de recherche en km")
            @RequestParam(required = false) Double radius_km,
            
            @Parameter(description = "Date de début (format YYYY-MM-DD)")
            @RequestParam(required = false) LocalDate date_from,
            
            @Parameter(description = "Date de fin (format YYYY-MM-DD)")
            @RequestParam(required = false) LocalDate date_to,
            
            @Parameter(description = "Numéro de page")
            @RequestParam(defaultValue = "1") int page,
            
            @Parameter(description = "Taille de la page")
            @RequestParam(defaultValue = "20") int page_size) {

        try {
            // 🔍 LOGS DE DEBUG DÉTAILLÉS
            System.out.println("🔍 CONTROLLER getListings - DÉBUT");
            System.out.println("🔍 Paramètres reçus:");
            System.out.println("🔍 - q: " + q);
            System.out.println("🔍 - category: " + category);
            System.out.println("🔍 - location: " + location);
            System.out.println("🔍 - lat: " + lat);
            System.out.println("🔍 - lng: " + lng);
            System.out.println("🔍 - radius_km: " + radius_km);
            System.out.println("🔍 - date_from: " + date_from);
            System.out.println("🔍 - date_to: " + date_to);
            System.out.println("🔍 - page: " + page);
            System.out.println("🔍 - page_size: " + page_size);

            if (page < 1) page = 1;
            if (page_size < 1 || page_size > 100) page_size = 20;

            Pageable pageable = PageRequest.of(page - 1, page_size, 
                Sort.by(Sort.Direction.DESC, "createdAt"));

            System.out.println("🔍 Pageable créé: " + pageable);
            System.out.println("🔍 Appel du service searchListings...");

            PagedResponse<ListingResponse> listings = listingService.searchListings(
                q, category, location, lat, lng, radius_km, date_from, date_to, pageable);

            System.out.println("🔍 SERVICE RETOUR:");
            System.out.println("🔍 - Listings reçus: " + (listings != null ? listings.getItems().size() : "null"));
            System.out.println("🔍 - Total: " + (listings != null ? listings.getTotal() : "null"));
            System.out.println("🔍 - Page: " + (listings != null ? listings.getPage() : "null"));
            System.out.println("🔍 - TotalPages: " + (listings != null ? listings.getTotalPages() : "null"));

            if (listings != null && listings.getItems() != null && !listings.getItems().isEmpty()) {
                System.out.println("🔍 Première annonce:");
                ListingResponse first = listings.getItems().get(0);
                System.out.println("🔍 - ID: " + first.getId());
                System.out.println("🔍 - Title: " + first.getTitle());
                System.out.println("🔍 - Category: " + first.getCategory());
                System.out.println("🔍 - Status: " + first.getStatus());
            }

            ApiResponse<PagedResponse<ListingResponse>> response = new ApiResponse<>(
                true,
                "Annonces récupérées avec succès",
                listings
            );

            System.out.println("🔍 CONTROLLER getListings - FIN AVEC SUCCÈS");
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Erreur dans getListings: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Erreur lors de la recherche", null));
        }
    }

    /**
     * Obtenir une annonce par son ID
     * Format de réponse EXACTEMENT conforme au frontend
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE) // ✅ FORCER JSON
    @Operation(summary = "Obtenir les détails d'une annonce")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Annonce trouvée"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Annonce non trouvée")
    })
    public ResponseEntity<ApiResponse<ListingResponse>> getListing(
            @Parameter(description = "ID de l'annonce")
            @PathVariable String id) {

        try {
            System.out.println("🔍 CONTROLLER getListing - ID: " + id);
            
            // Incrémenter le compteur de vues
            listingService.incrementViewCount(id);
            
            ListingResponse listing = listingService.getListingById(id);
            
            System.out.println("🔍 Annonce trouvée: " + listing.getTitle());
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Annonce trouvée",
                listing
            ));
        } catch (Exception e) {
            System.err.println("❌ Erreur dans getListing: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(
                    false,
                    "Annonce non trouvée",
                    null
                ));
        }
    }

    /**
     * Mettre à jour une annonce (pour le retrouveur uniquement)
     */
    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE) // ✅ FORCER JSON
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
            // ✅ VALIDATION : Vérifier l'authentification
            if (userPrincipal == null || userPrincipal.getId() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Utilisateur non authentifié", null));
            }

            ListingResponse listing = listingService.updateListing(id, request, userPrincipal.getId());
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Annonce mise à jour avec succès",
                listing
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(
                    false,
                    "Annonce non trouvée",
                    null
                ));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>(
                    false,
                    "Vous n'êtes pas autorisé à modifier cette annonce",
                    null
                ));
        } catch (Exception e) {
            System.err.println("❌ Erreur dans updateListing: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Erreur lors de la mise à jour", null));
        }
    }

    /**
     * Supprimer une annonce
     */
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE) // ✅ FORCER JSON
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
            // ✅ VALIDATION : Vérifier l'authentification
            if (userPrincipal == null || userPrincipal.getId() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Utilisateur non authentifié", null));
            }

            listingService.deleteListing(id, userPrincipal.getId());
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Annonce supprimée avec succès",
                null
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(
                    false,
                    "Annonce non trouvée",
                    null
                ));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>(
                    false,
                    "Vous n'êtes pas autorisé à supprimer cette annonce",
                    null
                ));
        } catch (Exception e) {
            System.err.println("❌ Erreur dans deleteListing: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Erreur lors de la suppression", null));
        }
    }
}