package com.agilecheckup.persistency.converter;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@ExtendWith(MockitoExtension.class)
@DisplayName("LocalDateTimeAttributeConverter Tests")
class LocalDateTimeAttributeConverterTest {

  private LocalDateTimeAttributeConverter converter;
  private LocalDateTime testDateTime;
  private String expectedIsoString;

  @BeforeEach
  void setUp() {
    converter = new LocalDateTimeAttributeConverter();
    testDateTime = LocalDateTime.of(2023, 12, 25, 14, 30, 45, 123000000);
    expectedIsoString = "2023-12-25T14:30:45.123";
  }

  // CORRECT - Conformance tests
  @Test
  @DisplayName("Should transform valid LocalDateTime to ISO string AttributeValue")
  void shouldTransformValidLocalDateTimeToAttributeValue() {
    AttributeValue result = converter.transformFrom(testDateTime);

    assertThat(result).isNotNull();
    assertThat(result.s()).isEqualTo(expectedIsoString);
    assertThat(result.nul()).isNull();
  }

  @Test
  @DisplayName("Should transform null LocalDateTime to NULL AttributeValue")
  void shouldTransformNullLocalDateTimeToNullAttributeValue() {
    AttributeValue result = converter.transformFrom(null);

    assertThat(result).isNotNull();
    assertThat(result.nul()).isTrue();
    assertThat(result.s()).isNull();
  }

  @Test
  @DisplayName("Should transform valid ISO string AttributeValue to LocalDateTime")
  void shouldTransformValidAttributeValueToLocalDateTime() {
    AttributeValue input = createStringAttributeValue(expectedIsoString);

    LocalDateTime result = converter.transformTo(input);

    assertThat(result).isEqualTo(testDateTime);
  }

  @Test
  @DisplayName("Should transform NULL AttributeValue to null LocalDateTime")
  void shouldTransformNullAttributeValueToNullLocalDateTime() {
    AttributeValue input = createNullAttributeValue();

    LocalDateTime result = converter.transformTo(input);

    assertThat(result).isNull();
  }

  // CORRECT - Ordering tests
  @Test
  @DisplayName("Should handle round-trip conversion correctly")
  void shouldHandleRoundTripConversionCorrectly() {
    AttributeValue attributeValue = converter.transformFrom(testDateTime);
    LocalDateTime result = converter.transformTo(attributeValue);

    assertThat(result).isEqualTo(testDateTime);
  }

  // CORRECT - Range tests (RIGHT-BICEP - Range)
  @Test
  @DisplayName("Should handle minimum LocalDateTime value")
  void shouldHandleMinimumLocalDateTimeValue() {
    LocalDateTime minDateTime = LocalDateTime.MIN;

    AttributeValue result = converter.transformFrom(minDateTime);
    LocalDateTime roundTrip = converter.transformTo(result);

    assertThat(roundTrip).isEqualTo(minDateTime);
  }

  @Test
  @DisplayName("Should handle maximum LocalDateTime value")
  void shouldHandleMaximumLocalDateTimeValue() {
    LocalDateTime maxDateTime = LocalDateTime.MAX;

    AttributeValue result = converter.transformFrom(maxDateTime);
    LocalDateTime roundTrip = converter.transformTo(result);

    assertThat(roundTrip).isEqualTo(maxDateTime);
  }

  // CORRECT - Reference tests
  @Test
  @DisplayName("Should return correct EnhancedType for LocalDateTime")
  void shouldReturnCorrectEnhancedType() {
    EnhancedType<LocalDateTime> result = converter.type();

    assertThat(result).isEqualTo(EnhancedType.of(LocalDateTime.class));
  }

  @Test
  @DisplayName("Should return String AttributeValueType")
  void shouldReturnStringAttributeValueType() {
    AttributeValueType result = converter.attributeValueType();

    assertThat(result).isEqualTo(AttributeValueType.S);
  }

  // CORRECT - Existence tests (RIGHT-BICEP - Existence)
  @Test
  @DisplayName("Should handle null AttributeValue input")
  void shouldHandleNullAttributeValueInput() {
    LocalDateTime result = converter.transformTo(null);

    assertThat(result).isNull();
  }

  @Test
  @DisplayName("Should handle empty string AttributeValue")
  void shouldHandleEmptyStringAttributeValue() {
    AttributeValue input = createStringAttributeValue("");

    LocalDateTime result = converter.transformTo(input);

    assertThat(result).isNull();
  }

