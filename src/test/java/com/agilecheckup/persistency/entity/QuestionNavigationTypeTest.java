package com.agilecheckup.persistency.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QuestionNavigationTypeTest {

  @Test
  void shouldHaveThreeNavigationTypes() {
    // Given & When
    QuestionNavigationType[] values = QuestionNavigationType.values();

    // Then
    assertThat(values).hasSize(3);
    assertThat(values).containsExactlyInAnyOrder(
                                                 QuestionNavigationType.RANDOM, QuestionNavigationType.SEQUENTIAL, QuestionNavigationType.FREE_FORM
    );
  }

  @Test
  void shouldConvertFromStringValues() {
    // Then
    assertThat(QuestionNavigationType.valueOf("RANDOM")).isEqualTo(QuestionNavigationType.RANDOM);
    assertThat(QuestionNavigationType.valueOf("SEQUENTIAL")).isEqualTo(QuestionNavigationType.SEQUENTIAL);
    assertThat(QuestionNavigationType.valueOf("FREE_FORM")).isEqualTo(QuestionNavigationType.FREE_FORM);
  }

  @Test
  void shouldThrowExceptionForInvalidString() {
    // Then
    assertThatThrownBy(() -> QuestionNavigationType.valueOf("INVALID")).isInstanceOf(IllegalArgumentException.class);

    assertThatThrownBy(() -> QuestionNavigationType.valueOf("random")).isInstanceOf(IllegalArgumentException.class);

    assertThatThrownBy(() -> QuestionNavigationType.valueOf("Sequential")).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldReturnCorrectStringRepresentation() {
    // Then
    assertThat(QuestionNavigationType.RANDOM.toString()).isEqualTo("RANDOM");
    assertThat(QuestionNavigationType.SEQUENTIAL.toString()).isEqualTo("SEQUENTIAL");
    assertThat(QuestionNavigationType.FREE_FORM.toString()).isEqualTo("FREE_FORM");
  }

  @Test
  void shouldReturnCorrectName() {
    // Then
    assertThat(QuestionNavigationType.RANDOM.name()).isEqualTo("RANDOM");
    assertThat(QuestionNavigationType.SEQUENTIAL.name()).isEqualTo("SEQUENTIAL");
    assertThat(QuestionNavigationType.FREE_FORM.name()).isEqualTo("FREE_FORM");
  }

  @Test
  void shouldReturnCorrectOrdinal() {
    // Then
    assertThat(QuestionNavigationType.RANDOM.ordinal()).isEqualTo(0);
    assertThat(QuestionNavigationType.SEQUENTIAL.ordinal()).isEqualTo(1);
    assertThat(QuestionNavigationType.FREE_FORM.ordinal()).isEqualTo(2);
  }

  @Test
  void shouldSupportEqualityComparison() {
    // Given
    QuestionNavigationType random1 = QuestionNavigationType.RANDOM;
    QuestionNavigationType random2 = QuestionNavigationType.valueOf("RANDOM");
    QuestionNavigationType sequential = QuestionNavigationType.SEQUENTIAL;

    // Then
    assertThat(random1).isEqualTo(random2);
    assertThat(random1).isNotEqualTo(sequential);
    assertThat(random1 == random2).isTrue(); // Enum constants are singletons
  }
}