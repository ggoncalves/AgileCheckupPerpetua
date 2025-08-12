package com.agilecheckup.persistency.converter;

import com.agilecheckup.persistency.entity.person.GenderPronoun;

import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class GenderPronounConverter implements AttributeConverter<GenderPronoun> {
  @Override
  public AttributeValue transformFrom(GenderPronoun input) {
    return input != null ? AttributeValue.builder().s(input.name()).build() : AttributeValue.builder().nul(true).build();
  }

  @Override
  public GenderPronoun transformTo(AttributeValue input) {
    if (input == null || input.nul() != null && input.nul()) {
      return null;
    }
    return GenderPronoun.valueOf(input.s());
  }

  @Override
  public EnhancedType<GenderPronoun> type() {
    return EnhancedType.of(GenderPronoun.class);
  }

  @Override
  public AttributeValueType attributeValueType() {
    return AttributeValueType.S;
  }
}