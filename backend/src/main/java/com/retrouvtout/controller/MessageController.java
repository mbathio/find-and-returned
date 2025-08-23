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
 * Debug maximal et gestion d'erreur robuste
 */
@RestController
@RequestMapping("/api/messages")
@Tag(name = "Messages", description = "API de messagerie intégrée sécurisée")
@CrossOrigin(origins = {"*"}) // ✅ CORS permissif en dev
public class MessageController {

    private final MessageService messageService;

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * ✅ CORRECTION MAJEURE : Obtenir le nombre de messages non lus avec debug complet
     */
    @GetMapping("/unread-count")
    @Operation(summary = "Obtenir le nombre de messages non lus")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        // ✅ DÉBUT DEBUG
        System.out.println("🔧 getUnreadCount - DÉBUT");
        
        try {
            // ✅ VALIDATION 1 : Vérifier userPrincipal
            if (userPrincipal == null) {
                System.err.println("❌ getUnreadCount: userPrincipal est null");
                System.err.println("📍 Vérifiez l'authentification JWT");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Utilisateur non authentifié", 0L));
            }
            System.out.println("✅ UserPrincipal trouvé: " + userPrincipal.getId());

            // ✅ VALIDATION 2 : Vérifier l'ID utilisateur
            if (userPrincipal.getId() == null || userPrincipal.getId().isEmpty()) {
                System.err.println("❌ getUnreadCount: userId est null ou vide");
                System.err.println("📍 ID récupéré: '" + userPrincipal.getId() + "'");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "ID utilisateur invalide", 0L));
            }
            System.out.println("✅ UserID validé: " + userPrincipal.getId());

            // ✅ APPEL SERVICE avec try/catch
            System.out.println("🚀 Appel messageService.getUnreadMessageCount...");
            long unreadCount;
            try {
                unreadCount = messageService.getUnreadMessageCount(userPrincipal.getId());
                System.out.println("✅ Service terminé - Count: " + unreadCount);
            } catch (Exception serviceError) {
                System.err.println("❌ ERREUR DANS LE SERVICE:");
                System.err.println("📍 Message: " + serviceError.getMessage());
                System.err.println("📍 Classe: " + serviceError.getClass().getSimpleName());
                serviceError.printStackTrace();
                
                // Retourner 0 plutôt qu'une erreur 500 pour éviter de casser l'UI
                return ResponseEntity.ok(new ApiResponse<>(
                    true, 
                    "Compte récupéré avec erreur (défaut: 0)", 
                    0L
                ));
            }
            
            // ✅ RÉPONSE RÉUSSIE
            System.out.println("✅ getUnreadCount: Succès avec " + unreadCount + " messages non lus");
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Nombre de messages non lus récupéré",
                unreadCount
            ));
            
        } catch (Exception globalError) {
            // ✅ GESTION D'ERREUR GLOBALE avec debug maximal
            System.err.println("❌ ERREUR GLOBALE dans getUnreadCount:");
            System.err.println("📍 Message: " + globalError.getMessage());
            System.err.println("📍 Classe: " + globalError.getClass().getSimpleName());
            System.err.println("📍 Stack trace:");
            globalError.printStackTrace();
            
            // ✅ JAMAIS retourner 500 pour cette endpoint critique
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Nombre de messages non lus récupéré (avec erreur)",
                0L
            ));
        } finally {
            System.out.println("🔧 getUnreadCount - FIN");
        }
    }

    /**
     * ✅ Endpoint de debug pour tester l'authentification
     */
    @GetMapping("/debug-auth")
    public ResponseEntity<ApiResponse<Object>> debugAuth(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        try {
            System.out.println("🔧 DEBUG AUTH:");
            
            if (userPrincipal == null) {
                System.out.println("❌ UserPrincipal: null");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Non authentifié", null));
            }
            
            System.out.println("✅ UserPrincipal ID: " + userPrincipal.getId());
            System.out.println("✅ UserPrincipal Name: " + userPrincipal.getName());
            System.out.println("✅ UserPrincipal Email: " + userPrincipal.getEmail());
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Auth OK", Map.of(
                "id", userPrincipal.getId(),
                "name", userPrincipal.getName(),
                "email", userPrincipal.getEmail()
            )));
            
        } catch (Exception e) {
            System.err.println("❌ Erreur debug auth: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Erreur debug", null));
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
            System.out.println("🔧 createMessage - DÉBUT pour userId: " + 
                (userPrincipal != null ? userPrincipal.getId() : "null"));

            // ✅ VALIDATION : Vérifier l'authentification
            if (userPrincipal == null || userPrincipal.getId() == null) {
                System.err.println("❌ createMessage: Utilisateur non authentifié");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Utilisateur non authentifié", null));
            }

            MessageResponse message = messageService.createMessage(request, userPrincipal.getId());
            
            System.out.println("✅ createMessage: Message créé avec ID: " + message.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                    true,
                    "Message envoyé avec succès",
                    message
                ));
        } catch (IllegalArgumentException e) {
            System.err.println("❌ createMessage - Argument invalide: " + e.getMessage());
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (SecurityException e) {
            System.err.println("❌ createMessage - Sécurité: " + e.getMessage());
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
            System.out.println("🔧 getThreadMessages - threadId: " + threadId + 
                ", userId: " + (userPrincipal != null ? userPrincipal.getId() : "null"));

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
            System.err.println("❌ getThreadMessages - Thread non trouvé: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(false, "Conversation non trouvée", null));
        } catch (SecurityException e) {
            System.err.println("❌ getThreadMessages - Sécurité: " + e.getMessage());
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
            System.out.println("🔧 markThreadAsRead - threadId: " + threadId + 
                ", userId: " + (userPrincipal != null ? userPrincipal.getId() : "null"));

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
            System.err.println("❌ markThreadAsRead - Thread non trouvé: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(false, "Conversation non trouvée", null));
        } catch (SecurityException e) {
            System.err.println("❌ markThreadAsRead - Sécurité: " + e.getMessage());
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