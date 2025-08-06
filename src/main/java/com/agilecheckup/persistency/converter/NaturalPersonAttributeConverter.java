package com.agilecheckup.persistency.converter;

import com.agilecheckup.persistency.entity.person.NaturalPersonV2;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class NaturalPersonAttributeConverter implements AttributeConverter<NaturalPersonV2> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public AttributeValue transformFrom(NaturalPersonV2 input) {
        if (input == null) {
            return AttributeValue.builder().nul(true).build();
        }
        
        try {
            String json = OBJECT_MAPPER.writeValueAsString(input);
            return AttributeValue.builder().s(json).build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize NaturalPersonV2", e);
        }
    }

    @Override
    public NaturalPersonV2 transformTo(AttributeValue input) {
        if (input.nul() != null && input.nul()) {
            return null;
        }
        
        String content = input.s();
        if (content == null || content.trim().isEmpty()) {
            return null;
        }
        
        try {
            return OBJECT_MAPPER.readValue(content, NaturalPersonV2.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize NaturalPersonV2", e);
        }
    }

    @Override
    public EnhancedType<NaturalPersonV2> type() {
        return EnhancedType.of(NaturalPersonV2.class);
    }

    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.S;
    }
}