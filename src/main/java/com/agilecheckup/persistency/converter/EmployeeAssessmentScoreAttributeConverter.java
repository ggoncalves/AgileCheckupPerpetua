package com.agilecheckup.persistency.converter;

import com.agilecheckup.persistency.entity.EmployeeAssessmentScore;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EmployeeAssessmentScoreAttributeConverter implements AttributeConverter<EmployeeAssessmentScore> {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public AttributeValue transformFrom(EmployeeAssessmentScore input) {
        if (input == null) {
            return AttributeValue.builder().nul(true).build();
        }
        try {
            String json = objectMapper.writeValueAsString(input);
            return AttributeValue.builder().s(json).build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert EmployeeAssessmentScore to JSON", e);
        }
    }
    
    @Override
    public EmployeeAssessmentScore transformTo(AttributeValue input) {
        if (input == null || input.nul() != null && input.nul()) {
            return null;
        }
        try {
            return objectMapper.readValue(input.s(), EmployeeAssessmentScore.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert JSON to EmployeeAssessmentScore", e);
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