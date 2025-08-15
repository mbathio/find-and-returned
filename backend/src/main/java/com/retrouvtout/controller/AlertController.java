package com.retrouvtout.controller;

import com.retrouvtout.dto.request.CreateAlertRequest;
import com.retrouvtout.dto.response.AlertResponse;
import com.retrouvtout.dto.response.ApiResponse;
import com.retrouvtout.dto.response.PagedResponse;
import com.retrouvtout.security.UserPrincipal;
import com.retrouvtout.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

/**
 * Contrôleur pour la gestion des alertes d'objets perdus
 */
@RestController
@RequestMapping("/alerts")
@Tag(name = "Alerts", description = "API de gestion des alertes d'objets perdus")
@CrossOrigin(origins = {"${app.cors.allowed-origins}"})
public class AlertController {

    private final AlertService alertService;

    @Autowired
    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    /**
     * Créer une nouvelle alerte
     */
    @PostMapping
    @Operation(summary = "Créer une nouvelle alerte")
    @SecurityRequirement(name = "bearerAuth")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Alerte créée avec succès"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Données invalides"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<AlertResponse>> createAlert(
            @Valid @RequestBody CreateAlertRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        try {
            AlertResponse alert = alertService.createAlert(request, userPrincipal.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                    true,
                    "Alerte créée avec succès",
                    alert
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
     * Obtenir les alertes de l'utilisateur connecté
     */
    @GetMapping
    @Operation(summary = "Obtenir mes alertes")
    @SecurityRequirement(name = "bearerAuth")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Liste des alertes")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<PagedResponse<AlertResponse>>> getUserAlerts(
            @Parameter(description = "Numéro de page")
            @RequestParam(defaultValue = "1") int page,
            
            @Parameter(description = "Taille de la page")
            @RequestParam(defaultValue = "20") int pageSize,
            
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        if (page < 1) page = 1;
        if (pageSize < 1 || pageSize > 100) pageSize = 20;

        Pageable pageable = PageRequest.of(page - 1, pageSize, 
            Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<AlertResponse> alerts = alertService.getUserAlerts(userPrincipal.getId(), pageable);

        PagedResponse<AlertResponse> pagedResponse = new PagedResponse<>(
            alerts.getContent(),
            alerts.getNumber() + 1,
            alerts.getSize(),
            alerts.getTotalElements(),
            alerts.getTotalPages(),
            alerts.isLast()
        );

        return ResponseEntity.ok(new ApiResponse<>(
            true,
            "Alertes récupérées avec succès",
            pagedResponse
        ));
    }

    /**
     * Obtenir une alerte par son ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtenir une alerte par son ID")
    @SecurityRequirement(name = "bearerAuth")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Alerte trouvée"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Alerte non trouvée"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Accès non autorisé")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<AlertResponse>> getAlert(
            @Parameter(description = "ID de l'alerte")
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        try {
            AlertResponse alert = alertService.getAlertById(id, userPrincipal.getId());
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Alerte trouvée",
                alert
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>(
                    false,
                    "Vous n'êtes pas autorisé à accéder à cette alerte",
                    null
                ));
        }
    }

    /**
     * Mettre à jour une alerte
     */
    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une alerte")
    @SecurityRequirement(name = "bearerAuth")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Alerte mise à jour"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Données invalides"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Alerte non trouvée"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Accès non autorisé")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<AlertResponse>> updateAlert(
            @Parameter(description = "ID de l'alerte")
            @PathVariable String id,
            @Valid @RequestBody CreateAlertRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        try {
            AlertResponse alert = alertService.updateAlert(id, request, userPrincipal.getId());
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Alerte mise à jour avec succès",
                alert
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>(
                    false,
                    "Vous n'êtes pas autorisé à modifier cette alerte",
                    null
                ));
        }
    }

    /**
     * Supprimer une alerte
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une alerte")
    @SecurityRequirement(name = "bearerAuth")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Alerte supprimée"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Alerte non trouvée"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Accès non autorisé")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> deleteAlert(
            @Parameter(description = "ID de l'alerte")
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        try {
            alertService.deleteAlert(id, userPrincipal.getId());
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Alerte supprimée avec succès",
                null
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>(
                    false,
                    "Vous n'êtes pas autorisé à supprimer cette alerte",
                    null
                ));
        }
    }

    /**
     * Activer/désactiver une alerte
     */
    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Activer/désactiver une alerte")
    @SecurityRequirement(name = "bearerAuth")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Statut de l'alerte modifié"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Alerte non trouvée"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Accès non autorisé")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<AlertResponse>> toggleAlert(
            @Parameter(description = "ID de l'alerte")
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        try {
            AlertResponse alert = alertService.toggleAlert(id, userPrincipal.getId());
            
            String message = alert.getActive() ? 
                "Alerte activée avec succès" : "Alerte désactivée avec succès";
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                message,
                alert
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>(
                    false,
                    "Vous n'êtes pas autorisé à modifier cette alerte",
                    null
                ));
        }
    }
}