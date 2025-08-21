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

import java.util.Map;

/**
 * Contrôleur pour l'upload de fichiers conforme au cahier des charges
 * Section 3.2 - Upload de photos pour les annonces
 */
@RestController
@RequestMapping("/upload")
@Tag(name = "Upload", description = "API d'upload de photos pour annonces")
@CrossOrigin(origins = {"${app.cors.allowed-origins}"})
public class UploadController {

    private final FileUploadService fileUploadService;

    @Autowired
    public UploadController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    /**
     * Upload d'image pour les annonces - Section 3.2
     * Photos obligatoires pour les annonces d'objets retrouvés
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
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        try {
            String imageUrl = fileUploadService.uploadImage(file, userPrincipal.getId());
            
            return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Photo uploadée avec succès",
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