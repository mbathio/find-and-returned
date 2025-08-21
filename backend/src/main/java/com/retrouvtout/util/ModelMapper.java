package com.retrouvtout.util;

import com.retrouvtout.dto.response.*;
import com.retrouvtout.entity.*;
import org.springframework.stereotype.Component;

/**
 * Utilitaire de mapping entre entités et DTOs
 * Conforme au cahier des charges - mapping simple sans complexité
 */
@Component
public class ModelMapper {

    /**
     * Mapper User vers UserResponse - Section 3.1 (gestion utilisateurs)
     */
    public UserResponse mapUserToUserResponse(User user) {
        if (user == null) return null;

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole().getValue());
        response.setEmailVerified(user.getEmailVerified());
        response.setActive(user.getActive());
        response.setCreatedAt(user.getCreatedAt());
        response.setLastLoginAt(user.getLastLoginAt());

        return response;
    }

    /**
     * Mapper Listing vers ListingResponse - Section 3.2 (gestion annonces)
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
        response.setFoundAt(listing.getFoundAt());
        response.setDescription(listing.getDescription());
        response.setImageUrl(listing.getImageUrl());
        response.setStatus(listing.getStatus().getValue());
        response.setViewsCount(listing.getViewsCount());
        response.setIsModerated(listing.getIsModerated());
        response.setCreatedAt(listing.getCreatedAt());
        response.setUpdatedAt(listing.getUpdatedAt());

        // Mapper l'utilisateur retrouveur
        if (listing.getFinderUser() != null) {
            // Version publique sans informations sensibles - Section 3.4
            UserResponse finderUser = new UserResponse();
            finderUser.setId(listing.getFinderUser().getId());
            finderUser.setName(listing.getFinderUser().getName());
            // Email et téléphone masqués pour la confidentialité
            finderUser.setRole(listing.getFinderUser().getRole().getValue());
            response.setFinderUser(finderUser);
        }

        return response;
    }

    /**
     * Mapper Thread vers ThreadResponse - Section 3.5 (messagerie)
     */
    public ThreadResponse mapThreadToThreadResponse(Thread thread) {
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
            ownerUser.setRole(thread.getOwnerUser().getRole().getValue());
            response.setOwnerUser(ownerUser);
        }

        if (thread.getFinderUser() != null) {
            UserResponse finderUser = new UserResponse();
            finderUser.setId(thread.getFinderUser().getId());
            finderUser.setName(thread.getFinderUser().getName());
            finderUser.setRole(thread.getFinderUser().getRole().getValue());
            response.setFinderUser(finderUser);
        }

        return response;
    }

    /**
     * Mapper Message vers MessageResponse - Section 3.5 (messagerie)
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
            senderUser.setRole(message.getSenderUser().getRole().getValue());
            response.setSenderUser(senderUser);
        }

        return response;
    }

    /**
     * Créer une PagedResponse générique
     */
    public <T> PagedResponse<T> createPagedResponse(
            java.util.List<T> items, 
            int page, 
            int pageSize, 
            long totalElements) {
        
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        boolean isLast = page >= totalPages;

        return new PagedResponse<>(items, page, pageSize, totalElements, totalPages, isLast);
    }
}