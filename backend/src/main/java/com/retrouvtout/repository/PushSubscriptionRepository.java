// PushSubscriptionRepository.java
package com.retrouvtout.repository;

import com.retrouvtout.entity.PushSubscription;
import com.retrouvtout.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {

    /**
     * Trouver les abonnements d'un utilisateur
     */
    List<PushSubscription> findByUser(User user);

    /**
     * Trouver un abonnement par endpoint
     */
    Optional<PushSubscription> findByEndpoint(String endpoint);

    /**
     * Supprimer les abonnements inactifs
     */
    void deleteByLastUsedAtBefore(LocalDateTime cutoffDate);

    /**
     * Compter les abonnements actifs
     */
    @Query("SELECT COUNT(p) FROM PushSubscription p WHERE p.lastUsedAt >= :since")
    long countActiveSubscriptions(@Param("since") LocalDateTime since);
}