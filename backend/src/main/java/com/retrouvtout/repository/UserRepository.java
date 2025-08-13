package com.retrouvtout.repository;

import com.retrouvtout.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour les utilisateurs
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * Trouver un utilisateur par email (actif uniquement)
     */
    Optional<User> findByEmailAndActiveTrue(String email);

    /**
     * Trouver un utilisateur par ID (actif uniquement)
     */
    Optional<User> findByIdAndActiveTrue(String id);

    /**
     * Vérifier si un email existe (actif uniquement)
     */
    boolean existsByEmailAndActiveTrue(String email);

    /**
     * Trouver tous les utilisateurs actifs
     */
    Page<User> findAllByActiveTrue(Pageable pageable);

    /**
     * Recherche d'utilisateurs par nom ou email
     */
    Page<User> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseAndActiveTrue(
        String name, String email, Pageable pageable);

    /**
     * Trouver les utilisateurs récents
     */
    @Query("SELECT u FROM User u WHERE u.active = true ORDER BY u.createdAt DESC")
    List<User> findTopByActiveTrueOrderByCreatedAtDesc(int limit);

    /**
     * Compter les utilisateurs actifs
     */
    long countByActiveTrue();

    /**
     * Compter les utilisateurs créés dans une période
     */
    long countByActiveTrueAndCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Trouver les utilisateurs par rôle
     */
    Page<User> findByRoleAndActiveTrueOrderByCreatedAtDesc(User.UserRole role, Pageable pageable);

    /**
     * Trouver les utilisateurs avec email non vérifié
     */
    Page<User> findByEmailVerifiedFalseAndActiveTrueOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Recherche avancée d'utilisateurs
     */
    @Query("SELECT u FROM User u WHERE " +
           "u.active = true AND " +
           "(:name IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:emailVerified IS NULL OR u.emailVerified = :emailVerified) " +
           "ORDER BY u.createdAt DESC")
    Page<User> findUsersWithFilters(@Param("name") String name,
                                   @Param("email") String email,
                                   @Param("role") User.UserRole role,
                                   @Param("emailVerified") Boolean emailVerified,
                                   Pageable pageable);

    /**
     * Statistiques par rôle
     */
    @Query("SELECT u.role, COUNT(u) FROM User u WHERE u.active = true GROUP BY u.role")
    List<Object[]> getStatisticsByRole();

    /**
     * Trouver les utilisateurs avec des annonces récentes
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.listings l WHERE " +
           "u.active = true AND " +
           "l.createdAt >= :since AND " +
           "l.status = com.retrouvtout.entity.Listing$ListingStatus.ACTIVE " +
           "ORDER BY u.createdAt DESC")
    List<User> findUsersWithRecentListings(@Param("since") LocalDateTime since);

    /**
     * Trouver les utilisateurs les plus actifs
     */
    @Query("SELECT u, COUNT(l) as listingCount FROM User u LEFT JOIN u.listings l " +
           "WHERE u.active = true AND " +
           "(l.status = com.retrouvtout.entity.Listing$ListingStatus.ACTIVE OR l IS NULL) " +
           "GROUP BY u ORDER BY listingCount DESC")
    Page<Object[]> findMostActiveUsers(Pageable pageable);
}