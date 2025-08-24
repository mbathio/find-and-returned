// backend/src/main/java/com/retrouvtout/util/ModelMapper.java - VERSION SÉCURISÉE

package com.retrouvtout.util;

import com.retrouvtout.dto.response.*;
import com.retrouvtout.entity.*;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

/**
 * ✅ CORRECTION MAJEURE : ModelMapper avec protection contre les valeurs null
 */
@Component
public class ModelMapper {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Mapper User vers UserResponse avec protection null
     */
    public UserResponse mapUserToUserResponse(User user) {
        if (user == null) return null;

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        
        // ✅ Protection contre null sur le rôle
        if (user.getRole() != null) {
            response.setRole(user.getRole().getValue());
        } else {
            response.setRole("mixte"); // Valeur par défaut
        }
        
        response.setEmailVerified(user.getEmailVerified());
        response.setActive(user.getActive());
        response.setCreatedAt(user.getCreatedAt());
        response.setLastLoginAt(user.getLastLoginAt());

        return response;
    }

    /**
     * ✅ CORRECTION MAJEURE : Mapper Listing vers ListingResponse avec protection complète
     */
    public ListingResponse mapListingToListingResponse(Listing listing) {
        if (listing == null) {
            System.err.println("⚠️ Tentative de mapper un Listing null");
            return null;
        }

        try {
            ListingResponse response = new ListingResponse();
            
            // Champs simples avec protection null
            response.setId(listing.getId());
            response.setTitle(listing.getTitle() != null ? listing.getTitle() : "");
            response.setLocationText(listing.getLocationText() != null ? listing.getLocationText() : "");
            response.setLatitude(listing.getLatitude());
            response.setLongitude(listing.getLongitude());
            response.setDescription(listing.getDescription() != null ? listing.getDescription() : "");
            response.setImageUrl(listing.getImageUrl());
            
            // ✅ PROTECTION CATÉGORIE
            if (listing.getCategory() != null) {
                response.setCategory(listing.getCategory().getValue());
            } else {
                System.err.println("⚠️ Catégorie null pour listing " + listing.getId());
                response.setCategory("autre"); // Valeur par défaut
            }
            
            // ✅ PROTECTION FOUNDATAT (CRITIQUE)
            if (listing.getFoundAt() != null) {
                response.setFoundAt(listing.getFoundAt().format(ISO_FORMATTER));
            } else {
                System.err.println("❌ CRITICAL: foundAt est null pour listing " + listing.getId());
                // Utiliser une date par défaut ou lever une exception
                response.setFoundAt(java.time.LocalDateTime.now().format(ISO_FORMATTER));
            }
            
            // ✅ PROTECTION DATES D'AUDIT
            if (listing.getCreatedAt() != null) {
                response.setCreatedAt(listing.getCreatedAt().format(ISO_FORMATTER));
            } else {
                System.err.println("⚠️ createdAt null pour listing " + listing.getId());
                response.setCreatedAt(java.time.LocalDateTime.now().format(ISO_FORMATTER));
            }
            
            if (listing.getUpdatedAt() != null) {
                response.setUpdatedAt(listing.getUpdatedAt().format(ISO_FORMATTER));
            } else {
                System.err.println("⚠️ updatedAt null pour listing " + listing.getId());
                response.setUpdatedAt(java.time.LocalDateTime.now().format(ISO_FORMATTER));
            }
            
            // ✅ PROTECTION STATUS
            if (listing.getStatus() != null) {
                String frontendStatus = listing.getStatus() == Listing.ListingStatus.RESOLU ? 
                    "resolved" : listing.getStatus().getValue();
                response.setStatus(frontendStatus);
            } else {
                System.err.println("⚠️ Status null pour listing " + listing.getId());
                response.setStatus("active"); // Valeur par défaut
            }
            
            // ✅ PROTECTION USER
            if (listing.getFinderUser() != null && listing.getFinderUser().getId() != null) {
                response.setFinderUserId(listing.getFinderUser().getId());
            } else {
                System.err.println("❌ CRITICAL: finderUser ou finderUser.id null pour listing " + listing.getId());
                response.setFinderUserId("unknown"); // ou lever une exception
            }

            return response;
            
        } catch (Exception e) {
            System.err.println("❌ Erreur dans mapListingToListingResponse pour listing " + 
                (listing.getId() != null ? listing.getId() : "null") + ": " + e.getMessage());
            e.printStackTrace();
            
            // Retourner un objet minimal plutôt que null pour éviter de casser l'application
            ListingResponse errorResponse = new ListingResponse();
            errorResponse.setId(listing.getId() != null ? listing.getId() : "error-" + System.currentTimeMillis());
            errorResponse.setTitle("Erreur de chargement");
            errorResponse.setCategory("autre");
            errorResponse.setLocationText("Lieu non disponible");
            errorResponse.setFoundAt(java.time.LocalDateTime.now().format(ISO_FORMATTER));
            errorResponse.setCreatedAt(java.time.LocalDateTime.now().format(ISO_FORMATTER));
            errorResponse.setUpdatedAt(java.time.LocalDateTime.now().format(ISO_FORMATTER));
            errorResponse.setDescription("Erreur lors du chargement de cette annonce");
            errorResponse.setStatus("active");
            errorResponse.setFinderUserId("unknown");
            
            return errorResponse;
        }
    }

