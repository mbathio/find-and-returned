package com.retrouvtout.service;

import com.retrouvtout.dto.request.LoginRequest;
import com.retrouvtout.dto.request.RegisterRequest;
import com.retrouvtout.dto.response.AuthResponse;
import com.retrouvtout.entity.User;
import com.retrouvtout.repository.UserRepository;
import com.retrouvtout.security.JwtTokenProvider;
import com.retrouvtout.util.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Service pour l'authentification et l'autorisation
 */
@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final EmailService emailService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ModelMapper modelMapper;

    @Value("${app.jwt.expiration}")
    private long jwtExpirationInMs;

    @Value("${app.jwt.refresh-expiration}")
    private long refreshTokenExpirationInMs;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Autowired
    public AuthService(UserRepository userRepository,
                      UserService userService,
                      PasswordEncoder passwordEncoder,
                      JwtTokenProvider tokenProvider,
                      EmailService emailService,
                      RedisTemplate<String, Object> redisTemplate,
                      ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.emailService = emailService;
        this.redisTemplate = redisTemplate;
        this.modelMapper = modelMapper;
    }

    /**
     * Inscription d'un nouvel utilisateur
     */
    public AuthResponse register(RegisterRequest request, String clientIp) {
        // Validation des données
        validateRegisterRequest(request);

        // Vérifier si l'email existe déjà
        if (userService.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà");
        }

        // Créer l'utilisateur
        User.UserRole role = request.getRole() != null ? 
            User.UserRole.fromValue(request.getRole()) : User.UserRole.MIXTE;

        User user = userService.createUser(
            request.getName(),
            request.getEmail(),
            request.getPassword(),
            role
        );

        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            user.setPhone(request.getPhone().trim());
            userRepository.save(user);
        }

        // Générer les tokens
        String accessToken = tokenProvider.generateToken(user.getId());
        String refreshToken = tokenProvider.generateRefreshToken(user.getId());

        // Stocker le refresh token en cache
        storeRefreshToken(user.getId(), refreshToken);

        // Logger la connexion
        logAuthEvent(user.getId(), "REGISTER", clientIp, true);

        return new AuthResponse(
            accessToken,
            refreshToken,
            "Bearer",
            jwtExpirationInMs / 1000,
            modelMapper.mapUserToUserResponse(user)
        );
    }

    /**
     * Connexion d'un utilisateur
     */
    public AuthResponse login(LoginRequest request, String clientIp) {
        // Validation des données
        validateLoginRequest(request);

        // Rechercher l'utilisateur
        User user = userService.getUserByEmail(request.getEmail())
            .orElseThrow(() -> new BadCredentialsException("Email ou mot de passe incorrect"));

        // Vérifier si le compte est actif
        if (!user.getActive()) {
            throw new IllegalStateException("Votre compte a été désactivé");
        }

        // Vérifier le mot de passe
        if (user.getPasswordHash() == null) {
            throw new BadCredentialsException("Ce compte n'a pas de mot de passe défini");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            // Logger la tentative de connexion échouée
            logAuthEvent(user.getId(), "LOGIN_FAILED", clientIp, false);
            throw new BadCredentialsException("Email ou mot de passe incorrect");
        }

        // Mettre à jour la dernière connexion
        userService.updateLastLogin(user.getId());

        // Générer les tokens
        String accessToken = tokenProvider.generateToken(user.getId());
        String refreshToken = tokenProvider.generateRefreshToken(user.getId());

        // Stocker le refresh token
        storeRefreshToken(user.getId(), refreshToken);

        // Logger la connexion réussie
        logAuthEvent(user.getId(), "LOGIN_SUCCESS", clientIp, true);

        return new AuthResponse(
            accessToken,
            refreshToken,
            "Bearer",
            jwtExpirationInMs / 1000,
            modelMapper.mapUserToUserResponse(user)
        );
    }

    /**
     * Rafraîchissement du token d'accès
     */
    public AuthResponse refreshToken(String refreshToken) {
        // Valider le refresh token
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Token de rafraîchissement invalide");
        }

        // Extraire l'ID utilisateur
        String userId = tokenProvider.getUserIdFromToken(refreshToken);

        // Vérifier que le refresh token est stocké
        String storedToken = getStoredRefreshToken(userId);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new IllegalArgumentException("Token de rafraîchissement non reconnu");
        }

        // Vérifier que l'utilisateur existe et est actif
        User user = userRepository.findByIdAndActiveTrue(userId)
            .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé ou inactif"));

        // Générer un nouveau token d'accès
        String newAccessToken = tokenProvider.generateToken(userId);

        return new AuthResponse(
            newAccessToken,
            refreshToken, // Garder le même refresh token
            "Bearer",
            jwtExpirationInMs / 1000,
            modelMapper.mapUserToUserResponse(user)
        );
    }

    /**
     * Déconnexion de l'utilisateur
     */
    public void logout(String accessToken) {
        try {
            // Extraire l'ID utilisateur du token
            String userId = tokenProvider.getUserIdFromToken(accessToken);

            // Supprimer le refresh token du cache
            removeRefreshToken(userId);

            // Blacklister le token d'accès
            blacklistToken(accessToken);

            // Logger la déconnexion
            logAuthEvent(userId, "LOGOUT", null, true);
        } catch (Exception e) {
            // Ignore les erreurs lors de la déconnexion
        }
    }

    /**
     * Validation du token d'accès
     */
    public boolean validateToken(String token) {
        try {
            // Vérifier si le token est blacklisté
            if (isTokenBlacklisted(token)) {
                return false;
            }

            return tokenProvider.validateToken(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Initier la réinitialisation du mot de passe
     */
    public void initiatePasswordReset(String email) {
        Optional<User> userOpt = userService.getUserByEmail(email);
        if (userOpt.isEmpty()) {
            // Ne pas révéler que l'email n'existe pas
            return;
        }

        User user = userOpt.get();
        if (!user.getActive()) {
            return;
        }

        // Générer un token de réinitialisation
        String resetToken = tokenProvider.generatePasswordResetToken(user.getId());

        // Stocker le token avec une expiration
        storePasswordResetToken(user.getId(), resetToken);

        // Envoyer l'email de réinitialisation
        try {
            emailService.sendPasswordResetEmail(user, resetToken);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de l'email de réinitialisation: " + e.getMessage());
        }
    }

    /**
     * Réinitialiser le mot de passe
     */
    public void resetPassword(String resetToken, String newPassword) {
        // Valider le token
        if (!tokenProvider.validateToken(resetToken)) {
            throw new IllegalArgumentException("Token de réinitialisation invalide ou expiré");
        }

        // Extraire l'ID utilisateur
        String userId = tokenProvider.getUserIdFromToken(resetToken);

        // Vérifier que le token est stocké
        String storedToken = getStoredPasswordResetToken(userId);
        if (storedToken == null || !storedToken.equals(resetToken)) {
            throw new IllegalArgumentException("Token de réinitialisation non reconnu");
        }

        // Réinitialiser le mot de passe
        User user = userRepository.findByIdAndActiveTrue(userId)
            .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Supprimer le token de réinitialisation
        removePasswordResetToken(userId);

        // Supprimer tous les refresh tokens de l'utilisateur
        removeRefreshToken(userId);

        // Logger l'événement
        logAuthEvent(userId, "PASSWORD_RESET", null, true);
    }

    /**
     * Vérifier l'email d'un utilisateur
     */
    public void verifyEmail(String verificationToken) {
        // Valider le token
        if (!tokenProvider.validateToken(verificationToken)) {
            throw new IllegalArgumentException("Token de vérification invalide ou expiré");
        }

        // Extraire l'ID utilisateur
        String userId = tokenProvider.getUserIdFromToken(verificationToken);

        // Vérifier l'email
        userService.verifyUserEmail(userId);

        // Logger l'événement
        logAuthEvent(userId, "EMAIL_VERIFIED", null, true);
    }

    /**
     * Renvoyer l'email de vérification
     */
    public void resendVerificationEmail(String email) {
        Optional<User> userOpt = userService.getUserByEmail(email);
        if (userOpt.isEmpty()) {
            return; // Ne pas révéler que l'email n'existe pas
        }

        User user = userOpt.get();
        if (user.getEmailVerified()) {
            return; // Email déjà vérifié
        }

        try {
            emailService.sendEmailVerification(user);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de l'email de vérification: " + e.getMessage());
        }
    }

    /**
     * Obtenir l'URL d'authentification Google
     */
    public String getGoogleAuthUrl() {
        // Implémentation pour générer l'URL OAuth2 Google
        return frontendUrl + "/auth/google";
    }

    /**
     * Obtenir l'URL d'authentification Facebook
     */
    public String getFacebookAuthUrl() {
        // Implémentation pour générer l'URL OAuth2 Facebook
        return frontendUrl + "/auth/facebook";
    }

    /**
     * Traiter le callback OAuth2
     */
    public AuthResponse processOAuth2Callback(String provider, String code, String state, String clientIp) {
        // Implémentation du traitement OAuth2
        // Cette méthode devrait échanger le code contre un token et récupérer les infos utilisateur
        throw new UnsupportedOperationException("OAuth2 callback processing not implemented yet");
    }

    // Méthodes privées utilitaires

    private void validateRegisterRequest(RegisterRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom est obligatoire");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("L'email est obligatoire");
        }
        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 8 caractères");
        }
    }

    private void validateLoginRequest(LoginRequest request) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("L'email est obligatoire");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Le mot de passe est obligatoire");
        }
    }

    private void storeRefreshToken(String userId, String refreshToken) {
        String key = "refresh_token:" + userId;
        redisTemplate.opsForValue().set(key, refreshToken, 
            refreshTokenExpirationInMs, TimeUnit.MILLISECONDS);
    }

    private String getStoredRefreshToken(String userId) {
        String key = "refresh_token:" + userId;
        return (String) redisTemplate.opsForValue().get(key);
    }

    private void removeRefreshToken(String userId) {
        String key = "refresh_token:" + userId;
        redisTemplate.delete(key);
    }

    private void storePasswordResetToken(String userId, String resetToken) {
        String key = "password_reset:" + userId;
        redisTemplate.opsForValue().set(key, resetToken, 1, TimeUnit.HOURS);
    }

    private String getStoredPasswordResetToken(String userId) {
        String key = "password_reset:" + userId;
        return (String) redisTemplate.opsForValue().get(key);
    }

    private void removePasswordResetToken(String userId) {
        String key = "password_reset:" + userId;
        redisTemplate.delete(key);
    }

    private void blacklistToken(String token) {
        String key = "blacklisted_token:" + token;
        long expiration = tokenProvider.getExpirationFromToken(token).getTime() - System.currentTimeMillis();
        if (expiration > 0) {
            redisTemplate.opsForValue().set(key, "blacklisted", expiration, TimeUnit.MILLISECONDS);
        }
    }

    private boolean isTokenBlacklisted(String token) {
        String key = "blacklisted_token:" + token;
        return redisTemplate.hasKey(key);
    }

    private void logAuthEvent(String userId, String eventType, String clientIp, boolean success) {
        // Implémentation du logging des événements d'authentification
        System.out.println(String.format(
            "Auth Event - User: %s, Type: %s, IP: %s, Success: %s, Time: %s",
            userId, eventType, clientIp, success, LocalDateTime.now()
        ));
    }
}