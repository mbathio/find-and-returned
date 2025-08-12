// DTO Request Classes
package com.retrouvtout.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO pour la demande d'inscription
 */
public class RegisterRequest {
    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 120, message = "Le nom ne peut pas dépasser 120 caractères")
    private String name;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    @Size(max = 190, message = "L'email ne peut pas dépasser 190 caractères")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String password;

    @Size(max = 40, message = "Le téléphone ne peut pas dépasser 40 caractères")
    private String phone;

    private String role; // retrouveur, proprietaire, mixte

    // Constructeurs
    public RegisterRequest() {}

    public RegisterRequest(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    // Getters et Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}

/**
 * DTO pour la demande de connexion
 */
public class LoginRequest {
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    private String password;

    // Constructeurs
    public LoginRequest() {}

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Getters et Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}

/**
 * DTO pour la demande de rafraîchissement de token
 */
public class RefreshTokenRequest {
    @NotBlank(message = "Le token de rafraîchissement est obligatoire")
    private String refreshToken;

    // Constructeurs
    public RefreshTokenRequest() {}

    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    // Getters et Setters
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}

/**
 * DTO pour la création d'une annonce
 */
public class CreateListingRequest {
    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 180, message = "Le titre ne peut pas dépasser 180 caractères")
    private String title;

    @NotBlank(message = "La catégorie est obligatoire")
    private String category;

    @NotBlank(message = "Le lieu est obligatoire")
    @Size(max = 255, message = "Le lieu ne peut pas dépasser 255 caractères")
    private String locationText;

    private BigDecimal latitude;
    private BigDecimal longitude;

    @NotNull(message = "La date de découverte est obligatoire")
    private LocalDateTime foundAt;

    @NotBlank(message = "La description est obligatoire")
    private String description;

    @Size(max = 512, message = "L'URL de l'image ne peut pas dépasser 512 caractères")
    private String imageUrl;

    // Constructeurs
    public CreateListingRequest() {}

    // Getters et Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getLocationText() { return locationText; }
    public void setLocationText(String locationText) { this.locationText = locationText; }

    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }

    public LocalDateTime getFoundAt() { return foundAt; }
    public void setFoundAt(LocalDateTime foundAt) { this.foundAt = foundAt; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}

/**
 * DTO pour la mise à jour d'une annonce
 */
public class UpdateListingRequest {
    @Size(max = 180, message = "Le titre ne peut pas dépasser 180 caractères")
    private String title;

    private String category;

    @Size(max = 255, message = "Le lieu ne peut pas dépasser 255 caractères")
    private String locationText;

    private BigDecimal latitude;
    private BigDecimal longitude;
    private LocalDateTime foundAt;
    private String description;

    @Size(max = 512, message = "L'URL de l'image ne peut pas dépasser 512 caractères")
    private String imageUrl;

    private String status;

    // Constructeurs
    public UpdateListingRequest() {}

    // Getters et Setters (similaires à CreateListingRequest)
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getLocationText() { return locationText; }
    public void setLocationText(String locationText) { this.locationText = locationText; }

    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }

    public LocalDateTime getFoundAt() { return foundAt; }
    public void setFoundAt(LocalDateTime foundAt) { this.foundAt = foundAt; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

/**
 * DTO pour la mise à jour d'un utilisateur
 */
public class UpdateUserRequest {
    @Size(max = 120, message = "Le nom ne peut pas dépasser 120 caractères")
    private String name;

    @Size(max = 40, message = "Le téléphone ne peut pas dépasser 40 caractères")
    private String phone;

    private String role; // retrouveur, proprietaire, mixte

    // Constructeurs
    public UpdateUserRequest() {}

    // Getters et Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}

// DTO Response Classes
package com.retrouvtout.dto.response;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTO de réponse générique pour l'API
 */
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    // Constructeurs
    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    // Getters et Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}

/**
 * DTO de réponse pour l'authentification
 */
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
    private UserResponse user;

    // Constructeurs
    public AuthResponse() {}

    public AuthResponse(String accessToken, String refreshToken, String tokenType, long expiresIn, UserResponse user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.user = user;
    }

    // Getters et Setters
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }

    public UserResponse getUser() { return user; }
    public void setUser(UserResponse user) { this.user = user; }
}

/**
 * DTO de réponse pour un utilisateur
 */
public class UserResponse {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String role;
    private boolean emailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructeurs
    public UserResponse() {}

    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

/**
 * DTO de réponse pour une annonce
 */
public class ListingResponse {
    private String id;
    private String finderUserId;
    private String finderUserName;
    private String title;
    private String category;
    private String locationText;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private LocalDateTime foundAt;
    private String description;
    private String imageUrl;
    private String status;
    private long viewsCount;
    private boolean isModerated;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructeurs
    public ListingResponse() {}

    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFinderUserId() { return finderUserId; }
    public void setFinderUserId(String finderUserId) { this.finderUserId = finderUserId; }

    public String getFinderUserName() { return finderUserName; }
    public void setFinderUserName(String finderUserName) { this.finderUserName = finderUserName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getLocationText() { return locationText; }
    public void setLocationText(String locationText) { this.locationText = locationText; }

    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }

    public LocalDateTime getFoundAt() { return foundAt; }
    public void setFoundAt(LocalDateTime foundAt) { this.foundAt = foundAt; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getViewsCount() { return viewsCount; }
    public void setViewsCount(long viewsCount) { this.viewsCount = viewsCount; }

    public boolean isModerated() { return isModerated; }
    public void setModerated(boolean moderated) { isModerated = moderated; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

/**
 * DTO de réponse paginée
 */
public class PagedResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;
    private boolean first;
    private boolean empty;

    // Constructeurs
    public PagedResponse() {}

    public PagedResponse(List<T> content, int page, int size, long totalElements, int totalPages, boolean last) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.last = last;
        this.first = page == 1;
        this.empty = content == null || content.isEmpty();
    }

    // Getters et Setters
    public List<T> getContent() { return content; }
    public void setContent(List<T> content) { this.content = content; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public boolean isLast() { return last; }
    public void setLast(boolean last) { this.last = last; }

    public boolean isFirst() { return first; }
    public void setFirst(boolean first) { this.first = first; }

    public boolean isEmpty() { return empty; }
    public void setEmpty(boolean empty) { this.empty = empty; }
}

/**
 * DTO de réponse pour les statistiques utilisateur
 */
public class UserStatsResponse {
    private long totalListings;
    private long activeListings;
    private long resolvedListings;
    private long totalThreads;
    private long activeAlerts;

    // Constructeurs
    public UserStatsResponse() {}

    public UserStatsResponse(long totalListings, long activeListings, long resolvedListings, 
                           long totalThreads, long activeAlerts) {
        this.totalListings = totalListings;
        this.activeListings = activeListings;
        this.resolvedListings = resolvedListings;
        this.totalThreads = totalThreads;
        this.activeAlerts = activeAlerts;
    }

    // Getters et Setters
    public long getTotalListings() { return totalListings; }
    public void setTotalListings(long totalListings) { this.totalListings = totalListings; }

    public long getActiveListings() { return activeListings; }
    public void setActiveListings(long activeListings) { this.activeListings = activeListings; }

    public long getResolvedListings() { return resolvedListings; }
    public void setResolvedListings(long resolvedListings) { this.resolvedListings = resolvedListings; }

    public long getTotalThreads() { return totalThreads; }
    public void setTotalThreads(long totalThreads) { this.totalThreads = totalThreads; }

    public long getActiveAlerts() { return activeAlerts; }
    public void setActiveAlerts(long activeAlerts) { this.activeAlerts = activeAlerts; }
}