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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * ✅ SERVICE D'AUTHENTIFICATION ULTRA-ROBUSTE
 * Gestion complète des erreurs avec logs détaillés
 */
@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final EmailService emailService;
    private final ModelMapper modelMapper;

    @Value("${app.jwt.expiration:86400000}")
    private long jwtExpirationInMs;

    @Value("${app.jwt.refresh-expiration:604800000}")
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
     * ✅ INSCRIPTION ULTRA-ROBUSTE
     */
    public AuthResponse register(RegisterRequest request, String clientIp) {
        System.out.println("🔧 Début de l'inscription pour: " + request.getEmail());
        
        try {
            // 1. Validation préliminaire
            validateRegisterRequestStrict(request);
            System.out.println("✅ Validation préliminaire réussie");

            // 2. Normalisation des données
            String email = request.getEmail().trim().toLowerCase();
            String name = request.getName().trim();
            String phone = request.getPhone() != null ? request.getPhone().trim() : null;
            
            System.out.println("✅ Données normalisées - Email: " + email);

            // 3. Vérification de l'unicité de l'email
            if (checkEmailExists(email)) {
                throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà");
            }
            System.out.println("✅ Email unique vérifié");

            // 4. Détermination du rôle
            User.UserRole role = determineUserRole(request.getRole());
            System.out.println("✅ Rôle déterminé: " + role.getValue());

            // 5. Création de l'utilisateur
            User user = createUserEntity(name, email, request.getPassword(), phone, role);
            System.out.println("✅ Entité utilisateur créée");

            // 6. Sauvegarde sécurisée
            User savedUser = saveUserSecurely(user);
            System.out.println("✅ Utilisateur sauvegardé avec ID: " + savedUser.getId());

            // 7. Envoi de l'email de vérification (non bloquant)
            sendVerificationEmailAsync(savedUser);

            // 8. Génération des tokens
            AuthResponse authResponse = generateAuthTokens(savedUser);
            System.out.println("✅ Tokens générés avec succès");

            return authResponse;

        } catch (IllegalArgumentException e) {
            System.out.println("❌ Erreur de validation: " + e.getMessage());
            throw e;
        } catch (DataIntegrityViolationException e) {
            System.err.println("❌ Erreur de contrainte DB: " + e.getMessage());
            throw new IllegalArgumentException("Cette adresse email est déjà utilisée");
        } catch (Exception e) {
            System.err.println("❌ Erreur inattendue lors de l'inscription: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur interne lors de l'inscription. Veuillez réessayer plus tard.");
        }
    }

    /**
     * ✅ CONNEXION ULTRA-ROBUSTE
     */
    public AuthResponse login(LoginRequest request, String clientIp) {
        System.out.println("🔧 Début de la connexion pour: " + request.getEmail());
        
        try {
            // 1. Validation des données
            validateLoginRequestStrict(request);
            System.out.println("✅ Validation des données de connexion réussie");

            // 2. Normalisation
            String email = request.getEmail().trim().toLowerCase();
            System.out.println("✅ Email normalisé: " + email);

            // 3. Recherche de l'utilisateur
            User user = findUserByEmailSecurely(email);
            System.out.println("✅ Utilisateur trouvé: " + user.getId());

            // 4. Vérifications de sécurité
            performSecurityChecks(user);
            System.out.println("✅ Vérifications de sécurité passées");

            // 5. Vérification du mot de passe
            if (!verifyPassword(request.getPassword(), user.getPasswordHash())) {
                System.out.println("❌ Mot de passe incorrect pour: " + email);
                throw new BadCredentialsException("Email ou mot de passe incorrect");
            }
            System.out.println("✅ Mot de passe vérifié");

            // 6. Mise à jour de la dernière connexion
            updateLastLogin(user);
            System.out.println("✅ Dernière connexion mise à jour");

            // 7. Génération des tokens
            AuthResponse authResponse = generateAuthTokens(user);
            System.out.println("✅ Tokens de connexion générés");

            return authResponse;

        } catch (BadCredentialsException | IllegalStateException e) {
            System.out.println("❌ Erreur d'authentification: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("❌ Erreur inattendue lors de la connexion: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur interne lors de la connexion. Veuillez réessayer plus tard.");
        }
    }

    /**
     * ✅ RAFRAÎCHISSEMENT DE TOKEN
     */
    public AuthResponse refreshToken(String refreshToken) {
        try {
            System.out.println("🔧 Rafraîchissement de token");
            
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                throw new IllegalArgumentException("Token de rafraîchissement manquant");
            }

            if (!tokenProvider.validateToken(refreshToken)) {
                throw new IllegalArgumentException("Token de rafraîchissement invalide ou expiré");
            }

            String userId = tokenProvider.getUserIdFromToken(refreshToken);
            User user = userRepository.findByIdAndActiveTrue(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé ou inactif"));

            String newAccessToken = tokenProvider.generateToken(userId);
            UserResponse userResponse = modelMapper.mapUserToUserResponse(user);

            System.out.println("✅ Token rafraîchi pour utilisateur: " + userId);

            return new AuthResponse(
                newAccessToken,
                refreshToken,
                "Bearer",
                jwtExpirationInMs / 1000,
                userResponse
            );

        } catch (IllegalArgumentException e) {
            System.out.println("❌ Erreur de token: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du refresh: " + e.getMessage());
            throw new RuntimeException("Erreur lors du rafraîchissement du token");
        }
    }

    /**
     * ✅ DÉCONNEXION
     */
    public void logout(String accessToken) {
        try {
            if (accessToken != null && !accessToken.trim().isEmpty()) {
                String userId = tokenProvider.getUserIdFromToken(accessToken);
                System.out.println("✅ Utilisateur déconnecté: " + userId);
            }
        } catch (Exception e) {
            System.out.println("ℹ️ Déconnexion sans validation de token");
        }
    }

    // ✅ MÉTHODES UTILITAIRES PRIVÉES

    private void validateRegisterRequestStrict(RegisterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Données d'inscription manquantes");
        }

        if (isEmpty(request.getName())) {
            throw new IllegalArgumentException("Le nom est obligatoire");
        }

        if (request.getName().trim().length() > 120) {
            throw new IllegalArgumentException("Le nom ne peut pas dépasser 120 caractères");
        }

        if (isEmpty(request.getEmail())) {
            throw new IllegalArgumentException("L'email est obligatoire");
        }

        if (!isValidEmailFormat(request.getEmail())) {
            throw new IllegalArgumentException("Format d'email invalide");
        }

        if (request.getEmail().trim().length() > 190) {
            throw new IllegalArgumentException("L'email ne peut pas dépasser 190 caractères");
        }

        if (isEmpty(request.getPassword())) {
            throw new IllegalArgumentException("Le mot de passe est obligatoire");
        }

        if (request.getPassword().length() < 6) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 6 caractères");
        }

        if (request.getPassword().length() > 255) {
            throw new IllegalArgumentException("Le mot de passe ne peut pas dépasser 255 caractères");
        }
    }

    private void validateLoginRequestStrict(LoginRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Données de connexion manquantes");
        }

        if (isEmpty(request.getEmail())) {
            throw new IllegalArgumentException("L'email est obligatoire");
        }

        if (!isValidEmailFormat(request.getEmail())) {
            throw new IllegalArgumentException("Format d'email invalide");
        }

        if (isEmpty(request.getPassword())) {
            throw new IllegalArgumentException("Le mot de passe est obligatoire");
        }
    }

    private boolean checkEmailExists(String email) {
        try {
            return userRepository.existsByEmailAndActiveTrue(email);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la vérification d'email: " + e.getMessage());
            throw new RuntimeException("Erreur lors de la vérification de l'email");
        }
    }

    private User.UserRole determineUserRole(String roleString) {
        if (isEmpty(roleString)) {
            return User.UserRole.MIXTE;
        }

        try {
            return User.UserRole.fromValue(roleString.trim().toLowerCase());
        } catch (Exception e) {
            System.out.println("⚠️ Rôle invalide '" + roleString + "', utilisation de 'mixte'");
            return User.UserRole.MIXTE;
        }
    }

    private User createUserEntity(String name, String email, String password, String phone, User.UserRole role) {
        try {
            User user = new User();
            user.setId(java.util.UUID.randomUUID().toString());
            user.setName(name);
            user.setEmail(email);
            user.setPasswordHash(passwordEncoder.encode(password));
            user.setPhone(isEmpty(phone) ? null : phone);
            user.setRole(role);
            user.setEmailVerified(false);
            user.setActive(true);
            
            return user;
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la création de l'entité: " + e.getMessage());
            throw new RuntimeException("Erreur lors de la création du compte");
        }
    }

    private User saveUserSecurely(User user) {
        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            System.err.println("❌ Violation de contrainte DB: " + e.getMessage());
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("email")) {
                throw new IllegalArgumentException("Cette adresse email est déjà utilisée");
            }
            throw new IllegalArgumentException("Ces informations sont déjà utilisées");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la sauvegarde: " + e.getMessage());
            throw new RuntimeException("Erreur lors de la création du compte");
        }
    }

    private User findUserByEmailSecurely(String email) {
        try {
            return userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new BadCredentialsException("Email ou mot de passe incorrect"));
        } catch (BadCredentialsException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la recherche utilisateur: " + e.getMessage());
            throw new RuntimeException("Erreur lors de la connexion");
        }
    }

    private void performSecurityChecks(User user) {
        if (!user.getActive()) {
            throw new IllegalStateException("Votre compte a été désactivé");
        }

        if (user.getPasswordHash() == null || user.getPasswordHash().isEmpty()) {
            throw new BadCredentialsException("Ce compte n'a pas de mot de passe défini");
        }
    }

    private boolean verifyPassword(String rawPassword, String encodedPassword) {
        try {
            return passwordEncoder.matches(rawPassword, encodedPassword);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la vérification du mot de passe: " + e.getMessage());
            return false;
        }
    }

    private void updateLastLogin(User user) {
        try {
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
        } catch (Exception e) {
            System.err.println("⚠️ Erreur lors de la mise à jour de la dernière connexion: " + e.getMessage());
            // Ne pas faire échouer la connexion pour ça
        }
    }

    private AuthResponse generateAuthTokens(User user) {
        try {
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
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la génération des tokens: " + e.getMessage());
            throw new RuntimeException("Erreur lors de la génération des tokens");
        }
    }

    private void sendVerificationEmailAsync(User user) {
        try {
            emailService.sendEmailVerification(user);
            System.out.println("✅ Email de vérification envoyé à: " + user.getEmail());
        } catch (Exception e) {
            System.err.println("⚠️ Erreur envoi email pour " + user.getEmail() + ": " + e.getMessage());
            // Ne pas faire échouer l'inscription pour un problème d'email
        }
    }

    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    private boolean isValidEmailFormat(String email) {
        if (isEmpty(email)) {
            return false;
        }
        
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.trim().matches(emailRegex);
    }

    // Stubs pour OAuth2 (désactivé en développement)
    public String getGoogleAuthUrl() {
        throw new UnsupportedOperationException("OAuth2 Google non configuré en développement");
    }

    public String getFacebookAuthUrl() {
        throw new UnsupportedOperationException("OAuth2 Facebook non configuré en développement");
    }

    public AuthResponse processOAuth2Callback(String provider, String code, String state, String clientIp) {
        throw new UnsupportedOperationException("OAuth2 " + provider + " non configuré en développement");
    }
}