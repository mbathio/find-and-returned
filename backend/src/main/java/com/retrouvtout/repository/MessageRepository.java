// MessageRepository.java
package com.retrouvtout.repository;

import com.retrouvtout.entity.Message;
import com.retrouvtout.entity.Thread;
import com.retrouvtout.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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
     * Marquer tous les messages d'un thread comme lus pour un utilisateur
     */
    @Query("UPDATE Message m SET m.isRead = true, m.readAt = CURRENT_TIMESTAMP " +
           "WHERE m.thread = :thread AND m.senderUser != :user AND m.isRead = false")
    void markAllAsReadInThreadForUser(@Param("thread") Thread thread, @Param("user") User user);

    /**
     * Trouver le dernier message d'un thread
     */
    @Query("SELECT m FROM Message m WHERE m.thread = :thread ORDER BY m.createdAt DESC")
    List<Message> findLastMessageInThread(@Param("thread") Thread thread, Pageable pageable);
}
