// backend/src/main/java/com/retrouvtout/converter/ListingStatusConverter.java

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
        return attribute.getValue(); // Sauvegarde "active", "resolved", etc.
    }

    @Override
    public Listing.ListingStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        return Listing.ListingStatus.fromValue(dbData);
    }
}
