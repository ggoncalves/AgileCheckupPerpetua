package com.agilecheckup.persistency.converter;

import com.agilecheckup.persistency.entity.person.PersonDocumentType;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class PersonDocumentTypeConverter implements AttributeConverter<PersonDocumentType> {
  @Override
  public AttributeValue transformFrom(PersonDocumentType input) {
    return input != null ? AttributeValue.builder().s(input.name()).build() : AttributeValue.builder().nul(true).build();
  }

  @Override
  public PersonDocumentType transformTo(AttributeValue input) {
    if (input == null || input.nul() != null && input.nul()) {
      return null;
    }
    return PersonDocumentType.valueOf(input.s());
  }

  @Override
  public EnhancedType<PersonDocumentType> type() {
    return EnhancedType.of(PersonDocumentType.class);
  }

  @Override
  public AttributeValueType attributeValueType() {
    return AttributeValueType.S;
  }
}