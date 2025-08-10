package com.agilecheckup.persistency.converter;

import com.agilecheckup.persistency.entity.Category;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;

public class CategoryMapAttributeConverter implements AttributeConverter<Map<String, Category>> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .setDateFormat(new StdDateFormat());
    private static final TypeReference<Map<String, Category>> TYPE_REFERENCE = new TypeReference<Map<String, Category>>() {};

    @Override
    public AttributeValue transformFrom(Map<String, Category> input) {
        if (input == null) {
            return AttributeValue.builder().nul(true).build();
        }
        try {
            String jsonString = OBJECT_MAPPER.writeValueAsString(input);
            return AttributeValue.builder().s(jsonString).build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize Category map", e);
        }
    }

    @Override
    public Map<String, Category> transformTo(AttributeValue input) {
        if (input.nul() != null && input.nul()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(input.s(), TYPE_REFERENCE);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize Category map", e);
        }
    }

    @Override
    public EnhancedType<Map<String, Category>> type() {
        return EnhancedType.mapOf(String.class, Category.class);
    }

    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.S;
    }
}