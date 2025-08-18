// UploadController.java
package com.retrouvtout.controller;

import com.retrouvtout.dto.response.ApiResponse;
import com.retrouvtout.security.UserPrincipal;
import com.retrouvtout.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Contrôleur pour l'upload de fichiers
 */
@RestController
@RequestMapping("/upload")
@Tag(name = "Upload", description = "API d'upload de fichiers")
@CrossOrigin(origins = {"${app.cors.allowed-origins}"})
public class UploadController {

    private final FileUploadService fileUploadService;

    @Autowired
    public UploadController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    /**
     * Upload d'image pour les annonces
     */
    @PostMapping("/image")
    @Operation(summary = "Upload d'une image")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Image uploadée avec succès"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Fichier invalide"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Non authentifié")
    })
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        try {
            String imageUrl = fileUploadService.uploadImage(file, userPrincipal.getId());
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Image uploadée avec succès",
                Map.of("url", imageUrl)
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(
                    false,
                    e.getMessage(),
                    null
                ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(
                    false,
                    "Erreur lors de l'upload",
                    null
                ));
        }
    }

    /**
     * Upload temporaire d'image (pour prévisualisation)
     */
    @PostMapping("/temp")
    @Operation(summary = "Upload temporaire d'une image")
    @ApiResponses(value = {
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description =
 "Image uploadée temporairement"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Fichier invalide")
    })
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadTempImage(
            @RequestParam("file") MultipartFile file) {

        try {
            String imageUrl = fileUploadService.uploadTempImage(file);
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Image uploadée temporairement",
                Map.of("url", imageUrl)
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(
                    false,
                    e.getMessage(),
                    null
                ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(
                    false,
                    "Erreur lors de l'upload",
                    null
                ));
        }
    }
}