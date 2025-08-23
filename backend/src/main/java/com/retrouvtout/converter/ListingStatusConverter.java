package com.retrouvtout.converter;

import com.retrouvtout.entity.Listing;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * CONVERTER JPA POUR ListingStatus
 * Sauvegarde les VALUES en DB au lieu des enum names
 */
@Converter(autoApply = true)
public class ListingStatusConverter implements AttributeConverter<Listing.ListingStatus, String> {

    @Override
    public String convertToDatabaseColumn(Listing.ListingStatus attribute) {
        if (attribute == null) {
            return null;
        }
        
        String value = attribute.getValue();
        System.out.println("DB SAVE STATUS: " + attribute.name() + " -> \"" + value + "\"");
        return value;
    }

    @Override
    public Listing.ListingStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        
        try {
            Listing.ListingStatus status = Listing.ListingStatus.fromValue(dbData);
            System.out.println("DB READ STATUS: \"" + dbData + "\" -> " + status.name());
            return status;
        } catch (IllegalArgumentException e) {
            System.err.println("Status invalide en DB: '" + dbData + "'");
            return Listing.ListingStatus.ACTIVE;
        }
    }
}
