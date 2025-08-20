// src/main/java/com/retrouvtout/controller/ListingController.java
package com.retrouvtout.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Contrôleur simple pour les annonces - version de test
 */
@RestController
@RequestMapping("/listings")
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:3000"})
public class ListingController {

    // Données temporaires en mémoire pour les tests
    private List<Map<String, Object>> mockListings = new ArrayList<>();

    public ListingController() {
        // Initialiser avec quelques données de test
        initializeMockData();
    }

    private void initializeMockData() {
        Map<String, Object> listing1 = new HashMap<>();
        listing1.put("id", "1");
        listing1.put("title", "Trousseau de clés Opel");
        listing1.put("category", "cles");
        listing1.put("locationText", "Gare de Lyon");
        listing1.put("foundAt", "2025-08-05T10:30:00");
        listing1.put("description", "Trouvé près de la sortie 3, porte-clés jaune.");
        listing1.put("status", "active");
        listing1.put("createdAt", LocalDateTime.now().minusDays(2));

        Map<String, Object> listing2 = new HashMap<>();
        listing2.put("id", "2");
        listing2.put("title", "Sac à dos noir");
        listing2.put("category", "bagagerie");
        listing2.put("locationText", "Université Paris Cité");
        listing2.put("foundAt", "2025-08-06T14:15:00");
        listing2.put("description", "Contient des cahiers, sans papiers d'identité visibles.");
        listing2.put("status", "active");
        listing2.put("createdAt", LocalDateTime.now().minusDays(1));

        Map<String, Object> listing3 = new HashMap<>();
        listing3.put("id", "3");
        listing3.put("title", "iPhone 13 bleu");
        listing3.put("category", "electronique");
        listing3.put("locationText", "Tram T3a - Porte de Vincennes");
        listing3.put("foundAt", "2025-08-07T16:45:00");
        listing3.put("description", "Code verrouillé, coque transparente.");
        listing3.put("status", "active");
        listing3.put("createdAt", LocalDateTime.now());

        mockListings.add(listing1);
        mockListings.add(listing2);
        mockListings.add(listing3);
    }

    /**
     * GET /listings - Récupérer toutes les annonces
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllListings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location) {

        List<Map<String, Object>> filteredListings = new ArrayList<>(mockListings);

        // Filtrage simple par catégorie
        if (category != null && !category.isEmpty()) {
            filteredListings = filteredListings.stream()
                .filter(listing -> category.equals(listing.get("category")))
                .toList();
        }

        // Filtrage simple par mot-clé
        if (q != null && !q.isEmpty()) {
            filteredListings = filteredListings.stream()
                .filter(listing -> 
                    listing.get("title").toString().toLowerCase().contains(q.toLowerCase()) ||
                    listing.get("description").toString().toLowerCase().contains(q.toLowerCase()))
                .toList();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", filteredListings);
        response.put("total", filteredListings.size());
        response.put("page", page);
        response.put("size", size);
        response.put("message", "Listings récupérés avec succès");

        return ResponseEntity.ok(response);
    }

    /**
     * GET /listings/{id} - Récupérer une annonce par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getListingById(@PathVariable String id) {
        Optional<Map<String, Object>> listing = mockListings.stream()
            .filter(l -> id.equals(l.get("id")))
            .findFirst();

        if (listing.isPresent()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", listing.get());
            response.put("message", "Annonce trouvée");
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Annonce non trouvée");
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /listings - Créer une nouvelle annonce
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createListing(@RequestBody Map<String, Object> listingData) {
        // Générer un nouvel ID
        String newId = String.valueOf(mockListings.size() + 1);
        listingData.put("id", newId);
        listingData.put("status", "active");
        listingData.put("createdAt", LocalDateTime.now());
        listingData.put("updatedAt", LocalDateTime.now());

        mockListings.add(listingData);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", listingData);
        response.put("message", "Annonce créée avec succès");

        return ResponseEntity.ok(response);
    }

    /**
     * GET /listings/stats - Statistiques simples
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", mockListings.size());
        stats.put("active", mockListings.stream().mapToLong(l -> "active".equals(l.get("status")) ? 1 : 0).sum());
        
        Map<String, Long> categoryCounts = new HashMap<>();
        mockListings.forEach(listing -> {
            String cat = (String) listing.get("category");
            categoryCounts.put(cat, categoryCounts.getOrDefault(cat, 0L) + 1);
        });
        stats.put("byCategory", categoryCounts);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", stats);
        response.put("message", "Statistiques récupérées");

        return ResponseEntity.ok(response);
    }
}