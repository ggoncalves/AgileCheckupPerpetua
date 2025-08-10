package com.agilecheckup.persistency.converter;

import com.agilecheckup.persistency.entity.person.Address;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AddressAttributeConverter Tests")
class AddressAttributeConverterTest {

  private AddressAttributeConverter converter;
  private Address completeAddress;
  private String expectedJsonString;

  @BeforeEach
  void setUp() {
    converter = new AddressAttributeConverter();
    completeAddress = createCompleteAddress();
    expectedJsonString = "{\"id\":\"addr-123\",\"street\":\"123 Main St\",\"city\":\"New York\",\"state\":\"NY\",\"zipcode\":\"10001\",\"country\":\"USA\"}";
  }

  // CORRECT - Conformance tests
  @Test
  @DisplayName("Should transform complete Address to JSON AttributeValue")
  void shouldTransformCompleteAddressToJsonAttributeValue() {
    AttributeValue result = converter.transformFrom(completeAddress);

    assertThat(result).isNotNull();
    assertThat(result.s()).isEqualTo(expectedJsonString);
    assertThat(result.nul()).isNull();
  }

  @Test
  @DisplayName("Should transform null Address to NULL AttributeValue")
  void shouldTransformNullAddressToNullAttributeValue() {
    AttributeValue result = converter.transformFrom(null);

    assertThat(result).isNotNull();
    assertThat(result.nul()).isTrue();
    assertThat(result.s()).isNull();
  }

  @Test
  @DisplayName("Should transform JSON AttributeValue to complete Address")
  void shouldTransformJsonAttributeValueToCompleteAddress() {
    AttributeValue input = createJsonAttributeValue(expectedJsonString);

    Address result = converter.transformTo(input);

    assertAddressFieldsMatch(result, completeAddress);
  }

  @Test
  @DisplayName("Should transform NULL AttributeValue to null Address")
  void shouldTransformNullAttributeValueToNullAddress() {
    AttributeValue input = createNullAttributeValue();

    Address result = converter.transformTo(input);

    assertThat(result).isNull();
  }

  // CORRECT - Ordering tests (Round-trip conversion)
  @Test
  @DisplayName("Should handle round-trip conversion correctly")
  void shouldHandleRoundTripConversionCorrectly() {
    AttributeValue attributeValue = converter.transformFrom(completeAddress);
    Address result = converter.transformTo(attributeValue);

    assertAddressFieldsMatch(result, completeAddress);
  }

  // CORRECT - Existence tests
  @Test
  @DisplayName("Should handle empty string AttributeValue")
  void shouldHandleEmptyStringAttributeValue() {
    AttributeValue input = createJsonAttributeValue("");

    Address result = converter.transformTo(input);

    assertThat(result).isNull();
  }

  @Test
  @DisplayName("Should handle null AttributeValue input")
  void shouldHandleNullAttributeValueInput() {
    Address result = converter.transformTo(null);

    assertThat(result).isNull();
  }

  // CORRECT - Cardinality tests
  @Test
  @DisplayName("Should handle Address with partial fields")
  void shouldHandleAddressWithPartialFields() {
    Address partialAddress = createPartialAddress();

    AttributeValue attributeValue = converter.transformFrom(partialAddress);
    Address result = converter.transformTo(attributeValue);

    assertThat(result.getCity()).isEqualTo("Boston");
    assertThat(result.getCountry()).isEqualTo("USA");
    assertThat(result.getStreet()).isEmpty();
    assertThat(result.getState()).isEmpty();
    assertThat(result.getZipcode()).isEmpty();
  }

  @Test
  @DisplayName("Should handle Address with quoted strings in fields")
  void shouldHandleAddressWithQuotedStrings() {
    Address quotedAddress = createAddressWithQuotes();

    AttributeValue attributeValue = converter.transformFrom(quotedAddress);
    Address result = converter.transformTo(attributeValue);

    assertThat(result.getStreet()).isEqualTo("123 \"Main\" Street");
    assertThat(result.getCity()).isEqualTo("New \"York\" City");
  }

  // CORRECT - Reference tests
  @Test
  @DisplayName("Should return correct EnhancedType for Address")
  void shouldReturnCorrectEnhancedType() {
    EnhancedType<Address> result = converter.type();

    assertThat(result).isEqualTo(EnhancedType.of(Address.class));
  }

  @Test
  @DisplayName("Should return String AttributeValueType")
  void shouldReturnStringAttributeValueType() {
    AttributeValueType result = converter.attributeValueType();

    assertThat(result).isEqualTo(AttributeValueType.S);
  }

  // Helper methods for clean, readable test code
  private Address createCompleteAddress() {
    Address address = new Address();
    address.setId("addr-123");
    address.setStreet("123 Main St");
    address.setCity("New York");
    address.setState("NY");
    address.setZipcode("10001");
    address.setCountry("USA");
    return address;
  }

  private Address createPartialAddress() {
    Address address = new Address();
    address.setCity("Boston");
    address.setCountry("USA");
    return address;
  }

  private Address createAddressWithQuotes() {
    Address address = new Address();
    address.setStreet("123 \"Main\" Street");
    address.setCity("New \"York\" City");
    return address;
  }

  private AttributeValue createJsonAttributeValue(String json) {
    return AttributeValue.builder().s(json).build();
  }

  private AttributeValue createNullAttributeValue() {
    return AttributeValue.builder().nul(true).build();
  }

  private void assertAddressFieldsMatch(Address actual, Address expected) {
    assertThat(actual).isNotNull();
    assertThat(actual.getId()).isEqualTo(expected.getId());
    assertThat(actual.getStreet()).isEqualTo(expected.getStreet());
    assertThat(actual.getCity()).isEqualTo(expected.getCity());
    assertThat(actual.getState()).isEqualTo(expected.getState());
    assertThat(actual.getZipcode()).isEqualTo(expected.getZipcode());
    assertThat(actual.getCountry()).isEqualTo(expected.getCountry());
  }
}