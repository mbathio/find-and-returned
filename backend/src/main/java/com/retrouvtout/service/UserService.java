package com.retrouvtout.service;

import com.retrouvtout.dto.response.UserResponse;
import com.retrouvtout.entity.User;
import com.retrouvtout.exception.ResourceNotFoundException;
import com.retrouvtout.repository.UserRepository;
import com.retrouvtout.security.JwtTokenProvider;
import com.retrouvtout.util.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service pour la gestion des utilisateurs
 */
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final EmailService emailService;
    private final ModelMapper modelMapper;

    @Autowired
    public UserService(UserRepository userRepository,
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
     * Créer un nouvel utilisateur
     */
    public User createUser(String name, String email, String password, User.UserRole role) {
        // Vérifier si l'email existe déjà
        if (existsByEmail(email)) {
            throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà");
        }

        User user = new User();
        user.setId(java.util.UUID.randomUUID().toString());
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        user.setEmailVerified(false);
        user.setActive(true);

        User savedUser = userRepository.save(user);

        // Envoyer l'email de vérification
        try {
            emailService.sendEmailVerification(savedUser);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de l'email de vérification: " + e.getMessage());
        }

        // Envoyer l'email de bienvenue
        try {
            emailService.sendWelcomeEmail(savedUser);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de l'email de bienvenue: " + e.getMessage());
        }

        return savedUser;
    }

    /**
     * Obtenir un utilisateur par son ID
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(String id) {
        User user = userRepository.findByIdAndActiveTrue(id)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", id));
        
        return modelMapper.mapUserToUserResponse(user);
    }

    /**
     * Obtenir un utilisateur par son email
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmailAndActiveTrue(email);
    }

    /**
     * Vérifier si un email existe
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmailAndActiveTrue(email);
    }

    /**
     * Mettre à jour un utilisateur
     */
    public UserResponse updateUser(String id, String name, String phone, User.UserRole role) {
        User user = userRepository.findByIdAndActiveTrue(id)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", id));

        if (name != null && !name.trim().isEmpty()) {
            user.setName(name.trim());
        }
        
        if (phone != null) {
            user.setPhone(phone.trim().isEmpty() ? null : phone.trim());
        }
        
        if (role != null) {
            user.setRole(role);
        }

        User updatedUser = userRepository.save(user);
        return modelMapper.mapUserToUserResponse(updatedUser);
    }

    /**
     * Changer le mot de passe d'un utilisateur
     */
    public void changePassword(String userId, String oldPassword, String newPassword) {
        User user = userRepository.findByIdAndActiveTrue(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", userId));

        if (user.getPasswordHash() == null) {
            throw new IllegalStateException("Ce compte n'a pas de mot de passe défini");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Ancien mot de passe incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Vérifier l'email d'un utilisateur
     */
    public void verifyUserEmail(String userId) {
        User user = userRepository.findByIdAndActiveTrue(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", userId));

        if (user.getEmailVerified()) {
            throw new IllegalStateException("L'email est déjà vérifié");
        }

        user.setEmailVerified(true);
        userRepository.save(user);
    }

    /**
     * Mettre à jour la dernière connexion
     */
    public void updateLastLogin(String userId) {
        User user = userRepository.findByIdAndActiveTrue(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", userId));

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * Désactiver un utilisateur
     */
    public void deactivateUser(String userId) {
        User user = userRepository.findByIdAndActiveTrue(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", userId));

        user.setActive(false);
        userRepository.save(user);
    }

    /**
     * Réactiver un utilisateur
     */
    public void reactivateUser(String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", userId));

        user.setActive(true);
        userRepository.save(user);
    }

    /**
     * Obtenir tous les utilisateurs (pagination)
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAllByActiveTrue(pageable);
        return users.map(modelMapper::mapUserToUserResponse);
    }

    /**
     * Rechercher des utilisateurs
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(String query, Pageable pageable) {
        Page<User> users = userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseAndActiveTrue(
            query, query, pageable);
        return users.map(modelMapper::mapUserToUserResponse);
    }

    /**
     * Obtenir les statistiques des utilisateurs
     */
    @Transactional(readOnly = true)
    public long getTotalActiveUsers() {
        return userRepository.countByActiveTrue();
    }

    /**
     * Obtenir les utilisateurs récents
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> getRecentUsers(Pageable pageable) {
        Page<User> users = userRepository.findAllByActiveTrue(pageable);
        return users.map(modelMapper::mapUserToUserResponse);
    }

    /**
     * Obtenir un utilisateur par son ID (entity)
     */
    @Transactional(readOnly = true)
    public User getUserEntityById(String id) {
        return userRepository.findByIdAndActiveTrue(id)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", id));
    }

    /**
     * Recherche avancée d'utilisateurs avec filtres
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsersWithFilters(String name, String email, 
                                                     User.UserRole role, Boolean emailVerified, 
                                                     Pageable pageable) {
        Page<User> users = userRepository.findUsersWithFilters(name, email, role, emailVerified, pageable);
        return users.map(modelMapper::mapUserToUserResponse);
    }
}