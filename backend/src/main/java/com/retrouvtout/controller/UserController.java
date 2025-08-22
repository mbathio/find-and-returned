package com.retrouvtout.controller;

import com.retrouvtout.dto.response.ApiResponse;
import com.retrouvtout.dto.response.PagedResponse;
import com.retrouvtout.dto.response.UserResponse;
import com.retrouvtout.entity.User;
import com.retrouvtout.security.UserPrincipal;
import com.retrouvtout.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
 * ✅ CONTRÔLEUR UTILISATEUR CORRIGÉ - VERSION COMPLÈTE
 * Correction du mapping : /api/users au lieu de /users
 */
@RestController
@RequestMapping("/api/users") // ✅ CORRECTION : /api/users au lieu de /users
@Tag(name = "Users", description = "API de gestion des utilisateurs")
@CrossOrigin(origins = {"*"}) // ✅ CORS permissif en dev
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * ✅ CORRECTION : Obtenir le profil utilisateur avec gestion d'erreur robuste
     */
    @GetMapping("/me")
    @Operation(summary = "Obtenir mon profil")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profil récupéré"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        try {
            // ✅ VALIDATION : Vérifier que l'utilisateur principal existe
            if (userPrincipal == null) {
                System.err.println("❌ getCurrentUser: userPrincipal est null");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Utilisateur non authentifié", null));
            }

            // ✅ VALIDATION : Vérifier que l'ID utilisateur existe
            if (userPrincipal.getId() == null || userPrincipal.getId().isEmpty()) {
                System.err.println("❌ getCurrentUser: userId est null ou vide");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "ID utilisateur invalide", null));
            }

            System.out.println("✅ getCurrentUser: Récupération pour userId = " + userPrincipal.getId());

            UserResponse user = userService.getUserById(userPrincipal.getId());
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Profil récupéré avec succès",
                user
            ));
            
        } catch (Exception e) {
            // ✅ LOG pour debug
            System.err.println("❌ Erreur dans getCurrentUser: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Erreur lors de la récupération du profil", null));
        }
    }

    /**
     * ✅ CORRECTION : Mettre à jour le profil avec validation robuste
     */
    @PutMapping("/me")
    @Operation(summary = "Mettre à jour mon profil")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profil mis à jour"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Données invalides"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<UserResponse>> updateCurrentUser(
            @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        try {
            // ✅ VALIDATION : Vérifier l'authentification
            if (userPrincipal == null || userPrincipal.getId() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Utilisateur non authentifié", null));
            }

            User.UserRole role = null;
            if (request.getRole() != null && !request.getRole().trim().isEmpty()) {
                role = User.UserRole.fromValue(request.getRole());
            }
            
            UserResponse updatedUser = userService.updateUser(
                userPrincipal.getId(),
                request.getName(),
                request.getPhone(),
                role
            );
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Profil mis à jour avec succès",
                updatedUser
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            System.err.println("❌ Erreur dans updateCurrentUser: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Erreur lors de la mise à jour", null));
        }
    }

    /**
     * Changer le mot de passe
     */
    @PutMapping("/me/password")
    @Operation(summary = "Changer mon mot de passe")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Mot de passe changé"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Ancien mot de passe incorrect"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        try {
            // ✅ VALIDATION : Vérifier l'authentification
            if (userPrincipal == null || userPrincipal.getId() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Utilisateur non authentifié", null));
            }

            userService.changePassword(
                userPrincipal.getId(),
                request.getOldPassword(),
                request.getNewPassword()
            );
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Mot de passe changé avec succès",
                null
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            System.err.println("❌ Erreur dans changePassword: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Erreur lors du changement de mot de passe", null));
        }
    }

    /**
     * Obtenir un utilisateur public par son ID
     */
    @GetMapping("/{id}/public")
    @Operation(summary = "Obtenir le profil public d'un utilisateur")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profil public récupéré"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Utilisateur non trouvé")
    })
    public ResponseEntity<ApiResponse<UserResponse>> getPublicUser(
            @Parameter(description = "ID de l'utilisateur")
            @PathVariable String id) {
        
        try {
            UserResponse user = userService.getUserById(id);
            // Masquer les informations sensibles pour la version publique
            user.setEmail(null);
            user.setPhone(null);
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Profil public récupéré",
                user
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(false, "Utilisateur non trouvé", null));
        }
    }

    // ✅ CLASSES INTERNES POUR LES REQUÊTES
    public static class UpdateUserRequest {
        private String name;
        private String phone;
        private String role;

        // Getters et setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    public static class ChangePasswordRequest {
        private String oldPassword;
        private String newPassword;

        // Getters et setters
        public String getOldPassword() { return oldPassword; }
        public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }

        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
}