package com.retrouvtout.service;

import com.retrouvtout.dto.response.PagedResponse;
import com.retrouvtout.dto.response.ThreadResponse;
import com.retrouvtout.entity.Listing;
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

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service pour la gestion des threads de conversation
 * CORRIGÉ - Version finale avec mapping correct
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

        // Créer le thread SIMPLIFIÉ
        com.retrouvtout.entity.Thread thread = new com.retrouvtout.entity.Thread();
        thread.setId(java.util.UUID.randomUUID().toString());
        thread.setListing(listing);
        thread.setOwnerUser(ownerUser);
        thread.setFinderUser(listing.getFinderUser());
        thread.setStatus(com.retrouvtout.entity.Thread.ThreadStatus.ACTIVE);

        com.retrouvtout.entity.Thread savedThread = threadRepository.save(thread);

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
    public PagedResponse<ThreadResponse> getUserThreads(String userId, String status, Pageable pageable) {
        User user = userRepository.findByIdAndActiveTrue(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", userId));

        Page<com.retrouvtout.entity.Thread> threads;
        
        if (status != null && !status.trim().isEmpty()) {
            com.retrouvtout.entity.Thread.ThreadStatus threadStatus = 
                com.retrouvtout.entity.Thread.ThreadStatus.fromValue(status);
            threads = threadRepository.findByUserInvolvedAndStatus(user, threadStatus, pageable);
        } else {
            threads = threadRepository.findByUserInvolved(user, pageable);
        }

        // Conversion manuelle pour éviter l'erreur de type
        List<ThreadResponse> threadResponses = threads.getContent().stream()
            .map(modelMapper::mapThreadToThreadResponse)
            .collect(Collectors.toList());

        return modelMapper.createPagedResponse(
            threadResponses,
            pageable.getPageNumber() + 1,
            pageable.getPageSize(),
            threads.getTotalElements()
        );
    }

    /**
     * Obtenir un thread par son ID
     */
    @Transactional(readOnly = true)
    public ThreadResponse getThreadById(String id, String userId) {
        com.retrouvtout.entity.Thread thread = threadRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Thread", "id", id));

        // Vérifier que l'utilisateur fait partie du thread
        if (!thread.getOwnerUser().getId().equals(userId) && 
            !thread.getFinderUser().getId().equals(userId)) {
            throw new SecurityException("Vous n'êtes pas autorisé à accéder à cette conversation");
        }

        return modelMapper.mapThreadToThreadResponse(thread);
    }

    /**
     * Fermer un thread
     */
    public ThreadResponse closeThread(String id, String userId) {
        com.retrouvtout.entity.Thread thread = threadRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Thread", "id", id));

        // Vérifier que l'utilisateur fait partie du thread
        if (!thread.getOwnerUser().getId().equals(userId) && 
            !thread.getFinderUser().getId().equals(userId)) {
            throw new SecurityException("Vous n'êtes pas autorisé à fermer cette conversation");
        }

        thread.setStatus(com.retrouvtout.entity.Thread.ThreadStatus.CLOSED);
        com.retrouvtout.entity.Thread updatedThread = threadRepository.save(thread);

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