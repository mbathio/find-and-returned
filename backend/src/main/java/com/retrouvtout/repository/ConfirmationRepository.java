// ConfirmationRepository.java
package com.retrouvtout.repository;

import com.retrouvtout.entity.Confirmation;
import com.retrouvtout.entity.Thread;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConfirmationRepository extends JpaRepository<Confirmation, String> {

    /**
     * Trouver une confirmation par thread
     */
    Optional<Confirmation> findByThread(Thread thread);

    /**
     * Trouver une confirmation par code
     */
    Optional<Confirmation> findByCode(String code);

    /**
     * Trouver les confirmations expirées
     */
    @Query("SELECT c FROM Confirmation c WHERE c.expiresAt < :now AND c.usedAt IS NULL")
    List<Confirmation> findExpiredConfirmations(@Param("now") LocalDateTime now);

    /**
     * Supprimer les confirmations expirées
     */
    void deleteByExpiresAtBeforeAndUsedAtIsNull(LocalDateTime expirationDate);
}
