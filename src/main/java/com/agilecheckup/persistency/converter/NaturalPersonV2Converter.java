package com.agilecheckup.persistency.converter;

import com.agilecheckup.persistency.entity.Industry;
import com.agilecheckup.persistency.entity.person.NaturalPersonV2;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class NaturalPersonV2Converter implements AttributeConverter<NaturalPersonV2> {
  @Override
  public AttributeValue transformFrom(NaturalPersonV2 input) {
    if (input == null) {
      return AttributeValue.builder().nul(true).build();
    }
    // Convert NaturalPersonV2 to JSON string for storage
    StringBuilder json = new StringBuilder("{");
    if (input.getId() != null) {
      json.append("\"id\":\"").append(escapeJson(input.getId())).append("\",");
    }
    if (input.getName() != null) {
      json.append("\"name\":\"").append(escapeJson(input.getName())).append("\",");
    }
    if (input.getEmail() != null) {
      json.append("\"email\":\"").append(escapeJson(input.getEmail())).append("\",");
    }
    if (input.getPhone() != null) {
      json.append("\"phone\":\"").append(escapeJson(input.getPhone())).append("\",");
    }
    if (input.getDocumentNumber() != null) {
      json.append("\"documentNumber\":\"").append(escapeJson(input.getDocumentNumber())).append("\",");
    }
    if (input.getAliasName() != null) {
      json.append("\"aliasName\":\"").append(escapeJson(input.getAliasName())).append("\",");
    }
    if (input.getPersonDocumentType() != null) {
      json.append("\"personDocumentType\":\"").append(input.getPersonDocumentType().name()).append("\",");
    }
    if (input.getGender() != null) {
      json.append("\"gender\":\"").append(input.getGender().name()).append("\",");
    }
    if (input.getGenderPronoun() != null) {
      json.append("\"genderPronoun\":\"").append(input.getGenderPronoun().name()).append("\",");
    }

    // Remove trailing comma if exists
    if (json.charAt(json.length() - 1) == ',') {
      json.setLength(json.length() - 1);
    }
    json.append("}");

    return AttributeValue.builder().s(json.toString()).build();
  }

  @Override
  public NaturalPersonV2 transformTo(AttributeValue input) {
    if (input == null || input.nul() != null && input.nul()) {
      return null;
    }

    String json = input.s();
    if (json == null || json.trim().isEmpty() || json.equals("{}")) {
      return null;
    }

    // Simple JSON parsing for NaturalPersonV2
    NaturalPersonV2.NaturalPersonV2Builder<?, ?> builder = NaturalPersonV2.builder();

    if (json.contains("\"id\":\"")) {
      String id = extractJsonValue(json, "id");
      if (!id.isEmpty()) builder.id(id);
    }
    if (json.contains("\"name\":\"")) {
      builder.name(extractJsonValue(json, "name"));
    }
    if (json.contains("\"email\":\"")) {
      builder.email(extractJsonValue(json, "email"));
    }
    if (json.contains("\"phone\":\"")) {
      builder.phone(extractJsonValue(json, "phone"));
    }
    if (json.contains("\"documentNumber\":\"")) {
      builder.documentNumber(extractJsonValue(json, "documentNumber"));
    }
    if (json.contains("\"aliasName\":\"")) {
      builder.aliasName(extractJsonValue(json, "aliasName"));
    }
    if (json.contains("\"personDocumentType\":\"")) {
      String docType = extractJsonValue(json, "personDocumentType");
      if (!docType.isEmpty()) {
        builder.personDocumentType(com.agilecheckup.persistency.entity.person.PersonDocumentType.valueOf(docType));
      }
    }
    if (json.contains("\"gender\":\"")) {
      String gender = extractJsonValue(json, "gender");
      if (!gender.isEmpty()) {
        builder.gender(com.agilecheckup.persistency.entity.person.Gender.valueOf(gender));
      }
    }
    if (json.contains("\"genderPronoun\":\"")) {
      String pronoun = extractJsonValue(json, "genderPronoun");
      if (!pronoun.isEmpty()) {
        builder.genderPronoun(com.agilecheckup.persistency.entity.person.GenderPronoun.valueOf(pronoun));
      }
    }

    return builder.build();
  }

  private String escapeJson(String value) {
    if (value == null) return "";
    return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
  }

  private String extractJsonValue(String json, String key) {
    String pattern = "\"" + key + "\":\"";
    int startIndex = json.indexOf(pattern);
    if (startIndex == -1) return "";
    startIndex += pattern.length();
    int endIndex = json.indexOf("\"", startIndex);
    if (endIndex == -1) return "";
    return json.substring(startIndex, endIndex).replace("\\\"", "\"").replace("\\\\", "\\");
  }

  @Override
  public EnhancedType<NaturalPersonV2> type() {
    return EnhancedType.of(NaturalPersonV2.class);
  }

  @Override
  public AttributeValueType attributeValueType() {
    return AttributeValueType.S;
  }
}