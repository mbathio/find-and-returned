package com.retrouvtout.util;

import com.retrouvtout.dto.response.*;
import com.retrouvtout.entity.*;
import com.retrouvtout.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Utilitaire pour mapper les entités vers les DTOs de réponse
 */
@Component
public class ModelMapper {

    @Autowired
    private MessageRepository messageRepository;

    /**
     * Mapper User vers UserResponse
     */
    public UserResponse mapUserToUserResponse(User user) {
        if (user == null) {
            return null;
        }

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole() != null ? user.getRole().getValue() : null);
        response.setEmailVerified(user.getEmailVerified());
        response.setActive(user.getActive());
        response.setCreatedAt(user.getCreatedAt());
        response.setLastLoginAt(user.getLastLoginAt());

        return response;
    }

    /**
     * Mapper User vers UserResponse (version publique sans informations sensibles)
     */
    public UserResponse mapUserToPublicUserResponse(User user) {
        if (user == null) {
            return null;
        }

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        // Ne pas inclure l'email et le téléphone dans la version publique
        response.setRole(user.getRole() != null ? user.getRole().getValue() : null);
        response.setCreatedAt(user.getCreatedAt());

        return response;
    }

    /**
     * Mapper Listing vers ListingResponse
     */
    public ListingResponse mapListingToListingResponse(Listing listing) {
        if (listing == null) {
            return null;
        }

        ListingResponse response = new ListingResponse();
        response.setId(listing.getId());
        response.setTitle(listing.getTitle());
        response.setCategory(listing.getCategory() != null ? listing.getCategory().getValue() : null);
        response.setLocationText(listing.getLocationText());
        response.setLatitude(listing.getLatitude());
        response.setLongitude(listing.getLongitude());
        response.setFoundAt(listing.getFoundAt());
        response.setDescription(listing.getDescription());
        response.setImageUrl(listing.getImageUrl());
        response.setStatus(listing.getStatus() != null ? listing.getStatus().getValue() : null);
        response.setViewsCount(listing.getViewsCount());
        response.setIsModerated(listing.getIsModerated());
        response.setCreatedAt(listing.getCreatedAt());
        response.setUpdatedAt(listing.getUpdatedAt());

        // Mapper l'utilisateur retrouveur (version publique)
        if (listing.getFinderUser() != null) {
            response.setFinderUser(mapUserToPublicUserResponse(listing.getFinderUser()));
        }

        return response;
    }

    /**
     * Mapper Alert vers AlertResponse
     */
    public AlertResponse mapAlertToAlertResponse(Alert alert) {
        if (alert == null) {
            return null;
        }

        AlertResponse response = new AlertResponse();
        response.setId(alert.getId());
        response.setTitle(alert.getTitle());
        response.setQueryText(alert.getQueryText());
        response.setCategory(alert.getCategory());
        response.setLocationText(alert.getLocationText());
        response.setLatitude(alert.getLatitude());
        response.setLongitude(alert.getLongitude());
        response.setRadiusKm(alert.getRadiusKm());
        response.setDateFrom(alert.getDateFrom());
        response.setDateTo(alert.getDateTo());
        response.setChannels(alert.getChannels());
        response.setActive(alert.getActive());
        response.setLastTriggeredAt(alert.getLastTriggeredAt());
        response.setCreatedAt(alert.getCreatedAt());
        response.setUpdatedAt(alert.getUpdatedAt());

        return response;
    }

    /**
     * Mapper Message vers MessageResponse
     */
    public MessageResponse mapMessageToMessageResponse(Message message) {
        if (message == null) {
            return null;
        }

        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setThreadId(message.getThread().getId());
        response.setSenderUser(mapUserToPublicUserResponse(message.getSenderUser()));
        response.setBody(message.getBody());
        response.setMessageType(message.getMessageType() != null ? message.getMessageType().getValue() : null);
        response.setIsRead(message.getIsRead());
        response.setReadAt(message.getReadAt());
        response.setCreatedAt(message.getCreatedAt());

        return response;
    }

    /**
     * Mapper Thread vers ThreadResponse
     */
    public ThreadResponse mapThreadToThreadResponse(com.retrouvtout.entity.Thread thread) {
        if (thread == null) {
            return null;
        }

        ThreadResponse response = new ThreadResponse();
        response.setId(thread.getId());
        response.setListing(mapListingToListingResponse(thread.getListing()));
        response.setOwnerUser(mapUserToPublicUserResponse(thread.getOwnerUser()));
        response.setFinderUser(mapUserToPublicUserResponse(thread.getFinderUser()));
        response.setStatus(thread.getStatus() != null ? thread.getStatus().getValue() : null);
        response.setApprovedByOwner(thread.getApprovedByOwner());
        response.setApprovedByFinder(thread.getApprovedByFinder());
        response.setLastMessageAt(thread.getLastMessageAt());
        response.setCreatedAt(thread.getCreatedAt());
        response.setUpdatedAt(thread.getUpdatedAt());

        // Ajouter le dernier message
        try {
            List<Message> lastMessages = messageRepository.findLastMessageInThread(
                thread, PageRequest.of(0, 1));
            if (!lastMessages.isEmpty()) {
                response.setLastMessage(mapMessageToMessageResponse(lastMessages.get(0)));
            }
        } catch (Exception e) {
            // Ignorer l'erreur si on ne peut pas récupérer le dernier message
        }

        return response;
    }

    /**
     * Mapper Confirmation vers ConfirmationResponse
     */
    public ConfirmationResponse mapConfirmationToConfirmationResponse(Confirmation confirmation) {
        if (confirmation == null) {
            return null;
        }

        ConfirmationResponse response = new ConfirmationResponse();
        response.setId(confirmation.getId());
        response.setThreadId(confirmation.getThread().getId());
        response.setCode(confirmation.getCode());
        response.setExpiresAt(confirmation.getExpiresAt());
        response.setUsedAt(confirmation.getUsedAt());
        response.setCreatedAt(confirmation.getCreatedAt());
        
        // Calculer les statuts
        response.setIsExpired(confirmation.isExpired());
        response.setIsUsed(confirmation.isUsed());

        if (confirmation.getUsedByUser() != null) {
            response.setUsedByUser(mapUserToPublicUserResponse(confirmation.getUsedByUser()));
        }

        return response;
    }

    /**
     * Mapper ModerationFlag vers ModerationFlagResponse
     */
    public ModerationFlagResponse mapModerationFlagToModerationFlagResponse(ModerationFlag flag) {
        if (flag == null) {
            return null;
        }

        ModerationFlagResponse response = new ModerationFlagResponse();
        response.setId(flag.getId());
        response.setEntityType(flag.getEntityType() != null ? flag.getEntityType().getValue() : null);
        response.setEntityId(flag.getEntityId());
        response.setReason(flag.getReason());
        response.setDescription(flag.getDescription());
        response.setStatus(flag.getStatus() != null ? flag.getStatus().getValue() : null);
        response.setPriority(flag.getPriority() != null ? flag.getPriority().getValue() : null);
        response.setCreatedAt(flag.getCreatedAt());
        response.setReviewedAt(flag.getReviewedAt());

        if (flag.getCreatedByUser() != null) {
            response.setCreatedByUser(mapUserToPublicUserResponse(flag.getCreatedByUser()));
        }

        if (flag.getReviewedByUser() != null) {
            response.setReviewedByUser(mapUserToPublicUserResponse(flag.getReviewedByUser()));
        }

        return response;
    }
}