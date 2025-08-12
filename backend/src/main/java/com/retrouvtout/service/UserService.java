package com.retrouvtout.service;

import com.retrouvtout.dto.request.UpdateUserRequest;
import com.retrouvtout.dto.response.UserResponse;
import com.retrouvtout.dto.response.UserStatsResponse;
import com.retrouvtout.entity.User;
import com.retrouvtout.exception.ResourceNotFoundException;
import com.retrouvtout.repository.UserRepository;
import com.retrouvtout.util.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service pour la gestion des utilisateurs
 */
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final EmailService emailService;

    @Autowired
    public UserService(UserRepository userRepository, 
                      PasswordEncoder passwordEncoder,
                      ModelMapper modelMapper,
                      EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
        this.emailService = emailService;
    }

    /**
     * Obtenir un utilisateur par son ID
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(String userId) {
        User user = userRepository.findByIdAndActiveTrue(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", userId));
        
        return modelMapper.mapUserToUserResponse(user);
    }

    /**
     * Obtenir un utilisateur par son email
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmailAndActiveTrue(email.toLowerCase().trim());
    }

    /**
     * Créer un nouvel utilisateur
     */
    public User createUser(String name, String email, String password, User.UserRole role) {
        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmailAndActiveTrue(email.toLowerCase().trim())) {
            throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà");
        }

        User user = new User();
        user.setId(java.util.UUID.randomUUID().toString());
        user.setName(name.trim());
        user.setEmail(email.toLowerCase().trim());
        user.setRole(role != null ? role : User.UserRole.MIXTE);
        user.setActive(true);
        user.setEmailVerified(false);

        if (password != null && !password.isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(password));
        }

        User savedUser = userRepository.save(user);

        // Envoyer l'email de vérification
        try {
            emailService.sendEmailVerification(savedUser);
        } catch (Exception e) {
            // Log l'erreur mais ne pas faire échouer la création
            System.err.println("Erreur lors de l'envoi de l'email de vérification: " + e.getMessage());
        }

        return savedUser;
    }

    /**
     * Mettre à jour un utilisateur
     */
    public UserResponse updateUser(String userId, UpdateUserRequest request) {
        User user = userRepository.findByIdAndActiveTrue(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", userId));

        // Mettre à jour les champs modifiables
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            user.setName(request.getName().trim());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim().isEmpty() ? null : request.getPhone().trim());
        }

        if (request.getRole() != null) {
            try {
                User.UserRole role = User.UserRole.fromValue(request.getRole());
                user.setRole(role);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Rôle invalide: " + request.getRole());
            }
        }

        User updatedUser = userRepository.save(user);
        return modelMapper.mapUserToUserResponse(updatedUser);
    }

    /**
     * Changer le mot de passe d'un utilisateur
     */
    public void changePassword(String userId, String currentPassword, String newPassword) {
        User user = userRepository.findByIdAndActiveTrue(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", userId));

        // Vérifier le mot de passe actuel
        if (user.getPasswordHash() == null) {
            throw new IllegalStateException("Aucun mot de passe défini pour ce compte");
        }

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Mot de passe actuel incorrect");
        }

        // Valider le nouveau mot de passe
        validatePassword(newPassword);

        // Mettre à jour le mot de passe
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Désactiver un utilisateur (soft delete)
     */
    public void deactivateUser(String userId) {
        User user = userRepository.findByIdAndActiveTrue(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", userId));

        user.setActive(false);
        user.setEmail("deleted_" + System.currentTimeMillis() + "_" + userId + "@deleted.com");
        userRepository.save(user);
    }

    /**
     * Vérifier l'email d'un utilisateur
     */
    public void verifyUserEmail(String userId) {
        User user = userRepository.findByIdAndActiveTrue(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", userId));

        user.setEmailVerified(true);
        userRepository.save(user);
    }

    /**
     * Obtenir les statistiques d'un utilisateur
     */
    @Transactional(readOnly = true)
    public UserStatsResponse getUserStats(String userId) {
        User user = userRepository.findByIdAndActiveTrue(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", userId));

        // Calculer les statistiques depuis les relations
        long totalListings = user.getListings().size();
        long activeListings = user.getListings().stream()
            .filter(listing -> listing.getStatus() == Listing.ListingStatus.ACTIVE)
            .count();
        long resolvedListings = user.getListings().stream()
            .filter(listing -> listing.getStatus() == Listing.ListingStatus.RESOLU)
            .count();

        long totalThreads = user.getOwnerThreads().size() + user.getFinderThreads().size();
        long activeAlerts = user.getAlerts().stream()
            .filter(alert -> alert.getActive())
            .count();

        return new UserStatsResponse(
            totalListings,
            activeListings,
            resolvedListings,
            totalThreads,
            activeAlerts
        );
    }

    /**
     * Rechercher des utilisateurs (pour les administrateurs)
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(String query, Pageable pageable) {
        Page<User> users;
        
        if (query != null && !query.trim().isEmpty()) {
            users = userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseAndActiveTrue(
                query.trim(), query.trim(), pageable);
        } else {
            users = userRepository.findAllByActiveTrue(pageable);
        }

        return users.map(modelMapper::mapUserToUserResponse);
    }

    /**
     * Obtenir les utilisateurs récents
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getRecentUsers(int limit) {
        List<User> users = userRepository.findTopByActiveTrueOrderByCreatedAtDesc(limit);
        return users.stream()
            .map(modelMapper::mapUserToUserResponse)
            .toList();
    }

    /**
     * Vérifier si un utilisateur existe par email
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmailAndActiveTrue(email.toLowerCase().trim());
    }

    /**
     * Obtenir le profil public d'un utilisateur
     */
    @Transactional(readOnly = true)
    public UserResponse getPublicProfile(String userId) {
        User user = userRepository.findByIdAndActiveTrue(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", userId));
        
        return modelMapper.mapUserToPublicUserResponse(user);
    }

    /**
     * Mettre à jour la dernière connexion
     */
    public void updateLastLogin(String userId) {
        userRepository.findByIdAndActiveTrue(userId).ifPresent(user -> {
            // Vous pouvez ajouter un champ lastLoginAt dans l'entité User si nécessaire
            userRepository.save(user);
        });
    }

    /**
     * Valider la complexité du mot de passe
     */
    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 8 caractères");
        }

        if (!password.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins une lettre majuscule");
        }

        if (!password.matches(".*[a-z].*")) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins une lettre minuscule");
        }

        if (!password.matches(".*[0-9].*")) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins un chiffre");
        }

        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>?].*")) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins un caractère spécial");
        }
    }

    /**
     * Créer un utilisateur OAuth
     */
    public User createOAuthUser(String name, String email, String provider, String providerId) {
        User user = createUser(name, email, null, User.UserRole.MIXTE);
        
        // L'utilisateur OAuth est automatiquement vérifié
        user.setEmailVerified(true);
        
        return userRepository.save(user);
    }

    /**
     * Lier un compte OAuth à un utilisateur existant
     */
    public void linkOAuthAccount(String userId, String provider, String providerId) {
        User user = userRepository.findByIdAndActiveTrue(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", userId));

        // Vérifier si le compte OAuth n'est pas déjà lié
        boolean alreadyLinked = user.getOauthAccounts().stream()
            .anyMatch(account -> account.getProvider().equals(provider) && 
                     account.getProviderUserId().equals(providerId));

        if (alreadyLinked) {
            throw new IllegalStateException("Ce compte " + provider + " est déjà lié");
        }

        // Créer le lien OAuth (sera géré par OAuthAccountService)
    }

    /**
     * Obtenir le nombre total d'utilisateurs actifs
     */
    @Transactional(readOnly = true)
    public long getTotalActiveUsers() {
        return userRepository.countByActiveTrue();
    }

    /**
     * Obtenir le nombre d'utilisateurs créés aujourd'hui
     */
    @Transactional(readOnly = true)
    public long getTodayRegistrations() {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);
        
        return userRepository.countByActiveTrueAndCreatedAtBetween(startOfDay, endOfDay);
    }

    /**
     * Réactiver un utilisateur désactivé
     */
    public UserResponse reactivateUser(String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", userId));

        if (user.getActive()) {
            throw new IllegalStateException("L'utilisateur est déjà actif");
        }

        user.setActive(true);
        User reactivatedUser = userRepository.save(user);
        
        return modelMapper.mapUserToUserResponse(reactivatedUser);
    }
}