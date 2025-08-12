package com.agilecheckup.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenField Enum Tests")
class TokenFieldTest {

  @Test
  @DisplayName("Should return correct field name for TENANT_ID")
  void shouldReturnCorrectFieldNameForTenantId() {
    // Given
    TokenField tokenField = TokenField.TENANT_ID;

    // When
    String fieldName = tokenField.getFieldName();

    // Then
    assertThat(fieldName).isEqualTo("tenantId");
  }

  @Test
  @DisplayName("Should return correct field name for ASSESSMENT_MATRIX_ID")
  void shouldReturnCorrectFieldNameForAssessmentMatrixId() {
    // Given
    TokenField tokenField = TokenField.ASSESSMENT_MATRIX_ID;

    // When
    String fieldName = tokenField.getFieldName();

    // Then
    assertThat(fieldName).isEqualTo("assessmentMatrixId");
  }

  @ParameterizedTest
  @EnumSource(TokenField.class)
  @DisplayName("Should return non-null and non-empty field name for all enum constants")
  void shouldReturnNonNullValueForAllEnumConstants(TokenField tokenField) {
    // When
    String fieldName = tokenField.getFieldName();

    // Then
    assertThat(fieldName).isNotNull().isNotEmpty().isNotBlank();
  }

  @Test
  @DisplayName("Should return all enum constants when calling values()")
  void shouldReturnAllEnumConstants() {
    // When
    TokenField[] values = TokenField.values();

    // Then
    assertThat(values).isNotNull().hasSize(2).contains(TokenField.TENANT_ID, TokenField.ASSESSMENT_MATRIX_ID);
  }

  @Test
  @DisplayName("Should return enum constants in correct declaration order")
  void shouldReturnArrayWithCorrectOrder() {
    // When
    TokenField[] values = TokenField.values();

    // Then
    assertThat(values).containsExactly(TokenField.TENANT_ID, TokenField.ASSESSMENT_MATRIX_ID);
  }

  @Test
  @DisplayName("Should return correct enum constant for valid TENANT_ID string")
  void shouldReturnCorrectEnumForValidStringTenantId() {
    // When
    TokenField tokenField = TokenField.valueOf("TENANT_ID");

    // Then
    assertThat(tokenField).isEqualTo(TokenField.TENANT_ID).extracting(TokenField::getFieldName).isEqualTo("tenantId");
  }

  @Test
  @DisplayName("Should return correct enum constant for valid ASSESSMENT_MATRIX_ID string")
  void shouldReturnCorrectEnumForValidStringAssessmentMatrixId() {
    // When
    TokenField tokenField = TokenField.valueOf("ASSESSMENT_MATRIX_ID");

    // Then
    assertThat(tokenField).isEqualTo(TokenField.ASSESSMENT_MATRIX_ID).extracting(TokenField::getFieldName).isEqualTo("assessmentMatrixId");
  }

  @ParameterizedTest
  @ValueSource(strings = {"", " ", "tenant_id", "TENANTID", "invalid", "null"})
  @DisplayName("Should throw IllegalArgumentException for invalid enum names")
  void shouldThrowIllegalArgumentExceptionForInvalidStrings(String invalidValue) {
    // When & Then
    assertThatThrownBy(() -> TokenField.valueOf(invalidValue)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("Should throw NPE when valueOf called with null")
  void shouldThrowNullPointerExceptionForNullValue() {
    // When & Then
    assertThatThrownBy(() -> TokenField.valueOf(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  @DisplayName("Should have unique field names across all enum constants")
  void shouldHaveUniqueFieldNames() {
    // Given
    TokenField[] values = TokenField.values();

    // When
    String[] fieldNames = extractFieldNames(values);

    // Then
    assertThat(fieldNames).doesNotHaveDuplicates().hasSize(values.length);
  }

  @Test
  @DisplayName("Should return consistent string representation for enum constants")
  void shouldBeConsistentWithToString() {
    // When & Then
    assertThat(TokenField.TENANT_ID).hasToString("TENANT_ID");

    assertThat(TokenField.ASSESSMENT_MATRIX_ID).hasToString("ASSESSMENT_MATRIX_ID");
  }

  @Test
  @DisplayName("Should implement equals and hashCode correctly")
  void shouldImplementEqualsAndHashCodeCorrectly() {
    // Given
    TokenField tenantId1 = TokenField.TENANT_ID;
    TokenField tenantId2 = TokenField.valueOf("TENANT_ID");
    TokenField assessmentMatrixId = TokenField.ASSESSMENT_MATRIX_ID;

    // When & Then
    assertThat(tenantId1).isEqualTo(tenantId2).isNotEqualTo(assessmentMatrixId).hasSameHashCodeAs(tenantId2);

    assertThat(tenantId1.hashCode()).isNotEqualTo(assessmentMatrixId.hashCode());
  }

  @Test
  @DisplayName("Should be comparable and maintain declaration order")
  void shouldBeComparable() {
    // Given
    TokenField tenantId = TokenField.TENANT_ID;
    TokenField assessmentMatrixId = TokenField.ASSESSMENT_MATRIX_ID;

    // When & Then
    assertThat(tenantId).isLessThan(assessmentMatrixId).isEqualByComparingTo(tenantId);

    assertThat(assessmentMatrixId).isGreaterThan(tenantId);
  }

  @Test
  @DisplayName("Should maintain immutability of field names")
  void shouldMaintainImmutabilityOfFieldNames() {
    // Given
    TokenField tokenField = TokenField.TENANT_ID;
    String originalFieldName = tokenField.getFieldName();

    // When
    String fieldName1 = tokenField.getFieldName();
    String fieldName2 = tokenField.getFieldName();

    // Then
    assertThat(fieldName1).isEqualTo(originalFieldName).isEqualTo(fieldName2);
  }

  // Helper method for extracting field names
  private String[] extractFieldNames(TokenField[] tokenFields) {
    return java.util.Arrays.stream(tokenFields).map(TokenField::getFieldName).toArray(String[]::new);
  }
}