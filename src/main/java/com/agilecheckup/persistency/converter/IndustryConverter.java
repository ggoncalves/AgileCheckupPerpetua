package com.agilecheckup.persistency.converter;

import com.agilecheckup.persistency.entity.Industry;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class IndustryConverter implements AttributeConverter<Industry> {
  @Override
  public AttributeValue transformFrom(Industry input) {
    return input != null ? AttributeValue.builder().s(input.name()).build() : AttributeValue.builder().nul(true).build();
  }

  @Override
  public Industry transformTo(AttributeValue input) {
    if (input == null || input.nul() != null && input.nul()) {
      return null;
    }
    return Industry.valueOf(input.s());
  }

  @Override
  public EnhancedType<Industry> type() {
    return EnhancedType.of(Industry.class);
  }

  @Override
  public AttributeValueType attributeValueType() {
    return AttributeValueType.S;
  }
}