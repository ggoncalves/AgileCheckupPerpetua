package com.agilecheckup.persistency.converter;

import com.agilecheckup.persistency.entity.person.NaturalPerson;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class NaturalPersonAttributeConverter implements AttributeConverter<NaturalPerson> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public AttributeValue transformFrom(NaturalPerson input) {
        if (input == null) {
            return AttributeValue.builder().nul(true).build();
        }
        
        try {
            String json = OBJECT_MAPPER.writeValueAsString(input);
            return AttributeValue.builder().s(json).build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize NaturalPerson", e);
        }
    }

    @Override
    public NaturalPerson transformTo(AttributeValue input) {
        if (input.nul() != null && input.nul()) {
            return null;
        }
        
        String content = input.s();
        if (content == null || content.trim().isEmpty()) {
            return null;
        }
        
        try {
            return OBJECT_MAPPER.readValue(content, NaturalPerson.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize NaturalPerson", e);
        }
    }

    @Override
    public EnhancedType<NaturalPerson> type() {
        return EnhancedType.of(NaturalPerson.class);
    }

    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.S;
    }
}