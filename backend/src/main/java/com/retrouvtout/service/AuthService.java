package com.retrouvtout.service;

import com.retrouvtout.dto.request.LoginRequest;
import com.retrouvtout.dto.request.RegisterRequest;
import com.retrouvtout.dto.response.AuthResponse;
import com.retrouvtout.dto.response.UserResponse;
import com.retrouvtout.entity.User;
import com.retrouvtout.repository.UserRepository;
import com.retrouvtout.security.JwtTokenProvider;
import com.retrouvtout.util.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service pour l'authentification conforme au cahier des charges
 * Section 3.1 - Inscription/Connexion via email ou réseaux sociaux
 */
@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final EmailService emailService;
    private final ModelMapper modelMapper;

    @Value("${app.jwt.expiration}")
    private long jwtExpirationInMs;

    @Value("${app.jwt.refresh-expiration}")
    private long refreshTokenExpirationInMs;

    @Autowired
    public AuthService(UserRepository userRepository,
                      PasswordEncoder passwordEncoder,
                      JwtTokenProvider tokenProvider,
                      EmailService emailService,
                      ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.emailService = emailService;
        this.modelMapper = modelMapper;
    }

    /**
     * Inscription d'un nouvel utilisateur - Section 3.1
     * Création de compte via email
     */
    public AuthResponse register(RegisterRequest request, String clientIp) {
        validateRegisterRequest(request);

        if (userRepository.existsByEmailAndActiveTrue(request.getEmail())) {
            throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà");
        }

        // Créer l'utilisateur
        User.UserRole role = request.getRole() != null ? 
            User.UserRole.fromValue(request.getRole()) : User.UserRole.RETROUVEUR;

        User user = new User();
        user.setId(java.util.UUID.randomUUID().toString());
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(role);
        user.setEmailVerified(false);
        user.setActive(true);

        User savedUser = userRepository.save(user);

        // Envoyer email de vérification - Section 3.3 (notifications)
        try {
            emailService.sendEmailVerification(savedUser);
        } catch (Exception e) {
            System.err.println("Erreur envoi email vérification: " + e.getMessage());
        }

        // Générer tokens JWT
        String accessToken = tokenProvider.generateToken(savedUser.getId());
        String refreshToken = tokenProvider.generateRefreshToken(savedUser.getId());

        UserResponse userResponse = modelMapper.mapUserToUserResponse(savedUser);

        return new AuthResponse(
            accessToken,
            refreshToken,
            "Bearer",
            jwtExpirationInMs / 1000,
            userResponse
        );
    }

    /**
     * Connexion d'un utilisateur - Section 3.1
     * Connexion via email
     */
    public AuthResponse login(LoginRequest request, String clientIp) {
        validateLoginRequest(request);

        User user = userRepository.findByEmailAndActiveTrue(request.getEmail())
            .orElseThrow(() -> new BadCredentialsException("Email ou mot de passe incorrect"));

        if (!user.getActive()) {
            throw new IllegalStateException("Votre compte a été désactivé");
        }

        if (user.getPasswordHash() == null) {
            throw new BadCredentialsException("Ce compte n'a pas de mot de passe défini");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Email ou mot de passe incorrect");
        }

        // Mettre à jour la dernière connexion
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // Générer tokens JWT
        String accessToken = tokenProvider.generateToken(user.getId());
        String refreshToken = tokenProvider.generateRefreshToken(user.getId());

        UserResponse userResponse = modelMapper.mapUserToUserResponse(user);

        return new AuthResponse(
            accessToken,
            refreshToken,
            "Bearer",
            jwtExpirationInMs / 1000,
            userResponse
        );
    }

    /**
     * Rafraîchissement du token d'accès
     */
    public AuthResponse refreshToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Token de rafraîchissement invalide");
        }

        String userId = tokenProvider.getUserIdFromToken(refreshToken);

        User user = userRepository.findByIdAndActiveTrue(userId)
            .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé ou inactif"));

        String newAccessToken = tokenProvider.generateToken(userId);
        UserResponse userResponse = modelMapper.mapUserToUserResponse(user);

        return new AuthResponse(
            newAccessToken,
            refreshToken,
            "Bearer",
            jwtExpirationInMs / 1000,
            userResponse
        );
    }

    /**
     * Déconnexion de l'utilisateur
     */
    public void logout(String accessToken) {
        // Implémentation simple - blacklister le token si nécessaire
        try {
            String userId = tokenProvider.getUserIdFromToken(accessToken);
            // Log de déconnexion si nécessaire
            System.out.println("Utilisateur déconnecté: " + userId);
        } catch (Exception e) {
            // Ignore les erreurs lors de la déconnexion
        }
    }

    /**
     * Obtenir l'URL d'authentification Google - Section 3.1 (réseaux sociaux)
     */
    public String getGoogleAuthUrl() {
        // URL de base OAuth2 Google - à implémenter selon la configuration OAuth2
        return "https://accounts.google.com/oauth/authorize?client_id=YOUR_CLIENT_ID&redirect_uri=YOUR_REDIRECT_URI&scope=email%20profile&response_type=code";
    }

    /**
     * Obtenir l'URL d'authentification Facebook - Section 3.1 (réseaux sociaux)
     */
    public String getFacebookAuthUrl() {
        // URL de base OAuth2 Facebook - à implémenter selon la configuration OAuth2
        return "https://www.facebook.com/v18.0/dialog/oauth?client_id=YOUR_APP_ID&redirect_uri=YOUR_REDIRECT_URI&scope=email";
    }

    /**
     * Traiter le callback OAuth2 - Section 3.1 (réseaux sociaux)
     */
    public AuthResponse processOAuth2Callback(String provider, String code, String state, String clientIp) {
        // Implémentation simplifiée pour la démo
        // En production, échanger le code contre un token et récupérer les infos utilisateur
        throw new UnsupportedOperationException("OAuth2 non encore implémenté - nécessite configuration spécifique");
    }

    // Méthodes privées de validation

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
}