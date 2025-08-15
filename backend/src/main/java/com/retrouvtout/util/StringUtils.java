// StringUtils.java - Utilitaires de chaînes
package com.retrouvtout.util;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Utilitaires pour les chaînes de caractères
 */
public class StringUtils {

    private static final Pattern DIACRITICS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    
    private static final List<String> FRENCH_STOP_WORDS = Arrays.asList(
        "le", "de", "et", "à", "un", "il", "être", "et", "en", "avoir", "que", "pour",
        "dans", "ce", "son", "une", "sur", "avec", "ne", "se", "pas", "tout", "plus",
        "par", "grand", "en", "me", "même", "te", "des", "ta", "mon", "ton", "nos", "vos"
    );

    /**
     * Supprimer les accents d'une chaîne
     */
    public static String removeAccents(String input) {
        if (input == null) return null;
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return DIACRITICS_PATTERN.matcher(normalized).replaceAll("");
    }

    /**
     * Normaliser une chaîne pour la recherche
     */
    public static String normalizeForSearch(String input) {
        if (input == null) return "";
        return removeAccents(input.toLowerCase().trim())
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ");
    }

    /**
     * Créer un slug URL-friendly
     */
    public static String createSlug(String input) {
        if (input == null) return "";
        return removeAccents(input.toLowerCase())
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("^-+|-+$", "");
    }

    /**
     * Extraire les mots-clés d'un texte
     */
    public static List<String> extractKeywords(String text) {
        if (text == null) return List.of();
        
        return Arrays.stream(normalizeForSearch(text).split("\\s+"))
                .filter(word -> word.length() > 2)
                .filter(word -> !FRENCH_STOP_WORDS.contains(word))
                .distinct()
                .toList();
    }

    /**
     * Tronquer un texte avec ellipses
     */
    public static String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    /**
     * Générer un code aléatoire
     */
    public static String generateRandomCode(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            code.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        
        return code.toString();
    }

    /**
     * Masquer un email partiellement
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        
        String[] parts = email.split("@");
        String local = parts[0];
        String domain = parts[1];
        
        if (local.length() <= 2) {
            return email;
        }
        
        String maskedLocal = local.charAt(0) + "*".repeat(local.length() - 2) + local.charAt(local.length() - 1);
        return maskedLocal + "@" + domain;
    }

    /**
     * Masquer un numéro de téléphone partiellement
     */
    public static String maskPhoneNumber(String phone) {
        if (phone == null || phone.length() < 4) {
            return phone;
        }
        
        String cleaned = phone.replaceAll("[^0-9]", "");
        if (cleaned.length() < 4) {
            return phone;
        }
        
        return cleaned.substring(0, 2) + "*".repeat(cleaned.length() - 4) + cleaned.substring(cleaned.length() - 2);
    }
}