package com.retrouvtout.converter;

import com.retrouvtout.entity.Listing;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ListingStatusConverter implements AttributeConverter<Listing.ListingStatus, String> {

    @Override
    public String convertToDatabaseColumn(Listing.ListingStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getValue();
    }

    @Override
    public Listing.ListingStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        
        try {
            return Listing.ListingStatus.fromValue(dbData);
        } catch (IllegalArgumentException e) {
            return Listing.ListingStatus.ACTIVE;
        }
    }
}