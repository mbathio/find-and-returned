package com.retrouvtout.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Contrôleur pour servir les fichiers statiques uploadés
 */
@RestController
@RequestMapping("/files")
@CrossOrigin(origins = {"${app.cors.allowed-origins}"})
public class FileController {

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    /**
     * Servir les images des annonces
     */
    @GetMapping("/images/{userId}/{filename}")
    public ResponseEntity<Resource> serveImage(
            @PathVariable String userId,
            @PathVariable String filename) {
        
        try {
            Path filePath = Paths.get(uploadDir).resolve("images").resolve(userId).resolve(filename);
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                String contentType = getContentType(filename);
                
                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=31536000") // Cache 1 an
                    .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Servir les images temporaires
     */
    @GetMapping("/temp/{filename}")
    public ResponseEntity<Resource> serveTempImage(@PathVariable String filename) {
        
        try {
            Path filePath = Paths.get(uploadDir).resolve("temp").resolve(filename);
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                String contentType = getContentType(filename);
                
                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache") // Pas de cache pour les fichiers temporaires
                    .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Déterminer le type de contenu basé sur l'extension du fichier
     */
    private String getContentType(String filename) {
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "webp":
                return "image/webp";
            default:
                return "application/octet-stream";
        }
    }
}