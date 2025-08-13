package com.retrouvtout.repository;

import com.retrouvtout.entity.Listing;
import com.retrouvtout.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour les annonces d'objets retrouvés
 */
@Repository
public interface ListingRepository extends JpaRepository<Listing, String>, JpaSpecificationExecutor<Listing> {

    /**
     * Trouver les annonces par utilisateur et statut
     */
    Page<Listing> findByFinderUserAndStatusOrderByCreatedAtDesc(User finderUser, Listing.ListingStatus status, Pageable pageable);

    /**
     * Trouver les annonces par utilisateur (excluant un statut)
     */
    Page<Listing> findByFinderUserAndStatusNotOrderByCreatedAtDesc(User finderUser, Listing.ListingStatus status, Pageable pageable);

    /**
     * Trouver les annonces actives et modérées
     */
    Page<Listing> findByStatusAndIsModeratedOrderByCreatedAtDesc(Listing.ListingStatus status, Boolean isModerated, Pageable pageable);

    /**
     * Trouver les annonces par catégorie
     */
    Page<Listing> findByCategoryAndStatusAndIsModeratedOrderByCreatedAtDesc(
        Listing.ListingCategory category, Listing.ListingStatus status, Boolean isModerated, Pageable pageable);

    /**
     * Recherche full-text dans le titre et la description
     */
    @Query("SELECT l FROM Listing l WHERE " +
           "(LOWER(l.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(l.description) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "l.status = :status AND l.isModerated = true " +
           "ORDER BY l.createdAt DESC")
    Page<Listing> findByTextSearchAndStatus(@Param("query") String query, 
                                          @Param("status") Listing.ListingStatus status, 
                                          Pageable pageable);

    /**
     * Recherche géographique par proximité
     */
    @Query(value = "SELECT * FROM listings l WHERE " +
                   "l.status = :status AND l.is_moderated = true AND " +
                   "l.latitude IS NOT NULL AND l.longitude IS NOT NULL AND " +
                   "ST_Distance_Sphere(POINT(l.longitude, l.latitude), POINT(:lng, :lat)) <= :radiusMeters " +
                   "ORDER BY ST_Distance_Sphere(POINT(l.longitude, l.latitude), POINT(:lng, :lat))",
           nativeQuery = true)
    Page<Listing> findByLocationWithinRadius(@Param("lat") BigDecimal lat,
                                           @Param("lng") BigDecimal lng,
                                           @Param("radiusMeters") double radiusMeters,
                                           @Param("status") String status,
                                           Pageable pageable);

    /**
     * Trouver des annonces similaires
     */
    @Query("SELECT l FROM Listing l WHERE " +
           "l.id != :excludeId AND " +
           "l.category = :category AND " +
           "l.status = com.retrouvtout.entity.Listing$ListingStatus.ACTIVE AND " +
           "l.isModerated = true " +
           "ORDER BY l.createdAt DESC")
    List<Listing> findSimilarListings(@Param("excludeId") String excludeId,
                                    @Param("category") Listing.ListingCategory category,
                                    @Param("lat") BigDecimal lat,
                                    @Param("lng") BigDecimal lng,
                                    Pageable pageable);

    /**
     * Version simplifiée pour les annonces similaires
     */
    @Query("SELECT l FROM Listing l WHERE " +
           "l.id != :excludeId AND " +
           "l.category = :category AND " +
           "l.status = com.retrouvtout.entity.Listing$ListingStatus.ACTIVE AND " +
           "l.isModerated = true " +
           "ORDER BY l.createdAt DESC")
    List<Listing> findSimilarListings(@Param("excludeId") String excludeId,
                                    @Param("category") Listing.ListingCategory category,
                                    @Param("lat") BigDecimal lat,
                                    @Param("lng") BigDecimal lng,
                                    int limit);

    /**
     * Compter les annonces par statut et modération
     */
    long countByStatusAndIsModerated(Listing.ListingStatus status, Boolean isModerated);

    /**
     * Compter les annonces créées dans une période
     */
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Trouver les annonces récentes pour les alertes
     */
    @Query("SELECT l FROM Listing l WHERE " +
           "l.createdAt >= :since AND " +
           "l.status = com.retrouvtout.entity.Listing$ListingStatus.ACTIVE AND " +
           "l.isModerated = true " +
           "ORDER BY l.createdAt DESC")
    List<Listing> findRecentListings(@Param("since") LocalDateTime since);

    /**
     * Trouver les annonces par catégorie pour les alertes
     */
    @Query("SELECT l FROM Listing l WHERE " +
           "l.category = :category AND " +
           "l.createdAt >= :since AND " +
           "l.status = com.retrouvtout.entity.Listing$ListingStatus.ACTIVE AND " +
           "l.isModerated = true " +
           "ORDER BY l.createdAt DESC")
    List<Listing> findRecentListingsByCategory(@Param("category") Listing.ListingCategory category,
                                             @Param("since") LocalDateTime since);

    /**
     * Recherche pour les alertes avec mots-clés
     */
    @Query("SELECT l FROM Listing l WHERE " +
           "(LOWER(l.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(l.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "l.createdAt >= :since AND " +
           "l.status = com.retrouvtout.entity.Listing$ListingStatus.ACTIVE AND " +
           "l.isModerated = true " +
           "ORDER BY l.createdAt DESC")
    List<Listing> findRecentListingsByKeyword(@Param("keyword") String keyword,
                                            @Param("since") LocalDateTime since);

    /**
     * Trouver les annonces nécessitant une modération
     */
    Page<Listing> findByIsModeratedFalseOrderByCreatedAtAsc(Pageable pageable);

    /**
     * Trouver les annonces les plus vues
     */
    Page<Listing> findByStatusAndIsModeratedOrderByViewsCountDesc(
        Listing.ListingStatus status, Boolean isModerated, Pageable pageable);

    /**
     * Statistiques par catégorie
     */
    @Query("SELECT l.category, COUNT(l) FROM Listing l WHERE " +
           "l.status = :status AND l.isModerated = true " +
           "GROUP BY l.category")
    List<Object[]> getStatisticsByCategory(@Param("status") Listing.ListingStatus status);
}