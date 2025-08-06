package com.agilecheckup.persistency.converter;

import com.agilecheckup.persistency.entity.AssessmentConfigurationV2;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class AssessmentConfigurationAttributeConverter implements AttributeConverter<AssessmentConfigurationV2> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .setDateFormat(new StdDateFormat());

    @Override
    public AttributeValue transformFrom(AssessmentConfigurationV2 input) {
        if (input == null) {
            return AttributeValue.builder().nul(true).build();
        }
        try {
            String jsonString = OBJECT_MAPPER.writeValueAsString(input);
            return AttributeValue.builder().s(jsonString).build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize AssessmentConfigurationV2", e);
        }
    }

    @Override
    public AssessmentConfigurationV2 transformTo(AttributeValue input) {
        if (input.nul() != null && input.nul()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(input.s(), AssessmentConfigurationV2.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize AssessmentConfigurationV2", e);
        }
    }

    @Override
    public EnhancedType<AssessmentConfigurationV2> type() {
        return EnhancedType.of(AssessmentConfigurationV2.class);
    }

    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.S;
    }
}