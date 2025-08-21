package com.retrouvtout.controller;

import com.retrouvtout.dto.request.CreateMessageRequest;
import com.retrouvtout.dto.response.ApiResponse;
import com.retrouvtout.dto.response.MessageResponse;
import com.retrouvtout.dto.response.PagedResponse;
import com.retrouvtout.security.UserPrincipal;
import com.retrouvtout.service.MessageService;
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

/**
 * Contrôleur pour la messagerie intégrée
 * Conforme au cahier des charges - Section 3.5
 */
@RestController
@RequestMapping("/messages")
@Tag(name = "Messages", description = "API de messagerie intégrée sécurisée")
@CrossOrigin(origins = {"${app.cors.allowed-origins}"})
public class MessageController {

    private final MessageService messageService;

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * Envoyer un message - Cahier des charges 3.5
     */
    @PostMapping
    @Operation(summary = "Envoyer un message via la messagerie intégrée")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Message envoyé"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Données invalides"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Non authentifié"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Non autorisé")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<MessageResponse>> createMessage(
            @Valid @RequestBody CreateMessageRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        try {
            MessageResponse message = messageService.createMessage(request, userPrincipal.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                    true,
                    "Message envoyé avec succès",
                    message
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
                    "Vous n'êtes pas autorisé à envoyer ce message",
                    null
                ));
        }
    }

    /**
     * Obtenir les messages d'une conversation
     */
    @GetMapping("/thread/{threadId}")
    @Operation(summary = "Obtenir les messages d'une conversation")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Messages récupérés"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Conversation non trouvée"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Accès non autorisé")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<PagedResponse<MessageResponse>>> getThreadMessages(
            @Parameter(description = "ID de la conversation")
            @PathVariable String threadId,
            
            @Parameter(description = "Numéro de page")
            @RequestParam(defaultValue = "1") int page,
            
            @Parameter(description = "Taille de la page")
            @RequestParam(defaultValue = "50") int pageSize,
            
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        if (page < 1) page = 1;
        if (pageSize < 1 || pageSize > 100) pageSize = 50;

        try {
            Pageable pageable = PageRequest.of(page - 1, pageSize, 
                Sort.by(Sort.Direction.ASC, "createdAt"));

            PagedResponse<MessageResponse> messages = messageService.getThreadMessages(
                threadId, userPrincipal.getId(), pageable);

            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Messages récupérés avec succès",
                messages
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(
                    false,
                    "Conversation non trouvée",
                    null
                ));
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
     * Marquer les messages comme lus
     */
    @PatchMapping("/thread/{threadId}/read")
    @Operation(summary = "Marquer les messages d'une conversation comme lus")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Messages marqués comme lus"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Conversation non trouvée"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Accès non autorisé")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> markThreadAsRead(
            @Parameter(description = "ID de la conversation")
            @PathVariable String threadId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        try {
            messageService.markThreadAsRead(threadId, userPrincipal.getId());
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Messages marqués comme lus",
                null
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(
                    false,
                    "Conversation non trouvée",
                    null
                ));
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
     * Obtenir le nombre de messages non lus
     */
    @GetMapping("/unread-count")
    @Operation(summary = "Obtenir le nombre de messages non lus")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Nombre de messages non lus")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        long unreadCount = messageService.getUnreadMessageCount(userPrincipal.getId());
        
        return ResponseEntity.ok(new ApiResponse<>(
            true,
            "Nombre de messages non lus récupéré",
            unreadCount
        ));
    }
}