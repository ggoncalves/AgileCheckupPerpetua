package com.agilecheckup.persistency.converter;

import com.agilecheckup.persistency.entity.CompanySize;

import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class CompanySizeConverter implements AttributeConverter<CompanySize> {
  @Override
  public AttributeValue transformFrom(CompanySize input) {
    return input != null ? AttributeValue.builder().s(input.name()).build() : AttributeValue.builder()
                                                                                            .nul(true)
                                                                                            .build();
  }

  @Override
  public CompanySize transformTo(AttributeValue input) {
    if (input == null || input.nul() != null && input.nul()) {
      return null;
    }
    return CompanySize.valueOf(input.s());
  }

  @Override
  public EnhancedType<CompanySize> type() {
    return EnhancedType.of(CompanySize.class);
  }

  @Override
  public AttributeValueType attributeValueType() {
    return AttributeValueType.S;
  }
}