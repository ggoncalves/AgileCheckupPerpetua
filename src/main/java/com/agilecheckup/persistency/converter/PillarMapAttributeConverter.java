package com.agilecheckup.persistency.converter;

import java.util.Map;

import com.agilecheckup.persistency.entity.Pillar;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;

import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class PillarMapAttributeConverter implements AttributeConverter<Map<String, Pillar>> {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                                                                      .setDateFormat(new StdDateFormat());
  private static final TypeReference<Map<String, Pillar>> TYPE_REFERENCE = new TypeReference<Map<String, Pillar>>() {
  };

  @Override
  public AttributeValue transformFrom(Map<String, Pillar> input) {
    if (input == null) {
      return AttributeValue.builder().nul(true).build();
    }
    try {
      String jsonString = OBJECT_MAPPER.writeValueAsString(input);
      return AttributeValue.builder().s(jsonString).build();
    }
    catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to serialize Pillar map", e);
    }
  }

  @Override
  public Map<String, Pillar> transformTo(AttributeValue input) {
    if (input.nul() != null && input.nul()) {
      return null;
    }
    try {
      return OBJECT_MAPPER.readValue(input.s(), TYPE_REFERENCE);
    }
    catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to deserialize Pillar map", e);
    }
  }

  @Override
  public EnhancedType<Map<String, Pillar>> type() {
    return EnhancedType.mapOf(String.class, Pillar.class);
  }

  @Override
  public AttributeValueType attributeValueType() {
    return AttributeValueType.S;
  }
}