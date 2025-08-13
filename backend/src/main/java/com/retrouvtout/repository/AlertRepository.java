// AlertRepository.java
package com.retrouvtout.repository;

import com.retrouvtout.entity.Alert;
import com.retrouvtout.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, String> {

    /**
     * Trouver les alertes actives d'un utilisateur
     */
    Page<Alert> findByOwnerUserAndActiveTrueOrderByCreatedAtDesc(User ownerUser, Pageable pageable);

    /**
     * Compter les alertes actives d'un utilisateur
     */
    long countByOwnerUserAndActiveTrue(User ownerUser);

    /**
     * Trouver toutes les alertes actives
     */
    List<Alert> findByActiveTrue();

    /**
     * Trouver les alertes récemment déclenchées d'un utilisateur
     */
    Page<Alert> findByOwnerUserAndLastTriggeredAtIsNotNullOrderByLastTriggeredAtDesc(
        User ownerUser, Pageable pageable);

    /**
     * Statistiques globales des alertes
     */
    long countByActiveTrue();
}