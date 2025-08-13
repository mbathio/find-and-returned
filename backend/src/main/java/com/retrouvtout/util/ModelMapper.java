package com.retrouvtout.util;

import com.retrouvtout.dto.response.AlertResponse;
import com.retrouvtout.dto.response.ListingResponse;
import com.retrouvtout.dto.response.UserResponse;
import com.retrouvtout.entity.Alert;
import com.retrouvtout.entity.Listing;
import com.retrouvtout.entity.User;
import org.springframework.stereotype.Component;

/**
 * Utilitaire pour mapper les entités vers les DTOs de réponse
 */
@Component
public class ModelMapper {

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
}