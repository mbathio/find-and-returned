package com.retrouvtout.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entité représentant une conversation
 * Conforme au cahier des charges - Section 3.5 (Messagerie intégrée)
 * Communication directe via la plateforme
 */
@Entity
@Table(name = "threads", indexes = {
    @Index(name = "idx_threads_listing", columnList = "listing_id"),
    @Index(name = "idx_threads_owner", columnList = "owner_user_id"),
    @Index(name = "idx_threads_finder", columnList = "finder_user_id"),
    @Index(name = "idx_threads_status", columnList = "status")
})
@EntityListeners(AuditingEntityListener.class)
public class Thread {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    /**
     * Propriétaire de l'objet - Section 3.1
     * Celui qui a perdu l'objet et contacte le retrouveur
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User ownerUser;

    /**
     * Retrouveur - Section 3.1
     * Celui qui a trouvé l'objet et publié l'annonce
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "finder_user_id", nullable = false)
    private User finderUser;

    /**
     * Statut simple de la conversation
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ThreadStatus status = ThreadStatus.ACTIVE;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "thread", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Message> messages;

    /**
     * Statuts simplifiés conformes au cahier des charges
     */
    public enum ThreadStatus {
        ACTIVE("active"),
        CLOSED("closed");

        private final String value;

        ThreadStatus(String value) { 
            this.value = value; 
        }

        public String getValue() { 
            return value; 
        }

        public static ThreadStatus fromValue(String value) {
            for (ThreadStatus status : ThreadStatus.values()) {
                if (status.value.equals(value)) return status;
            }
            throw new IllegalArgumentException("Statut de thread invalide: " + value);
        }
    }

    // Constructeurs
    public Thread() {}

    // Getters et setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Listing getListing() { return listing; }
    public void setListing(Listing listing) { this.listing = listing; }

    public User getOwnerUser() { return ownerUser; }
    public void setOwnerUser(User ownerUser) { this.ownerUser = ownerUser; }

    public User getFinderUser() { return finderUser; }
    public void setFinderUser(User finderUser) { this.finderUser = finderUser; }

    public ThreadStatus getStatus() { return status; }
    public void setStatus(ThreadStatus status) { this.status = status; }

    public LocalDateTime getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(LocalDateTime lastMessageAt) { this.lastMessageAt = lastMessageAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<Message> getMessages() { return messages; }
    public void setMessages(List<Message> messages) { this.messages = messages; }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
    }
}