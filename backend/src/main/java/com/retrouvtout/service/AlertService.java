package com.retrouvtout.service;

import com.retrouvtout.dto.request.CreateAlertRequest;
import com.retrouvtout.dto.response.AlertResponse;
import com.retrouvtout.entity.Alert;
import com.retrouvtout.entity.Listing;
import com.retrouvtout.entity.User;
import com.retrouvtout.exception.ResourceNotFoundException;
import com.retrouvtout.repository.AlertRepository;
import com.retrouvtout.repository.UserRepository;
import com.retrouvtout.util.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service pour la gestion des alertes d'objets perdus
 */
@Service
@Transactional
public class AlertService {

    private final AlertRepository alertRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final EmailService emailService;
    private final SmsService smsService;
    private final NotificationService notificationService;

    @Autowired
    public AlertService(AlertRepository alertRepository,
                       UserRepository userRepository,
                       ModelMapper modelMapper,
                       EmailService emailService,
                       SmsService smsService,
                       NotificationService notificationService) {
        this.alertRepository = alertRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.emailService = emailService;
        this.smsService = smsService;
        this.notificationService = notificationService;
    }

    /**
     * Créer une nouvelle alerte
     */
    public AlertResponse createAlert(CreateAlertRequest request, String userId) {
        User user = userRepository.findByIdAndActiveTrue(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", userId));

        Alert alert = new Alert();
        alert.setId(java.util.UUID.randomUUID().toString());
        alert.setOwnerUser(user);
        alert.setTitle(request.getTitle());
        alert.setQueryText(request.getQueryText());
        alert.setCategory(request.getCategory());
        alert.setLocationText(request.getLocationText());
        alert.setLatitude(request.getLatitude());
        alert.setLongitude(request.getLongitude());
        alert.setRadiusKm(request.getRadiusKm());
        alert.setDateFrom(request.getDateFrom());
        alert.setDateTo(request.getDateTo());
        alert.setChannels(request.getChannels());
        alert.setActive(true);

        Alert savedAlert = alertRepository.save(alert);
        return modelMapper.mapAlertToAlertResponse(savedAlert);
    }

    /**
     * Obtenir les alertes d'un utilisateur
     */
    @Transactional(readOnly = true)
    public Page<AlertResponse> getUserAlerts(String userId, Pageable pageable) {
        User user = userRepository.findByIdAndActiveTrue(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", userId));

        Page<Alert> alerts = alertRepository.findByOwnerUserAndActiveTrueOrderByCreatedAtDesc(user, pageable);
        return alerts.map(modelMapper::mapAlertToAlertResponse);
    }

    /**
     * Obtenir une alerte par son ID
     */
    @Transactional(readOnly = true)
    public AlertResponse getAlertById(String id, String userId) {
        Alert alert = alertRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Alerte", "id", id));

        // Vérifier que l'utilisateur est propriétaire de l'alerte
        if (!alert.getOwnerUser().getId().equals(userId)) {
            throw new SecurityException("Vous n'êtes pas autorisé à accéder à cette alerte");
        }

        return modelMapper.mapAlertToAlertResponse(alert);
    }

    /**
     * Mettre à jour une alerte
     */
    public AlertResponse updateAlert(String id, CreateAlertRequest request, String userId) {
        Alert alert = alertRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Alerte", "id", id));

        // Vérifier que l'utilisateur est propriétaire de l'alerte
        if (!alert.getOwnerUser().getId().equals(userId)) {
            throw new SecurityException("Vous n'êtes pas autorisé à modifier cette alerte");
        }

        // Mettre à jour les champs
        alert.setTitle(request.getTitle());
        alert.setQueryText(request.getQueryText());
        alert.setCategory(request.getCategory());
        alert.setLocationText(request.getLocationText());
        alert.setLatitude(request.getLatitude());
        alert.setLongitude(request.getLongitude());
        alert.setRadiusKm(request.getRadiusKm());
        alert.setDateFrom(request.getDateFrom());
        alert.setDateTo(request.getDateTo());
        alert.setChannels(request.getChannels());

        Alert updatedAlert = alertRepository.save(alert);
        return modelMapper.mapAlertToAlertResponse(updatedAlert);
    }

    /**
     * Supprimer une alerte
     */
    public void deleteAlert(String id, String userId) {
        Alert alert = alertRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Alerte", "id", id));

        // Vérifier que l'utilisateur est propriétaire de l'alerte
        if (!alert.getOwnerUser().getId().equals(userId)) {
            throw new SecurityException("Vous n'êtes pas autorisé à supprimer cette alerte");
        }

        alertRepository.delete(alert);
    }

    /**
     * Activer/désactiver une alerte
     */
    public AlertResponse toggleAlert(String id, String userId) {
        Alert alert = alertRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Alerte", "id", id));

        // Vérifier que l'utilisateur est propriétaire de l'alerte
        if (!alert.getOwnerUser().getId().equals(userId)) {
            throw new SecurityException("Vous n'êtes pas autorisé à modifier cette alerte");
        }

        alert.setActive(!alert.getActive());
        Alert updatedAlert = alertRepository.save(alert);
        
        return modelMapper.mapAlertToAlertResponse(updatedAlert);
    }

    /**
     * Vérifier et notifier les alertes correspondantes pour une nouvelle annonce
     */
    @Async
    public void checkAndNotifyMatchingAlerts(Listing listing) {
        List<Alert> activeAlerts = alertRepository.findByActiveTrue();

        for (Alert alert : activeAlerts) {
            if (isListingMatchingAlert(listing, alert)) {
                // Marquer l'alerte comme déclenchée
                alert.setLastTriggeredAt(LocalDateTime.now());
                alertRepository.save(alert);

                // Envoyer les notifications selon les canaux configurés
                sendAlertNotifications(alert, listing);
            }
        }
    }

    /**
     * Vérifier si une annonce correspond à une alerte
     */
    private boolean isListingMatchingAlert(Listing listing, Alert alert) {
        // Vérifier la catégorie
        if (alert.getCategory() != null && !alert.getCategory().trim().isEmpty()) {
            if (!listing.getCategory().getValue().equalsIgnoreCase(alert.getCategory())) {
                return false;
            }
        }

        // Vérifier le texte de recherche
        if (alert.getQueryText() != null && !alert.getQueryText().trim().isEmpty()) {
            String query = alert.getQueryText().toLowerCase();
            String title = listing.getTitle().toLowerCase();
            String description = listing.getDescription().toLowerCase();
            
            if (!title.contains(query) && !description.contains(query)) {
                return false;
            }
        }

        // Vérifier la localisation
        if (alert.getLocationText() != null && !alert.getLocationText().trim().isEmpty()) {
            String alertLocation = alert.getLocationText().toLowerCase();
            String listingLocation = listing.getLocationText().toLowerCase();
            
            if (!listingLocation.contains(alertLocation)) {
                return false;
            }
        }

        // Vérifier la proximité géographique
        if (alert.getLatitude() != null && alert.getLongitude() != null &&
            listing.getLatitude() != null && listing.getLongitude() != null) {
            
            double distance = calculateDistance(
                alert.getLatitude().doubleValue(), alert.getLongitude().doubleValue(),
                listing.getLatitude().doubleValue(), listing.getLongitude().doubleValue()
            );
            
            double radiusKm = alert.getRadiusKm() != null ? alert.getRadiusKm().doubleValue() : 10.0;
            if (distance > radiusKm) {
                return false;
            }
        }

        // Vérifier les dates
        if (alert.getDateFrom() != null) {
            LocalDateTime dateFrom = alert.getDateFrom().atStartOfDay();
            if (listing.getFoundAt().isBefore(dateFrom)) {
                return false;
            }
        }

        if (alert.getDateTo() != null) {
            LocalDateTime dateTo = alert.getDateTo().atTime(23, 59, 59);
            if (listing.getFoundAt().isAfter(dateTo)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Calculer la distance entre deux points géographiques (formule de Haversine)
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Rayon de la Terre en kilomètres

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }

    /**
     * Envoyer les notifications pour une alerte déclenchée
     */
    private void sendAlertNotifications(Alert alert, Listing listing) {
        User user = alert.getOwnerUser();
        String listingUrl = "http://localhost:3000/annonces/" + listing.getId();

        // Notifications email
        if (alert.getChannels().contains("email") && user.getEmailVerified()) {
            emailService.sendAlertNotification(
                user, alert.getTitle(), listing.getTitle(), listingUrl);
        }

        // Notifications SMS
        if (alert.getChannels().contains("sms") && user.getPhone() != null) {
            String message = String.format(
                "Retrouv'Tout: Nouvel objet trouvé pour votre alerte '%s' - %s. Voir: %s",
                alert.getTitle(), listing.getTitle(), listingUrl
            );
            smsService.sendSms(user.getPhone(), message);
        }

        // Notifications push
        if (alert.getChannels().contains("push")) {
            notificationService.sendPushNotification(
                user.getId(),
                "Nouvel objet trouvé !",
                String.format("L'objet '%s' correspond à votre alerte '%s'", 
                    listing.getTitle(), alert.getTitle()),
                listingUrl
            );
        }
    }

    /**
     * Obtenir les statistiques des alertes pour un utilisateur
     */
    @Transactional(readOnly = true)
    public long getUserActiveAlertsCount(String userId) {
        User user = userRepository.findByIdAndActiveTrue(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", userId));

        return alertRepository.countByOwnerUserAndActiveTrue(user);
    }

    /**
     * Obtenir les alertes récemment déclenchées
     */
    @Transactional(readOnly = true)
    public Page<AlertResponse> getRecentlyTriggeredAlerts(String userId, Pageable pageable) {
        User user = userRepository.findByIdAndActiveTrue(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", userId));

        Page<Alert> alerts = alertRepository.findByOwnerUserAndLastTriggeredAtIsNotNullOrderByLastTriggeredAtDesc(
            user, pageable);
        
        return alerts.map(modelMapper::mapAlertToAlertResponse);
    }
}