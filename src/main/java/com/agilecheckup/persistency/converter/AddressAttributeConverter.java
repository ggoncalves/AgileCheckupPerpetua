package com.agilecheckup.persistency.converter;

import com.agilecheckup.persistency.entity.person.Address;

import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class AddressAttributeConverter implements AttributeConverter<Address> {
  @Override
  public AttributeValue transformFrom(Address input) {
    if (input == null) {
      return AttributeValue.builder().nul(true).build();
    }
    // Convert Address to JSON string for storage
    return AttributeValue.builder().s(String.format(
        "{\"id\":\"%s\",\"street\":\"%s\",\"city\":\"%s\",\"state\":\"%s\",\"zipcode\":\"%s\",\"country\":\"%s\"}", escapeJsonValue(input.getId()), escapeJsonValue(input.getStreet()), escapeJsonValue(input.getCity()), escapeJsonValue(input.getState()), escapeJsonValue(input.getZipcode()), escapeJsonValue(input.getCountry())
    )).build();
  }

  @Override
  public Address transformTo(AttributeValue input) {
    if (input == null || input.nul() != null && input.nul()) {
      return null;
    }
    // Simple JSON parsing for Address
    String json = input.s();
    if (json == null || json.trim().isEmpty()) {
      return null;
    }

    // Parse JSON manually (simple implementation)
    Address address = new Address();
    if (json.contains("\"id\":\"")) {
      String id = extractJsonValue(json, "id");
      if (!id.isEmpty()) address.setId(id);
    }
    if (json.contains("\"street\":\"")) {
      address.setStreet(extractJsonValue(json, "street"));
    }
    if (json.contains("\"city\":\"")) {
      address.setCity(extractJsonValue(json, "city"));
    }
    if (json.contains("\"state\":\"")) {
      address.setState(extractJsonValue(json, "state"));
    }
    if (json.contains("\"zipcode\":\"")) {
      address.setZipcode(extractJsonValue(json, "zipcode"));
    }
    if (json.contains("\"country\":\"")) {
      address.setCountry(extractJsonValue(json, "country"));
    }
    return address;
  }

  private String escapeJsonValue(String value) {
    if (value == null) {
      return "";
    }
    return value.replace("\\", "\\\\")    // Escape backslashes first
        .replace("\"", "\\\"")    // Escape quotes
        .replace("\n", "\\n")     // Escape newlines
        .replace("\r", "\\r")     // Escape carriage returns
        .replace("\t", "\\t");    // Escape tabs
  }

  private String extractJsonValue(String json, String key) {
    String pattern = "\"" + key + "\":\"";
    int startIndex = json.indexOf(pattern);
    if (startIndex == -1) return "";

    startIndex += pattern.length();
    StringBuilder result = new StringBuilder();

    for (int i = startIndex; i < json.length(); i++) {
      char ch = json.charAt(i);

      if (ch == '"') {
        // Found closing quote, we're done
        break;
      }
      else if (ch == '\\' && i + 1 < json.length()) {
        // Handle escaped characters
        char nextChar = json.charAt(i + 1);
        switch (nextChar) {
          case '"':
            result.append('"');
            i++; // Skip the escaped character
            break;
          case '\\':
            result.append('\\');
            i++; // Skip the escaped character
            break;
          case 'n':
            result.append('\n');
            i++; // Skip the escaped character
            break;
          case 'r':
            result.append('\r');
            i++; // Skip the escaped character
            break;
          case 't':
            result.append('\t');
            i++; // Skip the escaped character
            break;
          default:
            // Unknown escape sequence, keep the backslash
            result.append(ch);
            break;
        }
      }
      else {
        result.append(ch);
      }
    }

    return result.toString();
  }

  @Override
  public EnhancedType<Address> type() {
    return EnhancedType.of(Address.class);
  }

  @Override
  public AttributeValueType attributeValueType() {
    return AttributeValueType.S;
  }
}