package com.retrouvtout.repository;

import com.retrouvtout.entity.Message;
import com.retrouvtout.entity.Thread;
import com.retrouvtout.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ✅ REPOSITORY MESSAGES CORRIGÉ - VERSION COMPLÈTE
 * Ajout de la méthode countUnreadMessagesForUser manquante
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, String> {

    /**
     * Trouver les messages d'un thread
     */
    Page<Message> findByThreadOrderByCreatedAtAsc(Thread thread, Pageable pageable);

    /**
     * Compter les messages non lus d'un thread pour un utilisateur
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.thread = :thread AND " +
           "m.senderUser != :user AND m.isRead = false")
    long countUnreadMessagesInThreadForUser(@Param("thread") Thread thread, @Param("user") User user);

    /**
     * ✅ CORRECTION : Compter les messages non lus pour un utilisateur (méthode manquante)
     * Compte tous les messages non lus dans les threads où l'utilisateur participe
     */
    @Query("SELECT COUNT(m) FROM Message m " +
           "JOIN m.thread t " +
           "WHERE (t.ownerUser = :user OR t.finderUser = :user) " +
           "AND m.senderUser != :user " +
           "AND m.isRead = false")
    long countUnreadMessagesForUser(@Param("user") User user);

    /**
     * Marquer tous les messages d'un thread comme lus pour un utilisateur
     */
    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.isRead = true, m.readAt = CURRENT_TIMESTAMP " +
           "WHERE m.thread = :thread AND m.senderUser != :user AND m.isRead = false")
    void markAllAsReadInThreadForUser(@Param("thread") Thread thread, @Param("user") User user);

    /**
     * Trouver le dernier message d'un thread
     */
    @Query("SELECT m FROM Message m WHERE m.thread = :thread ORDER BY m.createdAt DESC")
    List<Message> findLastMessageInThread(@Param("thread") Thread thread, Pageable pageable);

    /**
     * ✅ BONUS : Méthodes supplémentaires utiles
     */
    
    /**
     * Compter le nombre total de messages dans un thread
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.thread = :thread")
    long countByThread(@Param("thread") Thread thread);

    /**
     * Trouver les messages récents d'un utilisateur
     */
    @Query("SELECT m FROM Message m " +
           "JOIN m.thread t " +
           "WHERE (t.ownerUser = :user OR t.finderUser = :user) " +
           "ORDER BY m.createdAt DESC")
    Page<Message> findRecentMessagesForUser(@Param("user") User user, Pageable pageable);

    /**
     * Trouver tous les messages non lus d'un utilisateur
     */
    @Query("SELECT m FROM Message m " +
           "JOIN m.thread t " +
           "WHERE (t.ownerUser = :user OR t.finderUser = :user) " +
           "AND m.senderUser != :user " +
           "AND m.isRead = false " +
           "ORDER BY m.createdAt DESC")
    List<Message> findUnreadMessagesForUser(@Param("user") User user);

    /**
     * Compter les messages par type pour un utilisateur
     */
    @Query("SELECT m.messageType, COUNT(m) FROM Message m " +
           "JOIN m.thread t " +
           "WHERE (t.ownerUser = :user OR t.finderUser = :user) " +
           "GROUP BY m.messageType")
    List<Object[]> countMessagesByTypeForUser(@Param("user") User user);

    /**
     * Vérifier si un utilisateur a accès à un message
     */
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END " +
           "FROM Message m " +
           "JOIN m.thread t " +
           "WHERE m.id = :messageId " +
           "AND (t.ownerUser = :user OR t.finderUser = :user)")
    boolean hasUserAccessToMessage(@Param("messageId") String messageId, @Param("user") User user);

    /**
     * Trouver les threads avec messages non lus pour un utilisateur
     */
    @Query("SELECT DISTINCT t FROM Thread t " +
           "JOIN t.messages m " +
           "WHERE (t.ownerUser = :user OR t.finderUser = :user) " +
           "AND m.senderUser != :user " +
           "AND m.isRead = false")
    List<Thread> findThreadsWithUnreadMessagesForUser(@Param("user") User user);
}