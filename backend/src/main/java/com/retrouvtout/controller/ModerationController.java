package com.retrouvtout.controller;

import com.retrouvtout.dto.request.CreateModerationFlagRequest;
import com.retrouvtout.dto.response.ApiResponse;
import com.retrouvtout.dto.response.ModerationFlagResponse;
import com.retrouvtout.dto.response.PagedResponse;
import com.retrouvtout.security.UserPrincipal;
import com.retrouvtout.service.ModerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse;
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
 * Contrôleur pour la modération des contenus
 */
@RestController
@RequestMapping("/moderation")
@Tag(name = "Moderation", description = "API de modération des contenus")
@CrossOrigin(origins = {"${app.cors.allowed-origins}"})
public class ModerationController {

    private final ModerationService moderationService;

    @Autowired
    public ModerationController(ModerationService moderationService) {
        this.moderationService = moderationService;
    }

    /**
     * Signaler un contenu
     */
    @PostMapping("/flags")
    @Operation(summary = "Signaler un contenu")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @SwaggerApiResponse(responseCode = "201", description = "Signalement créé avec succès"),
        @SwaggerApiResponse(responseCode = "400", description = "Données invalides"),
        @SwaggerApiResponse(responseCode = "401", description = "Non authentifié")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ModerationFlagResponse>> createFlag(
            @Valid @RequestBody CreateModerationFlagRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        try {
            ModerationFlagResponse flag = moderationService.createFlag(request, userPrincipal.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                    true,
                    "Signalement créé avec succès",
                    flag
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
     * Obtenir les signalements (modérateurs/admins)
     */
    @GetMapping("/flags")
    @Operation(summary = "Obtenir les signalements")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @SwaggerApiResponse(responseCode = "200", description = "Liste des signalements")
    })
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<ModerationFlagResponse>>> getFlags(
            @Parameter(description = "Statut des signalements")
            @RequestParam(required = false) String status,
            
            @Parameter(description = "Priorité des signalements")
            @RequestParam(required = false) String priority,
            
            @Parameter(description = "Type d'entité")
            @RequestParam(required = false) String entityType,
            
            @Parameter(description = "Numéro de page")
            @RequestParam(defaultValue = "1") int page,
            
            @Parameter(description = "Taille de la page")
            @RequestParam(defaultValue = "20") int pageSize) {

        if (page < 1) page = 1;
        if (pageSize < 1 || pageSize > 100) pageSize = 20;

        Pageable pageable = PageRequest.of(page - 1, pageSize, 
            Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<ModerationFlagResponse> flags = moderationService.getFlags(
            status, priority, entityType, pageable);

        PagedResponse<ModerationFlagResponse> pagedResponse = new PagedResponse<>(
            flags.getContent(),
            flags.getNumber() + 1,
            flags.getSize(),
            flags.getTotalElements(),
            flags.getTotalPages(),
            flags.isLast()
        );

        return ResponseEntity.ok(new ApiResponse<>(
            true,
            "Signalements récupérés avec succès",
            pagedResponse
        ));
    }

    /**
     * Approuver un signalement
     */
    @PatchMapping("/flags/{id}/approve")
    @Operation(summary = "Approuver un signalement")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @SwaggerApiResponse(responseCode = "200", description = "Signalement approuvé"),
        @SwaggerApiResponse(responseCode = "404", description = "Signalement non trouvé")
    })
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ModerationFlagResponse>> approveFlag(
            @Parameter(description = "ID du signalement")
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        try {
            ModerationFlagResponse flag = moderationService.approveFlag(id, userPrincipal.getId());
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Signalement approuvé avec succès",
                flag
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Rejeter un signalement
     */
    @PatchMapping("/flags/{id}/reject")
    @Operation(summary = "Rejeter un signalement")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @SwaggerApiResponse(responseCode = "200", description = "Signalement rejeté"),
        @SwaggerApiResponse(responseCode = "404", description = "Signalement non trouvé")
    })
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ModerationFlagResponse>> rejectFlag(
            @Parameter(description = "ID du signalement")
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        try {
            ModerationFlagResponse flag = moderationService.rejectFlag(id, userPrincipal.getId());
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Signalement rejeté avec succès",
                flag
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtenir les statistiques de modération
     */
    @GetMapping("/stats")
    @Operation(summary = "Obtenir les statistiques de modération")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @SwaggerApiResponse(responseCode = "200", description = "Statistiques récupérées")
    })
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ModerationStatsResponse>> getModerationStats() {
        
        ModerationStatsResponse stats = moderationService.getModerationStats();
        
        return ResponseEntity.ok(new ApiResponse<>(
            true,
            "Statistiques récupérées avec succès",
            stats
        ));
    }

    /**
     * DTO pour les statistiques de modération
     */
    public static class ModerationStatsResponse {
        private long pendingFlags;
        private long approvedFlags;
        private long rejectedFlags;
        private long totalFlags;

        // Constructeurs, getters et setters
        public ModerationStatsResponse() {}

        public ModerationStatsResponse(long pendingFlags, long approvedFlags, long rejectedFlags, long totalFlags) {
            this.pendingFlags = pendingFlags;
            this.approvedFlags = approvedFlags;
            this.rejectedFlags = rejectedFlags;
            this.totalFlags = totalFlags;
        }

        public long getPendingFlags() { return pendingFlags; }
        public void setPendingFlags(long pendingFlags) { this.pendingFlags = pendingFlags; }

        public long getApprovedFlags() { return approvedFlags; }
        public void setApprovedFlags(long approvedFlags) { this.approvedFlags = approvedFlags; }

        public long getRejectedFlags() { return rejectedFlags; }
        public void setRejectedFlags(long rejectedFlags) { this.rejectedFlags = rejectedFlags; }

        public long getTotalFlags() { return totalFlags; }
        public void setTotalFlags(long totalFlags) { this.totalFlags = totalFlags; }
    }
}

// DTO pour créer un signalement
package com.retrouvtout.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateModerationFlagRequest {
    
    @NotNull(message = "Le type d'entité est obligatoire")
    private String entityType;
    
    @NotBlank(message = "L'ID de l'entité est obligatoire")
    private String entityId;
    
    @NotBlank(message = "La raison est obligatoire")
    @Size(max = 255, message = "La raison ne peut pas dépasser 255 caractères")
    private String reason;
    
    @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
    private String description;
    
    private String priority = "medium";
    
    // Constructeurs
    public CreateModerationFlagRequest() {}
    
    // Getters et Setters
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    
    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}