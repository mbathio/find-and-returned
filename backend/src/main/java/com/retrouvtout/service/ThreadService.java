package com.retrouvtout.service;

import com.retrouvtout.dto.response.ThreadResponse;
import com.retrouvtout.entity.Listing;
import com.retrouvtout.entity.Thread;
import com.retrouvtout.entity.User;
import com.retrouvtout.exception.ResourceNotFoundException;
import com.retrouvtout.repository.ListingRepository;
import com.retrouvtout.repository.ThreadRepository;
import com.retrouvtout.repository.UserRepository;
import com.retrouvtout.util.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service pour la gestion des threads de conversation
 */
@Service
@Transactional
public class ThreadService {

    private final ThreadRepository threadRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final NotificationService notificationService;

    @Autowired
    public ThreadService(ThreadRepository threadRepository,
                        ListingRepository listingRepository,
                        UserRepository userRepository,
                        ModelMapper modelMapper,
                        NotificationService notificationService) {
        this.threadRepository = threadRepository;
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.notificationService = notificationService;
    }

    /**
     * Créer un nouveau thread de conversation
     */
    public ThreadResponse createThread(String listingId, String ownerUserId) {
        User ownerUser = userRepository.findByIdAndActiveTrue(ownerUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", ownerUserId));

        Listing listing = listingRepository.findById(listingId)
            .orElseThrow(() -> new ResourceNotFoundException("Annonce", "id", listingId));

        // Vérifier que l'utilisateur n'est pas le créateur de l'annonce
        if (listing.getFinderUser().getId().equals(ownerUserId)) {
            throw new IllegalArgumentException("Vous ne pouvez pas créer une conversation avec vous-même");
        }

        // Vérifier qu'une conversation n'existe pas déjà
        if (threadRepository.findByListingAndOwnerUser(listing, ownerUser).isPresent()) {
            throw new IllegalStateException("Une conversation existe déjà pour cette annonce");
        }

        // Créer le thread
        Thread thread = new Thread();
        thread.setId(java.util.UUID.randomUUID().toString());
        thread.setListing(listing);
        thread.setOwnerUser(ownerUser);
        thread.setFinderUser(listing.getFinderUser());
        thread.setStatus(Thread.ThreadStatus.PENDING);
        thread.setApprovedByOwner(false);
        thread.setApprovedByFinder(false);

        Thread savedThread = threadRepository.save(thread);

        // Notifier le retrouveur
        try {
            String title = "Nouvelle demande de contact";
            String body = String.format("Quelqu'un s'intéresse à votre annonce : %s", listing.getTitle());
            String url = "/messages/" + savedThread.getId();

            notificationService.sendPushNotification(
                listing.getFinderUser().getId(), title, body, url);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de la notification: " + e.getMessage());
        }

        return modelMapper.mapThreadToThreadResponse(savedThread);
    }

    /**
     * Obtenir les threads d'un utilisateur
     */
    @Transactional(readOnly = true)
    public Page<ThreadResponse> getUserThreads(String userId, String status, Pageable pageable) {
        User user = userRepository.findByIdAndActiveTrue(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", userId));

        Page<Thread> threads;
        
        if (status != null && !status.trim().isEmpty()) {
            Thread.ThreadStatus threadStatus = Thread.ThreadStatus.fromValue(status);
            threads = threadRepository.findByUserInvolvedAndStatus(user, threadStatus, pageable);
        } else {
            threads = threadRepository.findByUserInvolved(user, pageable);
        }

        return threads.map(modelMapper::mapThreadToThreadResponse);
    }

    /**
     * Obtenir un thread par son ID
     */
    @Transactional(readOnly = true)
    public ThreadResponse getThreadById(String id, String userId) {
        Thread thread = threadRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Thread", "id", id));

        // Vérifier que l'utilisateur fait partie du thread
        if (!thread.getOwnerUser().getId().equals(userId) && 
            !thread.getFinderUser().getId().equals(userId)) {
            throw new SecurityException("Vous n'êtes pas autorisé à accéder à cette conversation");
        }

        return modelMapper.mapThreadToThreadResponse(thread);
    }

    /**
     * Approuver un thread (par le retrouveur)
     */
    public ThreadResponse approveThread(String id, String userId) {
        Thread thread = threadRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Thread", "id", id));

        // Seul le retrouveur peut approuver
        if (!thread.getFinderUser().getId().equals(userId)) {
            throw new SecurityException("Seul le retrouveur peut approuver cette conversation");
        }

        thread.setApprovedByFinder(true);
        
        // Si les deux parties ont approuvé, changer le statut
        if (thread.getApprovedByOwner() && thread.getApprovedByFinder()) {
            thread.setStatus(Thread.ThreadStatus.APPROVED);
        }

        Thread updatedThread = threadRepository.save(thread);

        // Notifier l'autre partie
        try {
            String title = "Conversation approuvée";
            String body = "Votre demande de contact a été acceptée";
            String url = "/messages/" + thread.getId();

            notificationService.sendPushNotification(
                thread.getOwnerUser().getId(), title, body, url);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de la notification: " + e.getMessage());
        }

        return modelMapper.mapThreadToThreadResponse(updatedThread);
    }

    /**
     * Fermer un thread
     */
    public ThreadResponse closeThread(String id, String userId) {
        Thread thread = threadRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Thread", "id", id));

        // Vérifier que l'utilisateur fait partie du thread
        if (!thread.getOwnerUser().getId().equals(userId) && 
            !thread.getFinderUser().getId().equals(userId)) {
            throw new SecurityException("Vous n'êtes pas autorisé à fermer cette conversation");
        }

        thread.setStatus(Thread.ThreadStatus.CLOSED);
        Thread updatedThread = threadRepository.save(thread);

        return modelMapper.mapThreadToThreadResponse(updatedThread);
    }

    /**
     * Obtenir le nombre de conversations non lues pour un utilisateur
     */
    @Transactional(readOnly = true)
    public long getUnreadThreadsCount(String userId) {
        User user = userRepository.findByIdAndActiveTrue(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", userId));

        return threadRepository.countUnreadThreadsForUser(user);
    }
}