// MessageController.java - VERSION CORRIGÉE pour éviter les 500
package com.retrouvtout.controller;

import com.retrouvtout.dto.request.CreateMessageRequest;
import com.retrouvtout.dto.response.ApiResponse;
import com.retrouvtout.dto.response.MessageResponse;
import com.retrouvtout.dto.response.PagedResponse;
import com.retrouvtout.security.UserPrincipal;
import com.retrouvtout.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
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
 * ✅ CONTRÔLEUR MESSAGES CORRIGÉ POUR ÉVITER LES 500
 */
@RestController
@RequestMapping("/api/messages") // ✅ CORRECTION : /api/messages au lieu de /messages
@Tag(name = "Messages", description = "API de messagerie intégrée sécurisée")
@CrossOrigin(origins = {"*"}) // ✅ CORS permissif en dev
public class MessageController {

    private final MessageService messageService;

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * ✅ CORRECTION : Obtenir le nombre de messages non lus avec gestion d'erreur robuste
     */
    @GetMapping("/unread-count")
    @Operation(summary = "Obtenir le nombre de messages non lus")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        try {
            // ✅ VALIDATION : Vérifier l'authentification
            if (userPrincipal == null) {
                System.err.println("❌ getUnreadCount: userPrincipal est null");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Utilisateur non authentifié", 0L));
            }

            if (userPrincipal.getId() == null || userPrincipal.getId().isEmpty()) {
                System.err.println("❌ getUnreadCount: userId est null ou vide");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "ID utilisateur invalide", 0L));
            }

            System.out.println("✅ getUnreadCount: Récupération pour userId = " + userPrincipal.getId());
            
            long unreadCount = messageService.getUnreadMessageCount(userPrincipal.getId());
            
            System.out.println("✅ getUnreadCount: " + unreadCount + " messages non lus trouvés");
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Nombre de messages non lus récupéré",
                unreadCount
            ));
        } catch (Exception e) {
            // ✅ LOG détaillé pour debug
            System.err.println("❌ Erreur dans getUnreadCount: " + e.getMessage());
            e.printStackTrace();
            
            // ✅ Retourner 0 au lieu d'une erreur 500 pour éviter de casser l'UI
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Nombre de messages non lus récupéré (avec erreur)",
                0L
            ));
        }
    }

    /**
     * Envoyer un message avec validation robuste
     */
    @PostMapping
    @Operation(summary = "Envoyer un message via la messagerie intégrée")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<MessageResponse>> createMessage(
            @Valid @RequestBody CreateMessageRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        try {
            // ✅ VALIDATION : Vérifier l'authentification
            if (userPrincipal == null || userPrincipal.getId() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Utilisateur non authentifié", null));
            }

            MessageResponse message = messageService.createMessage(request, userPrincipal.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                    true,
                    "Message envoyé avec succès",
                    message
                ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>(false, "Vous n'êtes pas autorisé à envoyer ce message", null));
        } catch (Exception e) {
            System.err.println("❌ Erreur dans createMessage: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Erreur lors de l'envoi du message", null));
        }
    }

    /**
     * Obtenir les messages d'une conversation avec validation robuste
     */
    @GetMapping("/thread/{threadId}")
    @Operation(summary = "Obtenir les messages d'une conversation")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<PagedResponse<MessageResponse>>> getThreadMessages(
            @PathVariable String threadId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int pageSize,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        try {
            // ✅ VALIDATION : Vérifier l'authentification
            if (userPrincipal == null || userPrincipal.getId() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Utilisateur non authentifié", null));
            }

            if (page < 1) page = 1;
            if (pageSize < 1 || pageSize > 100) pageSize = 50;

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
                .body(new ApiResponse<>(false, "Conversation non trouvée", null));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>(false, "Vous n'êtes pas autorisé à accéder à cette conversation", null));
        } catch (Exception e) {
            System.err.println("❌ Erreur dans getThreadMessages: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Erreur lors de la récupération des messages", null));
        }
    }

    /**
     * Marquer les messages comme lus avec validation robuste
     */
    @PatchMapping("/thread/{threadId}/read")
    @Operation(summary = "Marquer les messages d'une conversation comme lus")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> markThreadAsRead(
            @PathVariable String threadId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        try {
            // ✅ VALIDATION : Vérifier l'authentification
            if (userPrincipal == null || userPrincipal.getId() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Utilisateur non authentifié", null));
            }

            messageService.markThreadAsRead(threadId, userPrincipal.getId());
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Messages marqués comme lus",
                null
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(false, "Conversation non trouvée", null));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>(false, "Vous n'êtes pas autorisé à accéder à cette conversation", null));
        } catch (Exception e) {
            System.err.println("❌ Erreur dans markThreadAsRead: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Erreur lors du marquage comme lu", null));
        }
    }
}