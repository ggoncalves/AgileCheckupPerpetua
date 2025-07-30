package com.agilecheckup.persistency.converter;

import com.agilecheckup.persistency.entity.EmployeeAssessmentScore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class EmployeeAssessmentScoreAttributeConverter implements AttributeConverter<EmployeeAssessmentScore> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public AttributeValue transformFrom(EmployeeAssessmentScore input) {
        if (input == null) {
            return AttributeValue.builder().nul(true).build();
        }
        
        try {
            String json = OBJECT_MAPPER.writeValueAsString(input);
            return AttributeValue.builder().s(json).build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize EmployeeAssessmentScore", e);
        }
    }

    @Override
    public EmployeeAssessmentScore transformTo(AttributeValue input) {
        if (input.nul() != null && input.nul()) {
            return null;
        }
        
        String content = input.s();
        if (content == null || content.trim().isEmpty()) {
            return null;
        }
        
        try {
            return OBJECT_MAPPER.readValue(content, EmployeeAssessmentScore.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize EmployeeAssessmentScore", e);
        }
    }

    @Override
    public EnhancedType<EmployeeAssessmentScore> type() {
        return EnhancedType.of(EmployeeAssessmentScore.class);
    }

    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.S;
    }
}