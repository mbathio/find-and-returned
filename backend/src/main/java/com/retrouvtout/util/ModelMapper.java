package com.retrouvtout.util;

import com.retrouvtout.dto.response.*;
import com.retrouvtout.entity.*;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

/**
 * Utilitaire de mapping entre entités et DTOs
 * ✅ CORRECTION FINALE : Mapping du rôle avec @JsonValue
 */
@Component
public class ModelMapper {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Mapper User vers UserResponse - Section 3.1 (gestion utilisateurs)
     * ✅ CORRECTION: Le rôle sera automatiquement sérialisé avec @JsonValue
     */
    public UserResponse mapUserToUserResponse(User user) {
        if (user == null) return null;

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        // ✅ user.getRole().getValue() sera appelé automatiquement grâce à @JsonValue
        response.setRole(user.getRole().getValue()); 
        response.setEmailVerified(user.getEmailVerified());
        response.setActive(user.getActive());
        response.setCreatedAt(user.getCreatedAt());
        response.setLastLoginAt(user.getLastLoginAt());

        return response;
    }

    /**
     * Mapper Listing vers ListingResponse - EXACTEMENT conforme au frontend
     */
    public ListingResponse mapListingToListingResponse(Listing listing) {
        if (listing == null) return null;

        ListingResponse response = new ListingResponse();
        response.setId(listing.getId());
        response.setTitle(listing.getTitle());
        response.setCategory(listing.getCategory().getValue());
        response.setLocationText(listing.getLocationText());
        response.setLatitude(listing.getLatitude());
        response.setLongitude(listing.getLongitude());
        
        // Conversion des dates en format ISO string pour le frontend
        response.setFoundAt(listing.getFoundAt().format(ISO_FORMATTER));
        response.setCreatedAt(listing.getCreatedAt().format(ISO_FORMATTER));
        response.setUpdatedAt(listing.getUpdatedAt().format(ISO_FORMATTER));
        
        response.setDescription(listing.getDescription());
        response.setImageUrl(listing.getImageUrl());
        
        // Conversion du statut pour le frontend (resolu -> resolved)
        String frontendStatus = listing.getStatus() == Listing.ListingStatus.RESOLU ? 
            "resolved" : listing.getStatus().getValue();
        response.setStatus(frontendStatus);
        
        // ID utilisateur simple comme attendu par le frontend
        response.setFinderUserId(listing.getFinderUser().getId());

        return response;
    }

    /**
     * Mapper Thread vers ThreadResponse - Section 3.5 (messagerie)
     * ✅ CORRECTION: Mapping du rôle utilisateur avec @JsonValue
     */
    public ThreadResponse mapThreadToThreadResponse(com.retrouvtout.entity.Thread thread) {
        if (thread == null) return null;

        ThreadResponse response = new ThreadResponse();
        response.setId(thread.getId());
        response.setStatus(thread.getStatus().getValue());
        response.setLastMessageAt(thread.getLastMessageAt());
        response.setCreatedAt(thread.getCreatedAt());
        response.setUpdatedAt(thread.getUpdatedAt());

        // Mapper l'annonce liée
        if (thread.getListing() != null) {
            response.setListing(mapListingToListingResponse(thread.getListing()));
        }

        // Mapper les utilisateurs (masquage des infos personnelles - Section 3.4)
        if (thread.getOwnerUser() != null) {
            UserResponse ownerUser = new UserResponse();
            ownerUser.setId(thread.getOwnerUser().getId());
            ownerUser.setName(thread.getOwnerUser().getName());
            // ✅ CORRECTION: Utilisation de getValue() pour le rôle
            ownerUser.setRole(thread.getOwnerUser().getRole().getValue());
            response.setOwnerUser(ownerUser);
        }

        if (thread.getFinderUser() != null) {
            UserResponse finderUser = new UserResponse();
            finderUser.setId(thread.getFinderUser().getId());
            finderUser.setName(thread.getFinderUser().getName());
            // ✅ CORRECTION: Utilisation de getValue() pour le rôle
            finderUser.setRole(thread.getFinderUser().getRole().getValue());
            response.setFinderUser(finderUser);
        }

        return response;
    }

    /**
     * Mapper Message vers MessageResponse - Section 3.5 (messagerie)
     * ✅ CORRECTION: Mapping du rôle utilisateur avec @JsonValue
     */
    public MessageResponse mapMessageToMessageResponse(Message message) {
        if (message == null) return null;

        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setThreadId(message.getThread().getId());
        response.setBody(message.getBody());
        response.setMessageType(message.getMessageType().getValue());
        response.setIsRead(message.getIsRead());
        response.setReadAt(message.getReadAt());
        response.setCreatedAt(message.getCreatedAt());

        // Mapper l'expéditeur (informations limitées - Section 3.4)
        if (message.getSenderUser() != null) {
            UserResponse senderUser = new UserResponse();
            senderUser.setId(message.getSenderUser().getId());
            senderUser.setName(message.getSenderUser().getName());
            // ✅ CORRECTION: Utilisation de getValue() pour le rôle
            senderUser.setRole(message.getSenderUser().getRole().getValue());
            response.setSenderUser(senderUser);
        }

        return response;
    }

    /**
     * Créer une PagedResponse conforme au frontend
     */
    public <T> PagedResponse<T> createPagedResponse(
            java.util.List<T> items, 
            int page, 
            int pageSize, 
            long totalElements) {
        
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);

        return new PagedResponse<>(items, totalElements, page, totalPages);
    }
}