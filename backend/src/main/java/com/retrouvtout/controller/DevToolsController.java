package com.retrouvtout.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/**
 * Contrôleur pour gérer les requêtes Chrome DevTools
 * Évite les erreurs 404 pour /.well-known/appspecific/com.chrome.devtools.json
 */
@RestController
public class DevToolsController {

    /**
     * Gérer les requêtes Chrome DevTools sans erreur 404
     * Chrome DevTools utilise ce endpoint pour la fonctionnalité "Automatic Workspace Folders"
     */
    @GetMapping("/.well-known/appspecific/com.chrome.devtools.json")
    public ResponseEntity<Map<String, Object>> chromeDevTools() {
        // Option 1: Retourner une configuration valide pour DevTools
        Map<String, Object> workspace = Map.of(
            "root", System.getProperty("user.dir"),
            "uuid", "retrouvtout-dev-" + UUID.randomUUID().toString().substring(0, 8)
        );
        
        Map<String, Object> response = Map.of("workspace", workspace);
        return ResponseEntity.ok(response);
        
        // Option 2: Simplement retourner 204 No Content pour ignorer
        // return ResponseEntity.noContent().build();
        
        // Option 3: Retourner 404 proprement sans stack trace
        // return ResponseEntity.notFound().build();
    }
}