package com.agilecheckup.persistency.converter;

import com.agilecheckup.persistency.entity.question.Question;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

/**
 * Converter for Question object to/from DynamoDB String attribute.
 * Handles V2 entities with Instant-based timestamps.
 */
public class QuestionAttributeConverter implements AttributeConverter<Question> {

    private static final ObjectMapper OBJECT_MAPPER = createObjectMapper();
    
    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Override
    public AttributeValue transformFrom(Question input) {
        if (input == null) {
            return AttributeValue.builder().nul(true).build();
        }
        
        try {
            String json = OBJECT_MAPPER.writeValueAsString(input);
            return AttributeValue.builder().s(json).build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize QuestionV2", e);
        }
    }

    @Override
    public Question transformTo(AttributeValue input) {
        if (input == null || input.nul() != null && input.nul()) {
            return null;
        }
        
        String content = input.s();
        if (content == null || content.trim().isEmpty()) {
            return null;
        }
        
        try {
            return OBJECT_MAPPER.readValue(content, Question.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize QuestionV2", e);
        }
    }

    @Override
    public EnhancedType<Question> type() {
        return EnhancedType.of(Question.class);
    }

    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.S;
    }
}