    /**
     * Mapper Thread vers ThreadResponse avec protection null
     */
    public ThreadResponse mapThreadToThreadResponse(com.retrouvtout.entity.Thread thread) {
        if (thread == null) return null;

        ThreadResponse response = new ThreadResponse();
        response.setId(thread.getId());
        
        // ✅ Protection status
        if (thread.getStatus() != null) {
            response.setStatus(thread.getStatus().getValue());
        } else {
            response.setStatus("active");
        }
        
        response.setLastMessageAt(thread.getLastMessageAt());
        response.setCreatedAt(thread.getCreatedAt());
        response.setUpdatedAt(thread.getUpdatedAt());

        // Mapper l'annonce liée avec protection
        if (thread.getListing() != null) {
            response.setListing(mapListingToListingResponse(thread.getListing()));
        }

        // Mapper les utilisateurs avec protection
        if (thread.getOwnerUser() != null) {
            UserResponse ownerUser = new UserResponse();
            ownerUser.setId(thread.getOwnerUser().getId());
            ownerUser.setName(thread.getOwnerUser().getName());
            if (thread.getOwnerUser().getRole() != null) {
                ownerUser.setRole(thread.getOwnerUser().getRole().getValue());
            } else {
                ownerUser.setRole("mixte");
            }
            response.setOwnerUser(ownerUser);
        }

        if (thread.getFinderUser() != null) {
            UserResponse finderUser = new UserResponse();
            finderUser.setId(thread.getFinderUser().getId());
            finderUser.setName(thread.getFinderUser().getName());
            if (thread.getFinderUser().getRole() != null) {
                finderUser.setRole(thread.getFinderUser().getRole().getValue());
            } else {
                finderUser.setRole("mixte");
            }
            response.setFinderUser(finderUser);
        }

        return response;
    }

    /**
     * Mapper Message vers MessageResponse avec protection null
     */
    public MessageResponse mapMessageToMessageResponse(Message message) {
        if (message == null) return null;

        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        
        // ✅ Protection thread
        if (message.getThread() != null) {
            response.setThreadId(message.getThread().getId());
        }
        
        response.setBody(message.getBody());
        
        // ✅ Protection message type
        if (message.getMessageType() != null) {
            response.setMessageType(message.getMessageType().getValue());
        } else {
            response.setMessageType("text");
        }
        
        response.setIsRead(message.getIsRead() != null ? message.getIsRead() : false);
        response.setReadAt(message.getReadAt());
        response.setCreatedAt(message.getCreatedAt());

        // Mapper l'expéditeur avec protection
        if (message.getSenderUser() != null) {
            UserResponse senderUser = new UserResponse();
            senderUser.setId(message.getSenderUser().getId());
            senderUser.setName(message.getSenderUser().getName());
            if (message.getSenderUser().getRole() != null) {
                senderUser.setRole(message.getSenderUser().getRole().getValue());
            } else {
                senderUser.setRole("mixte");
            }
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
        
        // ✅ Protection contre les valeurs négatives
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 20;
        if (totalElements < 0) totalElements = 0;
        
        int totalPages = pageSize > 0 ? (int) Math.ceil((double) totalElements / pageSize) : 0;

        return new PagedResponse<>(
            items != null ? items : java.util.Collections.emptyList(), 
            totalElements, 
            page, 
            totalPages
        );
    }
}