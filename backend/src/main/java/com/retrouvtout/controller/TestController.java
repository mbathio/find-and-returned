package com.retrouvtout.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur de test pour vérifier que l'API fonctionne
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"*"})
public class TestController {

    /**
     * Endpoint de test simple
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "API Retrouv'Tout fonctionne correctement");
        response.put("timestamp", LocalDateTime.now());
        response.put("version", "1.0.0");
        response.put("environment", "development");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de ping
     */
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }

    /**
     * Endpoint pour tester CORS
     */
    @GetMapping("/cors-test")
    public ResponseEntity<Map<String, Object>> corsTest() {
        Map<String, Object> response = new HashMap<>();
        response.put("cors", "enabled");
        response.put("message", "CORS configuré correctement");
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Test POST
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testPost(@RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "POST test successful");
        response.put("received", body);
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }
}