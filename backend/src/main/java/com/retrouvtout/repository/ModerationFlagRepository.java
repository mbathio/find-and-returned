// ModerationFlagRepository.java
package com.retrouvtout.repository;

import com.retrouvtout.entity.ModerationFlag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ModerationFlagRepository extends JpaRepository<ModerationFlag, Long>, JpaSpecificationExecutor<ModerationFlag> {

    /**
     * Trouver les signalements par statut
     */
    Page<ModerationFlag> findByStatusOrderByCreatedAtDesc(
        ModerationFlag.FlagStatus status, Pageable pageable);

    /**
     * Trouver les signalements par priorité
     */
    Page<ModerationFlag> findByPriorityOrderByCreatedAtDesc(
        ModerationFlag.FlagPriority priority, Pageable pageable);

    /**
     * Trouver les signalements par entité
     */
    Page<ModerationFlag> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
        ModerationFlag.EntityType entityType, String entityId, Pageable pageable);

    /**
     * Compter les signalements en attente
     */
    long countByStatus(ModerationFlag.FlagStatus status);

    /**
     * Statistiques par priorité
     */
    @Query("SELECT f.priority, COUNT(f) FROM ModerationFlag f WHERE f.status = :status GROUP BY f.priority")
    Object[] getStatisticsByPriority(@Param("status") ModerationFlag.FlagStatus status);

    /**
     * Trouver les signalements récents
     */
    @Query("SELECT f FROM ModerationFlag f WHERE f.createdAt >= :since ORDER BY f.createdAt DESC")
    Page<ModerationFlag> findRecentFlags(@Param("since") LocalDateTime since, Pageable pageable);
}