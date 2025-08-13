// ThreadRepository.java
package com.retrouvtout.repository;

import com.retrouvtout.entity.Thread;
import com.retrouvtout.entity.User;
import com.retrouvtout.entity.Listing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThreadRepository extends JpaRepository<Thread, String> {

    /**
     * Trouver un thread par annonce et utilisateur propriétaire
     */
    Optional<Thread> findByListingAndOwnerUser(Listing listing, User ownerUser);

    /**
     * Trouver les threads d'un utilisateur (en tant que propriétaire ou retrouveur)
     */
    @Query("SELECT t FROM Thread t WHERE t.ownerUser = :user OR t.finderUser = :user " +
           "ORDER BY t.lastMessageAt DESC")
    Page<Thread> findByUserInvolved(@Param("user") User user, Pageable pageable);

    /**
     * Trouver les threads d'un utilisateur par statut
     */
    @Query("SELECT t FROM Thread t WHERE (t.ownerUser = :user OR t.finderUser = :user) " +
           "AND t.status = :status ORDER BY t.lastMessageAt DESC")
    Page<Thread> findByUserInvolvedAndStatus(@Param("user") User user, 
                                           @Param("status") Thread.ThreadStatus status, 
                                           Pageable pageable);

    /**
     * Compter les threads non lus pour un utilisateur
     */
    @Query("SELECT COUNT(t) FROM Thread t JOIN t.messages m WHERE " +
           "(t.ownerUser = :user OR t.finderUser = :user) AND " +
           "m.senderUser != :user AND m.isRead = false")
    long countUnreadThreadsForUser(@Param("user") User user);
}
