// GeocodingService.java
package com.retrouvtout.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Service de géocodage pour convertir les adresses en coordonnées
 */
@Service
public class GeocodingService {

    @Value("${app.geocoding.enabled:true}")
    private boolean geocodingEnabled;

    @Value("${app.geocoding.provider:nominatim}")
    private String provider;

    @Value("${app.geocoding.api-key:}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public GeocodingService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Géocoder une adresse
     */
    public GeocodingResult geocode(String address) {
        if (!geocodingEnabled || address == null || address.trim().isEmpty()) {
            return null;
        }

        try {
            switch (provider.toLowerCase()) {
                case "nominatim":
                    return geocodeWithNominatim(address);
                case "google":
                    return geocodeWithGoogle(address);
                default:
                    System.err.println("Fournisseur de géocodage non supporté: " + provider);
                    return null;
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du géocodage de '" + address + "': " + e.getMessage());
            return null;
        }
    }

    /**
     * Géocodage avec Nominatim (OpenStreetMap)
     */
    private GeocodingResult geocodeWithNominatim(String address) {
        String url = String.format(
            "https://nominatim.openstreetmap.org/search?q=%s&format=json&limit=1&addressdetails=1",
            address.replace(" ", "%20")
        );

        try {
            Map<String, Object>[] results = restTemplate.getForObject(url, Map[].class);
            
            if (results != null && results.length > 0) {
                Map<String, Object> result = results[0];
                
                double lat = Double.parseDouble(result.get("lat").toString());
                double lon = Double.parseDouble(result.get("lon").toString());
                String displayName = result.get("display_name").toString();

                return new GeocodingResult(
                    BigDecimal.valueOf(lat),
                    BigDecimal.valueOf(lon),
                    displayName
                );
            }
        } catch (Exception e) {
            System.err.println("Erreur Nominatim: " + e.getMessage());
        }

        return null;
    }

    /**
     * Géocodage avec Google Maps (nécessite une clé API)
     */
    private GeocodingResult geocodeWithGoogle(String address) {
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("Clé API Google manquante pour le géocodage");
            return null;
        }

        String url = String.format(
            "https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=%s",
            address.replace(" ", "%20"),
            apiKey
        );

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && "OK".equals(response.get("status"))) {
                @SuppressWarnings("unchecked")
                java.util.List<Map<String, Object>> results = 
                    (java.util.List<Map<String, Object>>) response.get("results");
                
                if (!results.isEmpty()) {
                    Map<String, Object> result = results.get(0);
                    
                    @SuppressWarnings("unchecked")
                    Map<String, Object> geometry = (Map<String, Object>) result.get("geometry");
                    @SuppressWarnings("unchecked")
                    Map<String, Object> location = (Map<String, Object>) geometry.get("location");
                    
                    double lat = (Double) location.get("lat");
                    double lng = (Double) location.get("lng");
                    String formattedAddress = result.get("formatted_address").toString();

                    return new GeocodingResult(
                        BigDecimal.valueOf(lat),
                        BigDecimal.valueOf(lng),
                        formattedAddress
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur Google Geocoding: " + e.getMessage());
        }

        return null;
    }

    /**
     * Classe pour le résultat du géocodage
     */
    public static class GeocodingResult {
        private final BigDecimal latitude;
        private final BigDecimal longitude;
        private final String formattedAddress;

        public GeocodingResult(BigDecimal latitude, BigDecimal longitude, String formattedAddress) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.formattedAddress = formattedAddress;
        }

        public BigDecimal getLatitude() { return latitude; }
        public BigDecimal getLongitude() { return longitude; }
        public String getFormattedAddress() { return formattedAddress; }
    }
}