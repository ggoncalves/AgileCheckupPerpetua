package com.agilecheckup.persistency.converter;

import com.agilecheckup.persistency.entity.QuestionType;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

/**
 * Converter for QuestionType enum to/from DynamoDB String attribute.
 * Maintains compatibility with V1 storage format.
 */
public class QuestionTypeAttributeConverter implements AttributeConverter<QuestionType> {

    @Override
    public AttributeValue transformFrom(QuestionType input) {
        if (input == null) {
            return AttributeValue.builder().nul(true).build();
        }
        return AttributeValue.builder().s(input.name()).build();
    }

    @Override
    public QuestionType transformTo(AttributeValue input) {
        if (input == null || input.nul() != null && input.nul()) {
            return null;
        }
        
        String value = input.s();
        if (value == null) {
            return null;
        }
        
        try {
            return QuestionType.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown QuestionType value: " + value, e);
        }
    }

    @Override
    public EnhancedType<QuestionType> type() {
        return EnhancedType.of(QuestionType.class);
    }

    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.S;
    }
}