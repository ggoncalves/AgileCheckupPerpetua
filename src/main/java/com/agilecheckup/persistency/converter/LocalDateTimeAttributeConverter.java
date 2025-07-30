package com.agilecheckup.persistency.converter;

import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Converter for LocalDateTime to/from DynamoDB String attribute.
 * Maintains compatibility with V1 Answer.LocalDateTimeConverter format using ISO_LOCAL_DATE_TIME.
 */
public class LocalDateTimeAttributeConverter implements AttributeConverter<LocalDateTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public AttributeValue transformFrom(LocalDateTime input) {
        if (input == null) {
            return AttributeValue.builder().nul(true).build();
        }
        return AttributeValue.builder().s(input.format(FORMATTER)).build();
    }

    @Override
    public LocalDateTime transformTo(AttributeValue input) {
        if (input == null || input.nul() != null && input.nul()) {
            return null;
        }
        
        String value = input.s();
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        try {
            return LocalDateTime.parse(value, FORMATTER);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse LocalDateTime: " + value, e);
        }
    }

    @Override
    public EnhancedType<LocalDateTime> type() {
        return EnhancedType.of(LocalDateTime.class);
    }

    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.S;
    }
}