// src/main/java/com/retrouvtout/controller/TestController.java
package com.retrouvtout.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur de test pour vérifier la connectivité API
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:3000", "http://localhost:5173"})
public class TestController {

    /**
     * Endpoint de test simple
     * GET /api/test
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Backend Retrouv'Tout fonctionne correctement !");
        response.put("timestamp", LocalDateTime.now());
        response.put("version", "1.0.0");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de santé
     * GET /api/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "retrouvtout-api");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Echo endpoint pour tester les paramètres
     * GET /api/echo?message=test
     */
    @GetMapping("/echo")
    public ResponseEntity<Map<String, Object>> echo(@RequestParam(defaultValue = "Hello World") String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("echo", message);
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }
}