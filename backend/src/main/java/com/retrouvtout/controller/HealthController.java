package com.retrouvtout.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuator.health.Health;
import org.springframework.boot.actuator.health.HealthIndicator;
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
 */
@RestController
public class HealthController implements HealthIndicator {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * Endpoint de santé public simple
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> simpleHealth() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", System.currentTimeMillis());
        response.put("service", "retrouvtout-api");
        response.put("version", "1.0.0");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Vérification détaillée de la santé
     */
    @Override
    public Health health() {
        Health.Builder builder = Health.up();
        
        // Vérification base de données
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(5)) {
                builder.withDetail("database", "UP");
            } else {
                builder.down().withDetail("database", "DOWN - Connection invalid");
            }
        } catch (Exception e) {
            builder.down().withDetail("database", "DOWN - " + e.getMessage());
        }

        // Vérification Redis
        try {
            redisTemplate.opsForValue().set("health-check", "test");
            String result = (String) redisTemplate.opsForValue().get("health-check");
            if ("test".equals(result)) {
                builder.withDetail("redis", "UP");
                redisTemplate.delete("health-check");
            } else {
                builder.down().withDetail("redis", "DOWN - Test failed");
            }
        } catch (Exception e) {
            builder.down().withDetail("redis", "DOWN - " + e.getMessage());
        }

        // Informations système
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        builder.withDetail("memory", Map.of(
            "max", formatBytes(maxMemory),
            "total", formatBytes(totalMemory),
            "used", formatBytes(usedMemory),
            "free", formatBytes(freeMemory),
            "usage", String.format("%.1f%%", (double) usedMemory / totalMemory * 100)
        ));

        builder.withDetail("disk", Map.of(
            "free", formatBytes(new java.io.File("/").getFreeSpace()),
            "total", formatBytes(new java.io.File("/").getTotalSpace())
        ));

        return builder.build();
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
}

