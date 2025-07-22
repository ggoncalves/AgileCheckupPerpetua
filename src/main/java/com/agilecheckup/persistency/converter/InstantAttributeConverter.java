package com.agilecheckup.persistency.converter;

import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class InstantAttributeConverter implements AttributeConverter<Instant> {
    
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT;
    
    @Override
    public AttributeValue transformFrom(Instant instant) {
        if (instant == null) {
            return AttributeValue.builder().nul(true).build();
        }
        return AttributeValue.builder().s(ISO_FORMATTER.format(instant)).build();
    }
    
    @Override
    public Instant transformTo(AttributeValue attributeValue) {
        if (attributeValue.nul() != null && attributeValue.nul()) {
            return null;
        }
        
        String dateString = attributeValue.s();
        if (dateString == null) {
            return null;
        }
        
        try {
            // First try to parse as ISO instant (handles both 'Z' suffix and microseconds)
            return Instant.parse(dateString);
        } catch (DateTimeParseException e) {
            // If that fails, try to handle the format without 'Z' by adding it
            try {
                if (!dateString.endsWith("Z")) {
                    return Instant.parse(dateString + "Z");
                }
                throw e; // Re-throw if it already had 'Z' but still failed
            } catch (DateTimeParseException e2) {
                throw new RuntimeException("Unable to parse date string: " + dateString, e2);
            }
        }
    }
    
    @Override
    public EnhancedType<Instant> type() {
        return EnhancedType.of(Instant.class);
    }
    
    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.S;
    }
}