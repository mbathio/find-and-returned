// AppConstants.java - Constantes de l'application
package com.retrouvtout.util;

/**
 * Constantes de l'application
 */
public final class AppConstants {

    // Configuration pagination
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    public static final int MIN_PAGE_SIZE = 1;

    // Configuration upload
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    public static final String[] ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/png", "image/gif", "image/webp"};
    public static final String[] ALLOWED_IMAGE_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "webp"};

    // Configuration cache
    public static final String CACHE_LISTINGS = "listings";
    public static final String CACHE_USERS = "users";
    public static final String CACHE_THREADS = "threads";
    public static final long CACHE_TTL_MINUTES = 10;

    // Configuration alerts
    public static final double DEFAULT_RADIUS_KM = 10.0;
    public static final double MAX_RADIUS_KM = 100.0;
    public static final int MAX_ALERTS_PER_USER = 20;

    // Configuration messages
    public static final int MAX_MESSAGE_LENGTH = 2000;
    public static final int MAX_THREAD_AGE_DAYS = 90;

    // Configuration modération
    public static final int MAX_LISTINGS_PER_DAY = 10;
    public static final int MAX_MESSAGES_PER_HOUR = 50;

    // Configuration notifications
    public static final int NOTIFICATION_BATCH_SIZE = 100;
    public static final long NOTIFICATION_RETRY_DELAY_MS = 5000;

    // Patterns de validation
    public static final String UUID_REGEX = "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$";
    public static final String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    public static final String PHONE_REGEX = "^(?:\\+33|0)[1-9](?:[0-9]{8})$";

    // Messages par défaut
    public static final String DEFAULT_SUCCESS_MESSAGE = "Opération réussie";
    public static final String DEFAULT_ERROR_MESSAGE = "Une erreur est survenue";
    public static final String DEFAULT_NOT_FOUND_MESSAGE = "Ressource non trouvée";
    public static final String DEFAULT_UNAUTHORIZED_MESSAGE = "Accès non autorisé";

    // Catégories d'objets
    public static final String[] LISTING_CATEGORIES = {
        "cles", "electronique", "bagagerie", "documents", "autre"
    };

    // Statuts d'annonce
    public static final String[] LISTING_STATUSES = {
        "active", "resolu", "suspendu", "supprime"
    };

    // Rôles utilisateur
    public static final String[] USER_ROLES = {
        "retrouveur", "proprietaire", "mixte"
    };

    // Constructeur privé pour empêcher l'instanciation
    private AppConstants() {
        throw new AssertionError("Utility class should not be instantiated");
    }
}