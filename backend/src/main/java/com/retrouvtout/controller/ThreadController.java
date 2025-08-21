package com.retrouvtout.controller;

import com.retrouvtout.dto.response.ApiResponse;
import com.retrouvtout.dto.response.PagedResponse;
import com.retrouvtout.dto.response.ThreadResponse;
import com.retrouvtout.security.UserPrincipal;
import com.retrouvtout.service.ThreadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur pour les conversations
 * Conforme au cahier des charges - Section 3.5 (Messagerie intégrée)
 * Permet communication directe via la plateforme
 */
@RestController
@RequestMapping("/threads")
@Tag(name = "Threads", description = "API de gestion des conversations")
@CrossOrigin(origins = {"${app.cors.allowed-origins}"})
public class ThreadController {

    private final ThreadService threadService;

    @Autowired
    public ThreadController(ThreadService threadService) {
        this.threadService = threadService;
    }

    /**
     * Créer une nouvelle conversation - Section 3.5
     * Permet aux propriétaires de contacter les retrouveurs
     */
    @PostMapping
    @Operation(summary = "Créer une nouvelle conversation")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Conversation créée"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Données invalides"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Non authentifié"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conversation déjà existante")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ThreadResponse>> createThread(
            @Parameter(description = "ID de l'annonce")
            @RequestParam String listingId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        try {
            ThreadResponse thread = threadService.createThread(listingId, userPrincipal.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                    true,
                    "Conversation créée avec succès",
                    thread
                ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(
                    false,
                    e.getMessage(),
                    null
                ));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResponse<>(
                    false,
                    e.getMessage(),
                    null
                ));
        }
    }

    /**
     * Obtenir les conversations de l'utilisateur
     * Masquage des informations personnelles - Section 3.4
     */
    @GetMapping
    @Operation(summary = "Obtenir mes conversations")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Liste des conversations")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<PagedResponse<ThreadResponse>>> getUserThreads(
            @Parameter(description = "Statut des conversations")
            @RequestParam(required = false) String status,
            
            @Parameter(description = "Numéro de page")
            @RequestParam(defaultValue = "1") int page,
            
            @Parameter(description = "Taille de la page")
            @RequestParam(defaultValue = "20") int pageSize,
            
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        if (page < 1) page = 1;
        if (pageSize < 1 || pageSize > 100) pageSize = 20;

        Pageable pageable = PageRequest.of(page - 1, pageSize, 
            Sort.by(Sort.Direction.DESC, "lastMessageAt"));

        PagedResponse<ThreadResponse> threads = threadService.getUserThreads(
            userPrincipal.getId(), status, pageable);

        return ResponseEntity.ok(new ApiResponse<>(
            true,
            "Conversations récupérées avec succès",
            threads
        ));
    }

    /**
     * Obtenir une conversation par son ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtenir une conversation par son ID")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Conversation trouvée"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Conversation non trouvée"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Accès non autorisé")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ThreadResponse>> getThread(
            @Parameter(description = "ID de la conversation")
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        try {
            ThreadResponse thread = threadService.getThreadById(id, userPrincipal.getId());
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Conversation trouvée",
                thread
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>(
                    false,
                    "Vous n'êtes pas autorisé à accéder à cette conversation",
                    null
                ));
        }
    }

    /**
     * Fermer une conversation
     */
    @PatchMapping("/{id}/close")
    @Operation(summary = "Fermer une conversation")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Conversation fermée"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Conversation non trouvée"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Accès non autorisé")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ThreadResponse>> closeThread(
            @Parameter(description = "ID de la conversation")
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        try {
            ThreadResponse thread = threadService.closeThread(id, userPrincipal.getId());
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Conversation fermée avec succès",
                thread
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>(
                    false,
                    "Vous n'êtes pas autorisé à fermer cette conversation",
                    null
                ));
        }
    }
}