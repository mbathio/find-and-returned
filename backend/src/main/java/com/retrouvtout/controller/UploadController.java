package com.retrouvtout.controller;

import com.retrouvtout.dto.response.ApiResponse;
import com.retrouvtout.security.UserPrincipal;
import com.retrouvtout.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * ✅ CONTRÔLEUR UPLOAD CORRIGÉ - VERSION COMPLÈTE
 * Correction du mapping : /api/upload au lieu de /upload
 * Contrôleur pour l'upload de fichiers conforme au cahier des charges
 * Section 3.2 - Upload de photos pour les annonces
 * Réponse EXACTEMENT conforme au frontend (services/listings.ts)
 */
@RestController
@RequestMapping("/api/upload") // ✅ CORRECTION : /api/upload au lieu de /upload
@Tag(name = "Upload", description = "API d'upload de photos pour annonces")
@CrossOrigin(origins = {"*"}) // ✅ CORS permissif en dev
public class UploadController {

    private final FileUploadService fileUploadService;

    @Autowired
    public UploadController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    /**
     * Upload d'image pour les annonces - Section 3.2
     * Réponse EXACTEMENT conforme au frontend : { url: string }
     */
    @PostMapping("/image")
    @Operation(summary = "Upload d'une photo pour annonce")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Photo uploadée avec succès"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Fichier invalide"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        try {
            // ✅ VALIDATION : Vérifier l'authentification
            if (userPrincipal == null || userPrincipal.getId() == null) {
                System.err.println("❌ uploadImage: userPrincipal est null ou invalide");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Utilisateur non authentifié", null));
            }

            // ✅ VALIDATION : Vérifier le fichier
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Aucun fichier sélectionné", null));
            }

            System.out.println("✅ uploadImage: Upload pour userId = " + userPrincipal.getId() + 
                             ", fichier = " + file.getOriginalFilename() + 
                             ", taille = " + file.getSize() + " bytes");

            String imageUrl = fileUploadService.uploadImage(file, userPrincipal.getId());
            
            // Réponse conforme au frontend : { url: string }
            UploadResponse response = new UploadResponse(imageUrl);
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Photo uploadée avec succès",
                response
            ));
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Validation error dans uploadImage: " + e.getMessage());
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(
                    false,
                    e.getMessage(),
                    null
                ));
        } catch (Exception e) {
            System.err.println("❌ Erreur dans uploadImage: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(
                    false,
                    "Erreur lors de l'upload",
                    null
                ));
        }
    }

    /**
     * Upload temporaire d'une image (pour prévisualisation)
     */
    @PostMapping("/temp")
    @Operation(summary = "Upload temporaire d'une photo")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Photo uploadée temporairement"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Fichier invalide"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadTempImage(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        try {
            // ✅ VALIDATION : Vérifier l'authentification
            if (userPrincipal == null || userPrincipal.getId() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Utilisateur non authentifié", null));
            }

            // ✅ VALIDATION : Vérifier le fichier
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Aucun fichier sélectionné", null));
            }

            String tempImageUrl = fileUploadService.uploadTempImage(file);
            
            // Réponse conforme au frontend : { url: string }
            UploadResponse response = new UploadResponse(tempImageUrl);
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Photo temporaire uploadée avec succès",
                response
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            System.err.println("❌ Erreur dans uploadTempImage: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Erreur lors de l'upload temporaire", null));
        }
    }

    /**
     * ✅ Classe de réponse EXACTEMENT conforme au frontend
     * services/listings.ts attend : { url: string }
     */
    public static class UploadResponse {
        private String url;

        public UploadResponse() {}

        public UploadResponse(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}