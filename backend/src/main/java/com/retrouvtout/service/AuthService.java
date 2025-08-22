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
 * ✅ SERVICE D'AUTHENTIFICATION CORRIGÉ
 * Suppression des validations causant l'erreur 500
 */
@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final ModelMapper modelMapper;

    @Value("${app.jwt.expiration:86400000}")
    private long jwtExpirationInMs;

    @Value("${app.jwt.refresh-expiration:604800000}")
    private long refreshTokenExpirationInMs;

    @Autowired
    public AuthService(UserRepository userRepository,
                      PasswordEncoder passwordEncoder,
                      JwtTokenProvider tokenProvider,
                      ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.modelMapper = modelMapper;
    }

    /**
     * ✅ CONNEXION SIMPLIFIÉE
     */
    public AuthResponse login(LoginRequest request, String clientIp) {
        System.out.println("🔧 Début de la connexion pour: " + request.getEmail());
        
        try {
            // Normalisation simple
            String email = request.getEmail().trim().toLowerCase();
            System.out.println("✅ Email normalisé: " + email);

            // Recherche de l'utilisateur
            User user = userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new BadCredentialsException("Email ou mot de passe incorrect"));
            
            System.out.println("✅ Utilisateur trouvé: " + user.getId());

            // Vérification du compte actif
            if (!user.getActive()) {
                throw new IllegalStateException("Votre compte a été désactivé");
            }

            // Vérification du mot de passe
            if (user.getPasswordHash() == null || user.getPasswordHash().isEmpty()) {
                throw new BadCredentialsException("Ce compte n'a pas de mot de passe défini");
            }

            if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                System.out.println("❌ Mot de passe incorrect pour: " + email);
                throw new BadCredentialsException("Email ou mot de passe incorrect");
            }
            
            System.out.println("✅ Mot de passe vérifié");

            // Mise à jour de la dernière connexion
            try {
                user.setLastLoginAt(LocalDateTime.now());
                userRepository.save(user);
                System.out.println("✅ Dernière connexion mise à jour");
            } catch (Exception e) {
                System.err.println("⚠️ Erreur lors de la mise à jour de la dernière connexion: " + e.getMessage());
                // Ne pas faire échouer la connexion pour ça
            }

            // Génération des tokens
            AuthResponse authResponse = generateAuthTokens(user);
            System.out.println("✅ Tokens de connexion générés");

            return authResponse;

        } catch (BadCredentialsException | IllegalStateException e) {
            System.out.println("❌ Erreur d'authentification: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("❌ Erreur inattendue lors de la connexion: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur interne lors de la connexion");
        }
    }

    /**
     * ✅ INSCRIPTION SIMPLIFIÉE
     */
    public AuthResponse register(RegisterRequest request, String clientIp) {
        System.out.println("🔧 Début de l'inscription pour: " + request.getEmail());
        
        try {
            // Normalisation des données
            String email = request.getEmail().trim().toLowerCase();
            String name = request.getName().trim();
            String phone = (request.getPhone() != null && !request.getPhone().trim().isEmpty()) ? 
                request.getPhone().trim() : null;
            
            System.out.println("✅ Données normalisées - Email: " + email);

            // Vérification de l'unicité de l'email
            if (userRepository.existsByEmailAndActiveTrue(email)) {
                throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà");
            }
            System.out.println("✅ Email unique vérifié");

            // Détermination du rôle
            User.UserRole role = determineUserRole(request.getRole());
            System.out.println("✅ Rôle déterminé: " + role.getValue());

            // Création de l'utilisateur
            User user = new User();
            user.setId(java.util.UUID.randomUUID().toString());
            user.setName(name);
            user.setEmail(email);
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            user.setPhone(phone);
            user.setRole(role);
            user.setEmailVerified(false);
            user.setActive(true);
            
            System.out.println("✅ Entité utilisateur créée");

            // Sauvegarde
            User savedUser = userRepository.save(user);
            System.out.println("✅ Utilisateur sauvegardé avec ID: " + savedUser.getId());

            // Génération des tokens
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
            throw new RuntimeException("Erreur interne lors de l'inscription");
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

    // Méthodes utilitaires privées

    private User.UserRole determineUserRole(String roleString) {
        if (roleString == null || roleString.trim().isEmpty()) {
            return User.UserRole.MIXTE;
        }

        try {
            return User.UserRole.fromValue(roleString.trim().toLowerCase());
        } catch (Exception e) {
            System.out.println("⚠️ Rôle invalide '" + roleString + "', utilisation de 'mixte'");
            return User.UserRole.MIXTE;
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
}