  @Test
  @DisplayName("Should handle whitespace-only string AttributeValue")
  void shouldHandleWhitespaceOnlyStringAttributeValue() {
    AttributeValue input = createStringAttributeValue("   ");

    LocalDateTime result = converter.transformTo(input);

    assertThat(result).isNull();
  }

  @Test
  @DisplayName("Should handle AttributeValue with null string value")
  void shouldHandleAttributeValueWithNullStringValue() {
    AttributeValue input = AttributeValue.builder().build();

    LocalDateTime result = converter.transformTo(input);

    assertThat(result).isNull();
  }

  // CORRECT - Cardinality tests (RIGHT-BICEP - Cardinality)
  @Test
  @DisplayName("Should handle LocalDateTime with nanoseconds")
  void shouldHandleLocalDateTimeWithNanoseconds() {
    LocalDateTime dateTimeWithNanos = LocalDateTime.of(2023, 6, 15, 10, 20, 30, 999999999);

    AttributeValue attributeValue = converter.transformFrom(dateTimeWithNanos);
    LocalDateTime result = converter.transformTo(attributeValue);

    assertThat(result).isEqualTo(dateTimeWithNanos);
  }

  @Test
  @DisplayName("Should handle LocalDateTime without nanoseconds")
  void shouldHandleLocalDateTimeWithoutNanoseconds() {
    LocalDateTime dateTimeWithoutNanos = LocalDateTime.of(2023, 6, 15, 10, 20, 30);

    AttributeValue attributeValue = converter.transformFrom(dateTimeWithoutNanos);
    LocalDateTime result = converter.transformTo(attributeValue);

    assertThat(result).isEqualTo(dateTimeWithoutNanos);
  }

  // CORRECT - Time tests (RIGHT-BICEP - Time)
  @Test
  @DisplayName("Should maintain temporal precision during conversion")
  void shouldMaintainTemporalPrecisionDuringConversion() {
    LocalDateTime preciseDateTime = LocalDateTime.of(2023, 12, 31, 23, 59, 59, 123456789);

    AttributeValue attributeValue = converter.transformFrom(preciseDateTime);
    LocalDateTime result = converter.transformTo(attributeValue);

    assertThat(result).isEqualTo(preciseDateTime);
    assertThat(result.getNano()).isEqualTo(123456789);
  }

  // RIGHT-BICEP - Inverse relationships
  @Test
  @DisplayName("Should maintain inverse relationship between transformFrom and transformTo")
  void shouldMaintainInverseRelationshipBetweenTransformMethods() {
    LocalDateTime originalDateTime = LocalDateTime.now();

    AttributeValue attributeValue = converter.transformFrom(originalDateTime);
    LocalDateTime convertedBack = converter.transformTo(attributeValue);

    assertThat(convertedBack).isEqualTo(originalDateTime);
  }

  // RIGHT-BICEP - Error conditions
  @Test
  @DisplayName("Should throw RuntimeException for invalid date string")
  void shouldThrowRuntimeExceptionForInvalidDateString() {
    AttributeValue invalidInput = createStringAttributeValue("not-a-valid-date");

    assertThatThrownBy(() -> converter.transformTo(invalidInput)).isInstanceOf(RuntimeException.class).hasMessageContaining("Failed to parse LocalDateTime: not-a-valid-date");
  }

  @Test
  @DisplayName("Should throw RuntimeException for malformed ISO string")
  void shouldThrowRuntimeExceptionForMalformedIsoString() {
    AttributeValue malformedInput = createStringAttributeValue("2023-13-45T25:70:90");

    assertThatThrownBy(() -> converter.transformTo(malformedInput)).isInstanceOf(RuntimeException.class).hasMessageContaining("Failed to parse LocalDateTime");
  }

  // RIGHT-BICEP - Performance characteristics
  @Test
  @DisplayName("Should handle multiple conversions efficiently")
  void shouldHandleMultipleConversionsEfficiently() {
    LocalDateTime baseDateTime = LocalDateTime.of(2023, 1, 1, 0, 0, 0);

    assertThatCode(() -> {
      for (int i = 0; i < 1000; i++) {
        LocalDateTime testDateTime = baseDateTime.plusHours(i);
        AttributeValue attributeValue = converter.transformFrom(testDateTime);
        LocalDateTime result = converter.transformTo(attributeValue);
        assertThat(result).isEqualTo(testDateTime);
      }
    }).doesNotThrowAnyException();
  }

  // Helper methods for clean, readable test code
  private AttributeValue createStringAttributeValue(String value) {
    return AttributeValue.builder().s(value).build();
  }

  private AttributeValue createNullAttributeValue() {
    return AttributeValue.builder().nul(true).build();
  }
}