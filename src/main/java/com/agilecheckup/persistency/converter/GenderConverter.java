package com.agilecheckup.persistency.converter;

import com.agilecheckup.persistency.entity.person.Gender;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class GenderConverter implements AttributeConverter<Gender> {
  @Override
  public AttributeValue transformFrom(Gender input) {
    return input != null ? AttributeValue.builder().s(input.name()).build() : AttributeValue.builder().nul(true).build();
  }

  @Override
  public Gender transformTo(AttributeValue input) {
    if (input == null || input.nul() != null && input.nul()) {
      return null;
    }
    return Gender.valueOf(input.s());
  }

  @Override
  public EnhancedType<Gender> type() {
    return EnhancedType.of(Gender.class);
  }

  @Override
  public AttributeValueType attributeValueType() {
    return AttributeValueType.S;
  }
}