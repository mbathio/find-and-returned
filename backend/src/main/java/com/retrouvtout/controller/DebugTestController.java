package com.retrouvtout.controller;

import com.retrouvtout.dto.request.RegisterRequest;
import com.retrouvtout.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * ✅ CONTRÔLEUR DE DEBUG POUR IDENTIFIER LES PROBLÈMES
 * À supprimer une fois les problèmes résolus
 */
@RestController
@RequestMapping("/api/debug")
@CrossOrigin(origins = {"*"})
public class DebugTestController {

    /**
     * Test de réception des données d'inscription
     */
    @PostMapping("/register-test")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testRegisterData(
            @RequestBody RegisterRequest registerRequest) {
        
        try {
            System.out.println("🔧 TEST REGISTER - Données reçues:");
            System.out.println("  - Nom: '" + registerRequest.getName() + "'");
            System.out.println("  - Email: '" + registerRequest.getEmail() + "'");
            System.out.println("  - Mot de passe: '" + (registerRequest.getPassword() != null ? "[DÉFINI - " + registerRequest.getPassword().length() + " chars]" : "[NULL]") + "'");
            System.out.println("  - Téléphone: '" + registerRequest.getPhone() + "'");
            System.out.println("  - Rôle: '" + registerRequest.getRole() + "'");
            
            Map<String, Object> result = new HashMap<>();
            result.put("name", registerRequest.getName());
            result.put("email", registerRequest.getEmail());
            result.put("passwordLength", registerRequest.getPassword() != null ? registerRequest.getPassword().length() : 0);
            result.put("phone", registerRequest.getPhone());
            result.put("role", registerRequest.getRole());
            result.put("status", "Données reçues correctement");
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Test réussi", result));
            
        } catch (Exception e) {
            System.err.println("❌ ERREUR DANS TEST REGISTER: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("class", e.getClass().getSimpleName());
            
            return ResponseEntity.status(500)
                .body(new ApiResponse<>(false, "Erreur dans le test", error));
        }
    }

    /**
     * Test de validation des données
     */
    @PostMapping("/validate-test")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testValidation(
            @RequestBody Map<String, Object> rawData) {
        
        try {
            System.out.println("🔧 TEST VALIDATION - Données brutes reçues:");
            rawData.forEach((key, value) -> {
                System.out.println("  - " + key + ": '" + value + "' (" + (value != null ? value.getClass().getSimpleName() : "null") + ")");
            });
            
            Map<String, Object> result = new HashMap<>();
            result.put("receivedFields", rawData.keySet());
            result.put("totalFields", rawData.size());
            result.put("status", "Données brutes reçues");
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Validation réussie", result));
            
        } catch (Exception e) {
            System.err.println("❌ ERREUR DANS TEST VALIDATION: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("class", e.getClass().getSimpleName());
            
            return ResponseEntity.status(500)
                .body(new ApiResponse<>(false, "Erreur dans la validation", error));
        }
    }

    /**
     * Test de connectivité simple
     */
    @GetMapping("/ping")
    public ResponseEntity<ApiResponse<String>> ping() {
        System.out.println("🔧 PING reçu");
        return ResponseEntity.ok(new ApiResponse<>(true, "Pong", "Debug controller actif"));
    }

    /**
     * Test des en-têtes de requête
     */
    @PostMapping("/headers-test")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testHeaders(
            @RequestHeader Map<String, String> headers,
            @RequestBody(required = false) String body) {
        
        System.out.println("🔧 TEST HEADERS:");
        headers.forEach((key, value) -> {
            System.out.println("  - " + key + ": " + value);
        });
        
        System.out.println("🔧 BODY LENGTH: " + (body != null ? body.length() : 0));
        
        Map<String, Object> result = new HashMap<>();
        result.put("headers", headers);
        result.put("bodyLength", body != null ? body.length() : 0);
        result.put("contentType", headers.get("content-type"));
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Headers analysés", result));
    }
}