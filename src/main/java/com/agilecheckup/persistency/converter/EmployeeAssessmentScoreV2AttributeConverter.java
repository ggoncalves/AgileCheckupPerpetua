package com.agilecheckup.persistency.converter;

import com.agilecheckup.persistency.entity.EmployeeAssessmentScoreV2;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Slf4j
public class EmployeeAssessmentScoreV2AttributeConverter implements AttributeConverter<EmployeeAssessmentScoreV2> {
    
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    @Override
    public AttributeValue transformFrom(EmployeeAssessmentScoreV2 input) {
        if (input == null) {
            return AttributeValue.builder().nul(true).build();
        }
        try {
            String json = OBJECT_MAPPER.writeValueAsString(input);
            return AttributeValue.builder().s(json).build();
        } catch (JsonProcessingException e) {
            log.error("Error converting EmployeeAssessmentScoreV2 to JSON", e);
            throw new RuntimeException("Error converting EmployeeAssessmentScoreV2 to JSON", e);
        }
    }
    
    @Override
    public EmployeeAssessmentScoreV2 transformTo(AttributeValue input) {
        if (input.nul() != null && input.nul()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(input.s(), EmployeeAssessmentScoreV2.class);
        } catch (JsonProcessingException e) {
            log.error("Error converting JSON to EmployeeAssessmentScoreV2", e);
            throw new RuntimeException("Error converting JSON to EmployeeAssessmentScoreV2", e);
        }
    }
    
    @Override
    public EnhancedType<EmployeeAssessmentScoreV2> type() {
        return EnhancedType.of(EmployeeAssessmentScoreV2.class);
    }
    
    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.S;
    }
}