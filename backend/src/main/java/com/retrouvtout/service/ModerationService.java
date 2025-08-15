package com.retrouvtout.service;

import com.retrouvtout.controller.ModerationController.ModerationStatsResponse;
import com.retrouvtout.dto.request.CreateModerationFlagRequest;
import com.retrouvtout.dto.response.ModerationFlagResponse;
import com.retrouvtout.entity.ModerationFlag;
import com.retrouvtout.entity.User;
import com.retrouvtout.exception.ResourceNotFoundException;
import com.retrouvtout.repository.ModerationFlagRepository;
import com.retrouvtout.repository.UserRepository;
import com.retrouvtout.util.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service pour la modération des contenus
 */
@Service
@Transactional
public class ModerationService {

    private final ModerationFlagRepository flagRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final NotificationService notificationService;

    @Autowired
    public ModerationService(ModerationFlagRepository flagRepository,
                           UserRepository userRepository,
                           ModelMapper modelMapper,
                           NotificationService notificationService) {
        this.flagRepository = flagRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.notificationService = notificationService;
    }

    /**
     * Créer un nouveau signalement
     */
    public ModerationFlagResponse createFlag(CreateModerationFlagRequest request, String userId) {
        User user = userRepository.findByIdAndActiveTrue(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", userId));

        // Vérifier que le type d'entité est valide
        ModerationFlag.EntityType entityType;
        try {
            entityType = ModerationFlag.EntityType.fromValue(request.getEntityType());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Type d'entité invalide: " + request.getEntityType());
        }

        // Vérifier que la priorité est valide
        ModerationFlag.FlagPriority priority;
        try {
            priority = ModerationFlag.FlagPriority.fromValue(request.getPriority());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Priorité invalide: " + request.getPriority());
        }

        // Créer le signalement
        ModerationFlag flag = new ModerationFlag();
        flag.setEntityType(entityType);
        flag.setEntityId(request.getEntityId());
        flag.setReason(request.getReason());
        flag.setDescription(request.getDescription());
        flag.setPriority(priority);
        flag.setStatus(ModerationFlag.FlagStatus.PENDING);
        flag.setCreatedByUser(user);

        ModerationFlag savedFlag = flagRepository.save(flag);

        // Notifier les modérateurs
        notifyModerators(savedFlag);

        return modelMapper.mapModerationFlagToModerationFlagResponse(savedFlag);
    }

    /**
     * Obtenir les signalements avec filtres
     */
    @Transactional(readOnly = true)
    public Page<ModerationFlagResponse> getFlags(String status, String priority, 
                                               String entityType, Pageable pageable) {
        
        Specification<ModerationFlag> spec = Specification.where(null);

        // Filtre par statut
        if (status != null && !status.trim().isEmpty()) {
            ModerationFlag.FlagStatus flagStatus = ModerationFlag.FlagStatus.fromValue(status);
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), flagStatus));
        }

        // Filtre par priorité
        if (priority != null && !priority.trim().isEmpty()) {
            ModerationFlag.FlagPriority flagPriority = ModerationFlag.FlagPriority.fromValue(priority);
            spec = spec.and((root, query, cb) -> cb.equal(root.get("priority"), flagPriority));
        }

        // Filtre par type d'entité
        if (entityType != null && !entityType.trim().isEmpty()) {
            ModerationFlag.EntityType flagEntityType = ModerationFlag.EntityType.fromValue(entityType);
            spec = spec.and((root, query, cb) -> cb.equal(root.get("entityType"), flagEntityType));
        }

        Page<ModerationFlag> flags = flagRepository.findAll(spec, pageable);
        return flags.map(modelMapper::mapModerationFlagToModerationFlagResponse);
    }

    /**
     * Approuver un signalement
     */
    public ModerationFlagResponse approveFlag(Long id, String moderatorId) {
        ModerationFlag flag = flagRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Signalement", "id", id));

        User moderator = userRepository.findByIdAndActiveTrue(moderatorId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", moderatorId));

        flag.setStatus(ModerationFlag.FlagStatus.APPROVED);
        flag.setReviewedByUser(moderator);
        flag.setReviewedAt(LocalDateTime.now());

        ModerationFlag updatedFlag = flagRepository.save(flag);

        // Appliquer les actions de modération selon le type d'entité
        applyModerationAction(flag);

        // Notifier le créateur du signalement
        notifyFlagCreator(flag, "approved");

        return modelMapper.mapModerationFlagToModerationFlagResponse(updatedFlag);
    }

    /**
     * Rejeter un signalement
     */
    public ModerationFlagResponse rejectFlag(Long id, String moderatorId) {
        ModerationFlag flag = flagRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Signalement", "id", id));

        User moderator = userRepository.findByIdAndActiveTrue(moderatorId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", moderatorId));

        flag.setStatus(ModerationFlag.FlagStatus.REJECTED);
        flag.setReviewedByUser(moderator);
        flag.setReviewedAt(LocalDateTime.now());

        ModerationFlag updatedFlag = flagRepository.save(flag);

        // Notifier le créateur du signalement
        notifyFlagCreator(flag, "rejected");

        return modelMapper.mapModerationFlagToModerationFlagResponse(updatedFlag);
    }

    /**
     * Obtenir les statistiques de modération
     */
    @Transactional(readOnly = true)
    public ModerationStatsResponse getModerationStats() {
        long pendingFlags = flagRepository.countByStatus(ModerationFlag.FlagStatus.PENDING);
        long approvedFlags = flagRepository.countByStatus(ModerationFlag.FlagStatus.APPROVED);
        long rejectedFlags = flagRepository.countByStatus(ModerationFlag.FlagStatus.REJECTED);
        long totalFlags = flagRepository.count();

        return new ModerationStatsResponse(pendingFlags, approvedFlags, rejectedFlags, totalFlags);
    }

    /**
     * Notifier les modérateurs d'un nouveau signalement
     */
    private void notifyModerators(ModerationFlag flag) {
        try {
            String title = "Nouveau signalement";
            String body = String.format("Nouveau signalement %s: %s", 
                flag.getEntityType().getValue(), flag.getReason());
            String url = "/moderation/flags/" + flag.getId();

            // TODO: Récupérer la liste des modérateurs et leur envoyer des notifications
            // Pour l'instant, on se contente d'un log
            System.out.println("Nouveau signalement créé: " + flag.getReason());

        } catch (Exception e) {
            System.err.println("Erreur lors de la notification des modérateurs: " + e.getMessage());
        }
    }

    /**
     * Appliquer l'action de modération selon le type d'entité
     */
    private void applyModerationAction(ModerationFlag flag) {
        try {
            switch (flag.getEntityType()) {
                case LISTING:
                    // Suspendre l'annonce
                    // TODO: Implémenter la suspension d'annonce
                    System.out.println("Suspension de l'annonce: " + flag.getEntityId());
                    break;
                
                case MESSAGE:
                    // Supprimer le message
                    // TODO: Implémenter la suppression de message
                    System.out.println("Suppression du message: " + flag.getEntityId());
                    break;
                
                case USER:
                    // Suspendre l'utilisateur
                    // TODO: Implémenter la suspension d'utilisateur
                    System.out.println("Suspension de l'utilisateur: " + flag.getEntityId());
                    break;
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'application de l'action de modération: " + e.getMessage());
        }
    }

    /**
     * Notifier le créateur du signalement du résultat
     */
    private void notifyFlagCreator(ModerationFlag flag, String action) {
        try {
            if (flag.getCreatedByUser() != null) {
                String title = "Signalement traité";
                String body = String.format("Votre signalement a été %s par la modération", 
                    "approved".equals(action) ? "approuvé" : "rejeté");
                
                notificationService.sendPushNotification(
                    flag.getCreatedByUser().getId(), title, body, "/profil");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la notification du créateur: " + e.getMessage());
        }
    }
}