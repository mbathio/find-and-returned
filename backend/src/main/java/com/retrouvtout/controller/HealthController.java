// backend/src/main/java/com/retrouvtout/controller/HealthController.java
// CORRECTION pour éviter l'erreur 500 sur /health

package com.retrouvtout.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur pour les vérifications de santé du système
 * CORRIGÉ pour éviter les erreurs Redis en développement
 */
@RestController
public class HealthController {

    @Autowired
    private DataSource dataSource;

    // ✅ CORRECTION : Redis optionnel pour éviter les erreurs en dev
    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * Endpoint de santé public simple
     * CORRIGÉ : Plus de dépendance Redis obligatoire
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> simpleHealth() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            response.put("status", "UP");
            response.put("timestamp", System.currentTimeMillis());
            response.put("service", "retrouvtout-api");
            response.put("version", "1.0.0");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("error", e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Vérification détaillée de la santé
     * CORRIGÉ : Gestion d'erreur robuste
     */
    @GetMapping("/health/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealth() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> checks = new HashMap<>();
        
        boolean isHealthy = true;
        
        // Vérification base de données
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(5)) {
                checks.put("database", Map.of("status", "UP"));
            } else {
                checks.put("database", Map.of("status", "DOWN", "error", "Connection invalid"));
                isHealthy = false;
            }
        } catch (Exception e) {
            checks.put("database", Map.of("status", "DOWN", "error", e.getMessage()));
            isHealthy = false;
        }

        // ✅ CORRECTION : Vérification Redis conditionnelle
        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set("health-check", "test");
                String result = (String) redisTemplate.opsForValue().get("health-check");
                if ("test".equals(result)) {
                    checks.put("redis", Map.of("status", "UP"));
                    redisTemplate.delete("health-check");
                } else {
                    checks.put("redis", Map.of("status", "DOWN", "error", "Test failed"));
                    isHealthy = false;
                }
            } catch (Exception e) {
                checks.put("redis", Map.of("status", "DOWN", "error", e.getMessage()));
                // ✅ Ne pas considérer Redis comme critique en dev
                if (!"dev".equals(System.getProperty("spring.profiles.active"))) {
                    isHealthy = false;
                }
            }
        } else {
            checks.put("redis", Map.of("status", "DISABLED", "note", "Redis not configured"));
        }

        // Informations système
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        checks.put("memory", Map.of(
            "max", formatBytes(maxMemory),
            "total", formatBytes(totalMemory),
            "used", formatBytes(usedMemory),
            "free", formatBytes(freeMemory),
            "usage", String.format("%.1f%%", (double) usedMemory / totalMemory * 100)
        ));

        checks.put("disk", Map.of(
            "free", formatBytes(new java.io.File("/").getFreeSpace()),
            "total", formatBytes(new java.io.File("/").getTotalSpace())
        ));

        response.put("status", isHealthy ? "UP" : "DOWN");
        response.put("timestamp", System.currentTimeMillis());
        response.put("service", "retrouvtout-api");
        response.put("version", "1.0.0");
        response.put("checks", checks);
        
        return ResponseEntity.ok(response);
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
}