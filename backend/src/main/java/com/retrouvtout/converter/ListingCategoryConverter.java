// backend/src/main/java/com/retrouvtout/converter/ListingCategoryConverter.java

package com.retrouvtout.converter;

import com.retrouvtout.entity.Listing;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * ✅ SOLUTION COMPLÈTE : Converter JPA pour sauvegarder les VALUES en DB
 * Au lieu de sauvegarder "CLES", "ELECTRONIQUE" (enum names)
 * On sauvegarde "cles", "electronique" (enum values) - conforme frontend
 */
@Converter(autoApply = true)
public class ListingCategoryConverter implements AttributeConverter<Listing.ListingCategory, String> {

    @Override
    public String convertToDatabaseColumn(Listing.ListingCategory attribute) {
        if (attribute == null) {
            return null;
        }
        
        // ✅ SAUVEGARDE LE VALUE ("cles", "electronique", etc.) en DB
        String value = attribute.getValue();
        System.out.println("🔧 DB SAVE: " + attribute.name() + " -> \"" + value + "\"");
        return value;
    }

    @Override
    public Listing.ListingCategory convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        
        try {
            // ✅ RELIT depuis la DB et reconvertit en enum
            Listing.ListingCategory category = Listing.ListingCategory.fromValue(dbData);
            System.out.println("🔧 DB READ: \"" + dbData + "\" -> " + category.name());
            return category;
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Catégorie invalide en DB: '" + dbData + "'");
            // Retourner une valeur par défaut plutôt que null
            return Listing.ListingCategory.AUTRE;
        }
    }
}