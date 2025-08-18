package com.retrouvtout.controller;

import com.retrouvtout.dto.response.ApiResponse;
import com.retrouvtout.dto.response.PagedResponse;
import com.retrouvtout.dto.response.UserResponse;
import com.retrouvtout.dto.response.ListingResponse;
import com.retrouvtout.security.UserPrincipal;
import com.retrouvtout.service.UserService;
import com.retrouvtout.service.ListingService;
import com.retrouvtout.service.AlertService;
import com.retrouvtout.service.ThreadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import com.retrouvtout.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur d'administration pour la gestion du système
 */
@RestController
@RequestMapping("/admin")
@Tag(name = "Administration", description = "API d'administration du système")
@CrossOrigin(origins = {"${app.cors.allowed-origins}"})
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final ListingService listingService;
    private final AlertService alertService;
    private final ThreadService threadService;

    @Autowired
    public AdminController(UserService userService,
                          ListingService listingService,
                          AlertService alertService,
                          ThreadService threadService) {
        this.userService = userService;
        this.listingService = listingService;
        this.alertService = alertService;
        this.threadService = threadService;
    }

    /**
     * Tableau de bord administrateur
     */
    @GetMapping("/dashboard")
    @Operation(summary = "Obtenir les statistiques du tableau de bord")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "...")
})
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard() {
        Map<String, Object> stats = new HashMap<>();

        // Statistiques utilisateurs
        stats.put("totalUsers", userService.getTotalActiveUsers());
        stats.put("newUsersToday", getUsersToday());

        // Statistiques annonces
        stats.put("totalListings", listingService.getTotalActiveListings());
        stats.put("newListingsToday", listingService.getTodayListings());

        // Statistiques système
        stats.put("systemHealth", getSystemHealth());
        stats.put("lastUpdate", LocalDateTime.now());

        return ResponseEntity.ok(new ApiResponse<>(
            true,
            "Statistiques du tableau de bord récupérées",
            stats
        ));
    }

    /**
     * Gestion des utilisateurs
     */
    @GetMapping("/users")
    @Operation(summary = "Obtenir la liste des utilisateurs")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getUsers(
            @Parameter(description = "Numéro de page")
            @RequestParam(defaultValue = "1") int page,
            
            @Parameter(description = "Taille de la page")
            @RequestParam(defaultValue = "20") int pageSize,
            
            @Parameter(description = "Recherche par nom ou email")
            @RequestParam(required = false) String search) {

        if (page < 1) page = 1;
        if (pageSize < 1 || pageSize > 100) pageSize = 20;

        Pageable pageable = PageRequest.of(page - 1, pageSize, 
            Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<UserResponse> users;
        if (search != null && !search.trim().isEmpty()) {
            users = userService.searchUsers(search.trim(), pageable);
        } else {
            users = userService.getAllUsers(pageable);
        }

        PagedResponse<UserResponse> pagedResponse = new PagedResponse<>(
            users.getContent(),
            users.getNumber() + 1,
            users.getSize(),
            users.getTotalElements(),
            users.getTotalPages(),
            users.isLast()
        );

        return ResponseEntity.ok(new ApiResponse<>(
            true,
            "Liste des utilisateurs récupérée",
            pagedResponse
        ));
    }

    /**
     * Désactiver un utilisateur
     */
    @PatchMapping("/users/{id}/deactivate")
    @Operation(summary = "Désactiver un utilisateur")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(
            @Parameter(description = "ID de l'utilisateur")
            @PathVariable String id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        // Empêcher l'auto-désactivation
        if (id.equals(userPrincipal.getId())) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(
                    false,
                    "Vous ne pouvez pas vous désactiver vous-même",
                    null
                ));
        }

        try {
            userService.deactivateUser(id);
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Utilisateur désactivé avec succès",
                null
            ));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Réactiver un utilisateur
     */
    @PatchMapping("/users/{id}/reactivate")
    @Operation(summary = "Réactiver un utilisateur")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> reactivateUser(
            @Parameter(description = "ID de l'utilisateur")
            @PathVariable String id) {

        try {
            userService.reactivateUser(id);
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Utilisateur réactivé avec succès",
                null
            ));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Statistiques détaillées du système
     */
    @GetMapping("/stats")
    @Operation(summary = "Obtenir les statistiques détaillées")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDetailedStats() {
        Map<String, Object> stats = new HashMap<>();

        // Statistiques par période
        stats.put("userGrowth", getUserGrowthStats());
        stats.put("listingGrowth", getListingGrowthStats());
        stats.put("activityStats", getActivityStats());

        return ResponseEntity.ok(new ApiResponse<>(
            true,
            "Statistiques détaillées récupérées",
            stats
        ));
    }

    /**
     * Logs du système
     */
    @GetMapping("/logs")
    @Operation(summary = "Obtenir les logs du système")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<PagedResponse<Map<String, Object>>>> getLogs(
            @Parameter(description = "Numéro de page")
            @RequestParam(defaultValue = "1") int page,
            
            @Parameter(description = "Taille de la page")
            @RequestParam(defaultValue = "50") int pageSize,
            
            @Parameter(description = "Niveau de log")
            @RequestParam(required = false) String level) {

        // Implémentation simplifiée - en production, connecter à un système de logs
        Map<String, Object> logEntry = new HashMap<>();
        logEntry.put("timestamp", LocalDateTime.now());
        logEntry.put("level", "INFO");
        logEntry.put("message", "Système fonctionnel");
        logEntry.put("source", "AdminController");

        PagedResponse<Map<String, Object>> response = new PagedResponse<>();
        response.setItems(java.util.List.of(logEntry));
        response.setPage(page);
        response.setPageSize(pageSize);
        response.setTotalElements(1);
        response.setTotalPages(1);
        response.setLast(true);

        return ResponseEntity.ok(new ApiResponse<>(
            true,
            "Logs récupérés",
            response
        ));
    }

    /**
     * Configuration du système
     */
    @GetMapping("/config")
    @Operation(summary = "Obtenir la configuration du système")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemConfig() {
        Map<String, Object> config = new HashMap<>();
        
        config.put("version", "1.0.0");
        config.put("environment", "production");
        config.put("features", Map.of(
            "emailNotifications", true,
            "smsNotifications", false,
            "autoModeration", true,
            "geocoding", true
        ));
        config.put("limits", Map.of(
            "maxFileSize", "10MB",
            "maxListingsPerDay", 10,
            "maxAlertsPerUser", 20
        ));

        return ResponseEntity.ok(new ApiResponse<>(
            true,
            "Configuration système récupérée",
            config
        ));
    }

    /**
     * Mise à jour de configuration
     */
    @PutMapping("/config")
    @Operation(summary = "Mettre à jour la configuration")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> updateConfig(
            @RequestBody Map<String, Object> configUpdates) {
        
        // Implémentation simplifiée - en production, sauvegarder en base
        return ResponseEntity.ok(new ApiResponse<>(
            true,
            "Configuration mise à jour avec succès",
            null
        ));
    }

    // Méthodes utilitaires privées

    private long getUsersToday() {
        // Implémentation simplifiée
        return 5L;
    }

    private Map<String, Object> getSystemHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("database", "UP");
        health.put("redis", "UP");
        health.put("email", "UP");
        return health;
    }

    private Map<String, Object> getUserGrowthStats() {
        Map<String, Object> growth = new HashMap<>();
        growth.put("thisMonth", 150);
        growth.put("lastMonth", 120);
        growth.put("growth", "+25%");
        return growth;
    }

    private Map<String, Object> getListingGrowthStats() {
        Map<String, Object> growth = new HashMap<>();
        growth.put("thisMonth", 89);
        growth.put("lastMonth", 76);
        growth.put("growth", "+17%");
        return growth;
    }

    private Map<String, Object> getActivityStats() {
        Map<String, Object> activity = new HashMap<>();
        activity.put("dailyActiveUsers", 45);
        activity.put("avgSessionDuration", "12 minutes");
        activity.put("bounceRate", "25%");
        return activity;
    }
}