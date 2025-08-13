package com.retrouvtout.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Service pour l'upload et la gestion des fichiers
 */
@Service
public class FileUploadService {

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @Value("${app.upload.max-file-size:10485760}") // 10MB par défaut
    private long maxFileSize;

    @Value("${app.upload.allowed-extensions:jpg,jpeg,png,gif,webp}")
    private String allowedExtensions;

    @Value("${server.servlet.context-path:/api}")
    private String contextPath;

    @Value("${server.port:8081}")
    private String serverPort;

    private static final String TEMP_DIR = "temp";
    private static final String IMAGES_DIR = "images";

    /**
     * Upload d'une image pour un utilisateur authentifié
     */
    public String uploadImage(MultipartFile file, String userId) throws IOException {
        validateFile(file);
        
        // Créer le répertoire utilisateur
        Path userDir = createUserDirectory(userId);
        
        // Générer un nom de fichier unique
        String filename = generateUniqueFilename(file.getOriginalFilename());
        
        // Sauvegarder le fichier
        Path filePath = userDir.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Retourner l'URL publique
        return generatePublicUrl(userId, filename);
    }

    /**
     * Upload temporaire d'une image (pour prévisualisation)
     */
    public String uploadTempImage(MultipartFile file) throws IOException {
        validateFile(file);
        
        // Créer le répertoire temporaire
        Path tempDir = createTempDirectory();
        
        // Générer un nom de fichier unique
        String filename = generateUniqueFilename(file.getOriginalFilename());
        
        // Sauvegarder le fichier temporairement
        Path filePath = tempDir.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Retourner l'URL temporaire
        return generateTempUrl(filename);
    }

    /**
     * Déplacer un fichier temporaire vers le répertoire utilisateur
     */
    public String moveTempToUser(String tempUrl, String userId) throws IOException {
        // Extraire le nom du fichier de l'URL temporaire
        String filename = extractFilenameFromUrl(tempUrl);
        
        Path tempFile = Paths.get(uploadDir, TEMP_DIR, filename);
        if (!Files.exists(tempFile)) {
            throw new IllegalArgumentException("Fichier temporaire non trouvé");
        }
        
        // Créer le répertoire utilisateur
        Path userDir = createUserDirectory(userId);
        
        // Déplacer le fichier
        Path newPath = userDir.resolve(filename);
        Files.move(tempFile, newPath, StandardCopyOption.REPLACE_EXISTING);
        
        return generatePublicUrl(userId, filename);
    }

    /**
     * Supprimer un fichier
     */
    public void deleteFile(String fileUrl) {
        try {
            String relativePath = extractRelativePathFromUrl(fileUrl);
            Path filePath = Paths.get(uploadDir, relativePath);
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la suppression du fichier " + fileUrl + ": " + e.getMessage());
        }
    }

    /**
     * Nettoyer les fichiers temporaires anciens
     */
    public void cleanupTempFiles() {
        try {
            Path tempDir = Paths.get(uploadDir, TEMP_DIR);
            if (!Files.exists(tempDir)) {
                return;
            }

            long cutoffTime = System.currentTimeMillis() - (24 * 60 * 60 * 1000); // 24 heures

            Files.list(tempDir)
                .filter(Files::isRegularFile)
                .filter(path -> {
                    try {
                        return Files.getLastModifiedTime(path).toMillis() < cutoffTime;
                    } catch (IOException e) {
                        return false;
                    }
                })
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        System.err.println("Erreur lors de la suppression du fichier temporaire: " + e.getMessage());
                    }
                });
        } catch (Exception e) {
            System.err.println("Erreur lors du nettoyage des fichiers temporaires: " + e.getMessage());
        }
    }

    // Méthodes privées utilitaires

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier est vide");
        }

        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("Le fichier est trop volumineux (max " + (maxFileSize / 1024 / 1024) + "MB)");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Nom de fichier invalide");
        }

        String extension = getFileExtension(filename).toLowerCase();
        List<String> allowedExts = Arrays.asList(allowedExtensions.toLowerCase().split(","));
        
        if (!allowedExts.contains(extension)) {
            throw new IllegalArgumentException("Type de fichier non autorisé. Extensions autorisées: " + allowedExtensions);
        }

        // Vérification du type MIME
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Le fichier doit être une image");
        }
    }

    private Path createUserDirectory(String userId) throws IOException {
        Path userDir = Paths.get(uploadDir, IMAGES_DIR, userId);
        Files.createDirectories(userDir);
        return userDir;
    }

    private Path createTempDirectory() throws IOException {
        Path tempDir = Paths.get(uploadDir, TEMP_DIR);
        Files.createDirectories(tempDir);
        return tempDir;
    }

    private String generateUniqueFilename(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + extension;
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }

    private String generatePublicUrl(String userId, String filename) {
        return String.format("http://localhost:%s%s/files/images/%s/%s", 
            serverPort, contextPath, userId, filename);
    }

    private String generateTempUrl(String filename) {
        return String.format("http://localhost:%s%s/files/temp/%s", 
            serverPort, contextPath, filename);
    }

    private String extractFilenameFromUrl(String url) {
        return url.substring(url.lastIndexOf('/') + 1);
    }

    private String extractRelativePathFromUrl(String url) {
        // Extraire le chemin relatif à partir de l'URL
        String baseUrl = String.format("http://localhost:%s%s/files/", serverPort, contextPath);
        if (url.startsWith(baseUrl)) {
            return url.substring(baseUrl.length());
        }
        throw new IllegalArgumentException("URL de fichier invalide");
    }
}