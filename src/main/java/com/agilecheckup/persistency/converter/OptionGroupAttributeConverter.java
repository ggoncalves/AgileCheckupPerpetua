package com.agilecheckup.persistency.converter;

import com.agilecheckup.persistency.entity.question.OptionGroup;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

/**
 * Converter for OptionGroup to/from DynamoDB JSON string attribute.
 * Maintains compatibility with V1 storage format.
 */
public class OptionGroupAttributeConverter implements AttributeConverter<OptionGroup> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public AttributeValue transformFrom(OptionGroup input) {
        if (input == null) {
            return AttributeValue.builder().nul(true).build();
        }
        
        try {
            String json = OBJECT_MAPPER.writeValueAsString(input);
            return AttributeValue.builder().s(json).build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize OptionGroup", e);
        }
    }

    @Override
    public OptionGroup transformTo(AttributeValue input) {
        if (input == null || input.nul() != null && input.nul()) {
            return null;
        }
        
        String content = input.s();
        if (content == null || content.trim().isEmpty()) {
            return null;
        }
        
        try {
            return OBJECT_MAPPER.readValue(content, OptionGroup.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize OptionGroup", e);
        }
    }

    @Override
    public EnhancedType<OptionGroup> type() {
        return EnhancedType.of(OptionGroup.class);
    }

    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.S;
    }
}