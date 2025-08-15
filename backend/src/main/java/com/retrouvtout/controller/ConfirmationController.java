package com.retrouvtout.controller;

import com.retrouvtout.dto.response.ApiResponse;
import com.retrouvtout.dto.response.ConfirmationResponse;
import com.retrouvtout.security.UserPrincipal;
import com.retrouvtout.service.ConfirmationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import com.retrouvtout.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur pour la gestion des confirmations de remise d'objets
 */
@RestController
@RequestMapping("/confirmations")
@Tag(name = "Confirmations", description = "API de gestion des confirmations de remise")
@CrossOrigin(origins = {"${app.cors.allowed-origins}"})
public class ConfirmationController {

    private final ConfirmationService confirmationService;

    @Autowired
    public ConfirmationController(ConfirmationService confirmationService) {
        this.confirmationService = confirmationService;
    }

    /**
     * Générer un code de confirmation pour un thread
     */
    @PostMapping("/generate")
    @Operation(summary = "Générer un code de confirmation")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @SwaggerApiResponse(responseCode = "201", description = "Code de confirmation généré"),
        @SwaggerApiResponse(responseCode = "400", description = "Données invalides"),
        @SwaggerApiResponse(responseCode = "401", description = "Non authentifié"),
        @SwaggerApiResponse(responseCode = "403", description = "Non autorisé")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ConfirmationResponse>> generateConfirmation(
            @Parameter(description = "ID du thread")
            @RequestParam String threadId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        try {
            ConfirmationResponse confirmation = confirmationService.generateConfirmation(
                threadId, userPrincipal.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                    true,
                    "Code de confirmation généré avec succès",
                    confirmation
                ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(
                    false,
                    e.getMessage(),
                    null
                ));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>(
                    false,
                    "Vous n'êtes pas autorisé à générer ce code",
                    null
                ));
        }
    }

    /**
     * Valider un code de confirmation
     */
    @PostMapping("/validate")
    @Operation(summary = "Valider un code de confirmation")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description =
"Code validé avec succès"),
        @SwaggerApiResponse(responseCode = "400", description = "Code invalide ou expiré"),
        @SwaggerApiResponse(responseCode = "401", description = "Non authentifié")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ConfirmationResponse>> validateConfirmation(
            @Parameter(description = "Code de confirmation")
            @RequestParam String code,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        try {
            ConfirmationResponse confirmation = confirmationService.validateConfirmation(
                code, userPrincipal.getId());
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Code validé avec succès - objet remis !",
                confirmation
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
     * Obtenir la confirmation d'un thread
     */
    @GetMapping("/thread/{threadId}")
    @Operation(summary = "Obtenir la confirmation d'un thread")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description =
"Confirmation trouvée"),
        @SwaggerApiResponse(responseCode = "404", description = "Confirmation non trouvée"),
        @SwaggerApiResponse(responseCode = "403", description = "Accès non autorisé")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ConfirmationResponse>> getThreadConfirmation(
            @Parameter(description = "ID du thread")
            @PathVariable String threadId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        try {
            ConfirmationResponse confirmation = confirmationService.getThreadConfirmation(
                threadId, userPrincipal.getId());
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Confirmation trouvée",
                confirmation
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>(
                    false,
                    "Vous n'êtes pas autorisé à accéder à cette confirmation",
                    null
                ));
        }
    }
}